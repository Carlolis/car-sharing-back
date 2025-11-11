import domain.models.PersonCreate
import domain.models.invoice.{DriverName, InvoiceCreate, InvoiceUpdate, Reimbursement}
import domain.services.invoice.repository.InvoiceRepository
import domain.services.invoice.storage.InvoiceStorage
import domain.services.invoice.{InvoiceService, InvoiceServiceLive}
import domain.services.person.PersonService
import inMemoryService.{InMemoryInvoiceRepository, InMemoryInvoiceStorage, InMemoryPersonRepository}
import sttp.tapir.FileRange
import zio.parser.Parser.Ignore
import zio.test.*
import zio.test.Assertion.*
import zio.{IO, Scope, ZIO, ZLayer}

import java.io.File
import java.nio.file.Files
import java.time.LocalDate

object ReimboursementInMemoryTest extends ZIOSpecDefault {

  // Test data configuration
  object TestData {
    val maePersonName      = "maé"
    val charlesPersonName  = "charles"
    val brigittePersonName = "brigitte"
    var kind               = "péage"

    val mae      = PersonCreate(maePersonName)
    val charles  = PersonCreate(charlesPersonName)
    val brigitte = PersonCreate(brigittePersonName)

    val allPersons               = Set(mae, charles, brigitte)
    val testPdfFile              = new File("test.pdf")
    val fileContent: Array[Byte] = Files.readAllBytes(testPdfFile.toPath)

    val driverName                  = "TestDriver"
    val updatedDriverName           = "UpdatedDriver"
    val sampleMaéInvoiceCreate      = InvoiceCreate(
      amount = 99,
      mileage = Some(99),
      date = LocalDate.now(),
      name = "Business",
      driver = DriverName(maePersonName),
      kind
    )
    val sampleCharlesInvoiceCreate  = InvoiceCreate(
      72,
      mileage = Some(40),
      date = LocalDate.now(),
      name = "Business",
      driver = DriverName(charlesPersonName),
      kind
    )
    val sampleBrigitteInvoiceCreate = InvoiceCreate(
      6,
      mileage = Some(6),
      date = LocalDate.now(),
      name = "Business",
      driver = DriverName(brigittePersonName),
      kind
    )

    val initialInvoiceCreate = InvoiceCreate(
      amount = 500,
      mileage = Some(100),
      date = LocalDate.now(),
      name = "Test Invoice",
      driver = DriverName(driverName),
      kind = "fuel",
      fileBytes = Some(FileRange(testPdfFile)),
      fileName = Some(testPdfFile.getName)
    )

    val invoiceCreateWithoutFile = InvoiceCreate(
      amount = 300,
      mileage = Some(75),
      date = LocalDate.now(),
      name = "Invoice Without File",
      driver = DriverName(driverName),
      kind = "parking"
    )

    def createUpdateData(invoiceId: domain.models.invoice.InvoiceId) = InvoiceUpdate(
      id = invoiceId,
      amount = 750,                           // Updated amount
      mileage = Some(150),                    // Updated mileage
      date = LocalDate.now().plusDays(1),     // Updated date
      name = "Updated Test Invoice",          // Updated name
      driver = DriverName(updatedDriverName), // Updated drivers
      kind = "maintenance",                   // Updated kind
      fileName = Some(testPdfFile.getName),
      fileBytes = Some(FileRange(testPdfFile))
    )

    val expectedReimbursementAmount = 33f
  }

  // Test utilities
  object TestUtils {
    def findReimbursementByDriver(reimbursements: Set[Reimbursement], driverName: String): IO[Option[Nothing], Reimbursement] =
      ZIO.fromOption(
        reimbursements.find(r =>
          // Adaptation nécessaire selon la structure réelle de vos objets de remboursement
          r.driverName == DriverName(driverName)))
    def cleanup: ZIO[InvoiceService & PersonService, Throwable, Unit]                                                         =
      for {
        allInvoices <- InvoiceService.getAllInvoices
        _           <- ZIO.foreachPar(allInvoices)(invoice => InvoiceService.deleteInvoice(invoice.id)).catchAll(_ => ZIO.unit)
        allPersons  <- PersonService.getAll
        _           <- ZIO.foreachPar(allPersons)(person => PersonService.deletePerson(person.id)).catchAll(_ => ZIO.unit)
        _           <- ZIO.log("[DEBUG_LOG] Storage cleanup completed")
      } yield ()
  }

  // Create layer configuration using InvoiceServiceLive with in-memory implementations
  // Create layer configuration using InvoiceServiceLive with in-memory implementations
  val testLayers = ZLayer.make[InvoiceService & InvoiceStorage & PersonService](
    InMemoryInvoiceStorage.layer,
    InMemoryInvoiceRepository.layer,
    InvoiceServiceLive.layer,
    InMemoryPersonRepository.layer
  )

  def spec: Spec[TestEnvironment & Scope, Any] =
    (suite("Invoice Service Test with In-Memory Storage Calcul des remboursements")(
      test("Un seul conducteur a une facture, deux conducteurs doivent rembourser le premier") {
        for {
          _              <- InvoiceService.createInvoice(TestData.sampleMaéInvoiceCreate)
          reimbursements <- InvoiceService.getReimbursementProposals

          maeReimbursement      <- TestUtils.findReimbursementByDriver(reimbursements, TestData.maePersonName)
          charlesReimbursement  <- TestUtils.findReimbursementByDriver(reimbursements, TestData.charlesPersonName)
          brigitteReimbursement <- TestUtils.findReimbursementByDriver(reimbursements, TestData.brigittePersonName)

        } yield {
          val baseAssertions = assertTrue(
            reimbursements.size == 3,
            maeReimbursement.totalAmount == -66,
            charlesReimbursement.totalAmount == TestData.expectedReimbursementAmount,
            brigitteReimbursement.totalAmount == TestData.expectedReimbursementAmount
          )

          val maeDistributionAssertion = assertTrue(
            maeReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0L,
              DriverName(TestData.charlesPersonName)  -> 0L
            )
          )

          val charlesDistributionAssertion = assertTrue(
            charlesReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0L,
              DriverName(TestData.maePersonName)      -> TestData.expectedReimbursementAmount
            )
          )

          val brigitteDistributionAssertion = assertTrue(
            brigitteReimbursement.to == Map(
              DriverName(TestData.maePersonName)     -> TestData.expectedReimbursementAmount,
              DriverName(TestData.charlesPersonName) -> 0L
            )
          )

          baseAssertions &&
          maeDistributionAssertion &&
          charlesDistributionAssertion &&
          brigitteDistributionAssertion
        }
      },
      test("Un seul conducteur avec un montant non entier a une facture, deux conducteurs doivent rembourser le premier") {
        for {
          _              <- InvoiceService.createInvoice(TestData.sampleMaéInvoiceCreate.copy(amount = 24.12))
          reimbursements <- InvoiceService.getReimbursementProposals

          maeReimbursement      <- TestUtils.findReimbursementByDriver(reimbursements, TestData.maePersonName)
          charlesReimbursement  <- TestUtils.findReimbursementByDriver(reimbursements, TestData.charlesPersonName)
          brigitteReimbursement <- TestUtils.findReimbursementByDriver(reimbursements, TestData.brigittePersonName)

        } yield {
          val baseAssertions = assertTrue(
            reimbursements.size == 3,
            maeReimbursement.totalAmount == -16.08,
            charlesReimbursement.totalAmount == 8.04,
            brigitteReimbursement.totalAmount == 8.04
          )

          val maeDistributionAssertion = assertTrue(
            maeReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0.0,
              DriverName(TestData.charlesPersonName)  -> 0.0
            )
          )

          val charlesDistributionAssertion = assertTrue(
            charlesReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0.0,
              DriverName(TestData.maePersonName)      -> 8.04
            )
          )

          val brigitteDistributionAssertion = assertTrue(
            brigitteReimbursement.to == Map(
              DriverName(TestData.maePersonName)     -> 8.04,
              DriverName(TestData.charlesPersonName) -> 0.0
            )
          )

          baseAssertions &&
          maeDistributionAssertion &&
          charlesDistributionAssertion &&
          brigitteDistributionAssertion
        }
      },
      test("2 conducteurs sont au-dessus, le dernier doit les rembourser") {
        for {
          _              <- InvoiceService.createInvoice(TestData.sampleMaéInvoiceCreate)
          _              <- InvoiceService.createInvoice(TestData.sampleCharlesInvoiceCreate)
          reimbursements <- InvoiceService.getReimbursementProposals

          maeReimbursement      <- TestUtils.findReimbursementByDriver(reimbursements, TestData.maePersonName)
          charlesReimbursement  <- TestUtils.findReimbursementByDriver(reimbursements, TestData.charlesPersonName)
          brigitteReimbursement <- TestUtils.findReimbursementByDriver(reimbursements, TestData.brigittePersonName)

        } yield {
          val baseAssertions = assertTrue(
            reimbursements.size == 3,
            maeReimbursement.totalAmount == -42,
            charlesReimbursement.totalAmount == -15,
            brigitteReimbursement.totalAmount == 57
          )

          val maeDistributionAssertion = assertTrue(
            maeReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0L,
              DriverName(TestData.charlesPersonName)  -> 0L
            )
          )

          val charlesDistributionAssertion = assertTrue(
            charlesReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0L,
              DriverName(TestData.maePersonName)      -> 0L
            )
          )

          val brigitteDistributionAssertion = assertTrue(
            brigitteReimbursement.to == Map(
              DriverName(TestData.maePersonName)     -> 42f,
              DriverName(TestData.charlesPersonName) -> 15f
            )
          )

          baseAssertions &&
          maeDistributionAssertion &&
          charlesDistributionAssertion &&
          brigitteDistributionAssertion
        }
      },
      test("1 conducteur est au-dessus, mais il y a deux factures deux conducteurs doivent rembourser le premier") {
        for {
          _              <- InvoiceService.createInvoice(TestData.sampleMaéInvoiceCreate)
          _              <- InvoiceService.createInvoice(TestData.sampleCharlesInvoiceCreate.copy(amount = 3))
          reimbursements <- InvoiceService.getReimbursementProposals

          maeReimbursement      <- TestUtils.findReimbursementByDriver(reimbursements, TestData.maePersonName)
          charlesReimbursement  <- TestUtils.findReimbursementByDriver(reimbursements, TestData.charlesPersonName)
          brigitteReimbursement <- TestUtils.findReimbursementByDriver(reimbursements, TestData.brigittePersonName)

        } yield {
          val baseAssertions = assertTrue(
            reimbursements.size == 3,
            maeReimbursement.totalAmount == -65,
            charlesReimbursement.totalAmount == 31f,
            brigitteReimbursement.totalAmount == 34f
          )

          val maeDistributionAssertion = assertTrue(
            maeReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0L,
              DriverName(TestData.charlesPersonName)  -> 0L
            )
          )

          val charlesDistributionAssertion = assertTrue(
            charlesReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0L,
              DriverName(TestData.maePersonName)      -> 31f
            )
          )

          val brigitteDistributionAssertion = assertTrue(
            brigitteReimbursement.to == Map(
              DriverName(TestData.maePersonName)     -> 34f,
              DriverName(TestData.charlesPersonName) -> 0L
            )
          )

          baseAssertions &&
          maeDistributionAssertion &&
          charlesDistributionAssertion &&
          brigitteDistributionAssertion
        }
      },
      test("1 conducteur est au-dessus, mais il y a trois factures deux conducteurs doivent rembourser le premier") {
        for {
          _              <- InvoiceService.createInvoice(TestData.sampleMaéInvoiceCreate)
          _              <- InvoiceService.createInvoice(TestData.sampleCharlesInvoiceCreate.copy(amount = 3))
          _              <- InvoiceService.createInvoice(TestData.sampleBrigitteInvoiceCreate.copy(amount = 6))
          reimbursements <- InvoiceService.getReimbursementProposals

          maeReimbursement      <- TestUtils.findReimbursementByDriver(reimbursements, TestData.maePersonName)
          charlesReimbursement  <- TestUtils.findReimbursementByDriver(reimbursements, TestData.charlesPersonName)
          brigitteReimbursement <- TestUtils.findReimbursementByDriver(reimbursements, TestData.brigittePersonName)

        } yield {
          val baseAssertions = assertTrue(
            reimbursements.size == 3,
            maeReimbursement.totalAmount == -63,
            charlesReimbursement.totalAmount == 33f,
            brigitteReimbursement.totalAmount == 30L
          )

          val maeDistributionAssertion = assertTrue(
            maeReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0L,
              DriverName(TestData.charlesPersonName)  -> 0L
            )
          )

          val charlesDistributionAssertion = assertTrue(
            charlesReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0L,
              DriverName(TestData.maePersonName)      -> 33f
            )
          )

          val brigitteDistributionAssertion = assertTrue(
            brigitteReimbursement.to == Map(
              DriverName(TestData.maePersonName)     -> 30L,
              DriverName(TestData.charlesPersonName) -> 0L
            )
          )

          baseAssertions &&
          maeDistributionAssertion &&
          charlesDistributionAssertion &&
          brigitteDistributionAssertion
        }
      },
      test("2 conducteurs sont au-dessus, il y a trois factures un conducteur doit rembourser les deux autres") {
        for {
          _              <- InvoiceService.createInvoice(TestData.sampleMaéInvoiceCreate)
          _              <- InvoiceService.createInvoice(TestData.sampleCharlesInvoiceCreate.copy(amount = 66))
          _              <- InvoiceService.createInvoice(TestData.sampleBrigitteInvoiceCreate.copy(amount = 6))
          reimbursements <- InvoiceService.getReimbursementProposals

          maeReimbursement      <- TestUtils.findReimbursementByDriver(reimbursements, TestData.maePersonName)
          charlesReimbursement  <- TestUtils.findReimbursementByDriver(reimbursements, TestData.charlesPersonName)
          brigitteReimbursement <- TestUtils.findReimbursementByDriver(reimbursements, TestData.brigittePersonName)

        } yield {
          val baseAssertions = assertTrue(
            reimbursements.size == 3,
            maeReimbursement.totalAmount == -42,
            charlesReimbursement.totalAmount == -9,
            brigitteReimbursement.totalAmount == 51f
          )

          val maeDistributionAssertion = assertTrue(
            maeReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0L,
              DriverName(TestData.charlesPersonName)  -> 0L
            )
          )

          val charlesDistributionAssertion = assertTrue(
            charlesReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0L,
              DriverName(TestData.maePersonName)      -> 0L
            )
          )

          val brigitteDistributionAssertion = assertTrue(
            brigitteReimbursement.to == Map(
              DriverName(TestData.maePersonName)     -> 42f,
              DriverName(TestData.charlesPersonName) -> 9f
            )
          )

          baseAssertions &&
          maeDistributionAssertion &&
          charlesDistributionAssertion &&
          brigitteDistributionAssertion
        }
      },
      test(
        "1 conducteur est au-dessus, il y a 1 facture, et deux remboursements faible deux conducteurs doivent rembourser les deux autres") {
        for {
          _              <- InvoiceService.createInvoice(TestData.sampleMaéInvoiceCreate)
          _              <- InvoiceService.createInvoice(
                              TestData.sampleCharlesInvoiceCreate.copy(amount = 3, kind = "Remboursement", toDriver = Some(DriverName("maé"))))
          _              <- InvoiceService.createInvoice(
                              TestData.sampleBrigitteInvoiceCreate.copy(amount = 6, kind = "Remboursement", toDriver = Some(DriverName("maé"))))
          reimbursements <- InvoiceService.getReimbursementProposals

          maeReimbursement      <- TestUtils.findReimbursementByDriver(reimbursements, TestData.maePersonName)
          charlesReimbursement  <- TestUtils.findReimbursementByDriver(reimbursements, TestData.charlesPersonName)
          brigitteReimbursement <- TestUtils.findReimbursementByDriver(reimbursements, TestData.brigittePersonName)

        } yield {
          val baseAssertions = assertTrue(
            reimbursements.size == 3,
            maeReimbursement.totalAmount == -57,
            charlesReimbursement.totalAmount == 30,
            brigitteReimbursement.totalAmount == 27
          )

          val maeDistributionAssertion = assertTrue(
            maeReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0L,
              DriverName(TestData.charlesPersonName)  -> 0L
            )
          )

          val charlesDistributionAssertion = assertTrue(
            charlesReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0L,
              DriverName(TestData.maePersonName)      -> 30
            )
          )

          val brigitteDistributionAssertion = assertTrue(
            brigitteReimbursement.to == Map(
              DriverName(TestData.maePersonName)     -> 27,
              DriverName(TestData.charlesPersonName) -> 0L
            )
          )

          baseAssertions &&
          maeDistributionAssertion &&
          charlesDistributionAssertion &&
          brigitteDistributionAssertion
        }
      },
      test("Create reimbursement invoice and verify it is stored correctly") {

        for {
          _              <- InvoiceService.createInvoice(TestData.sampleMaéInvoiceCreate)
          invoiceId      <-
            InvoiceService.createInvoice(
              TestData.sampleCharlesInvoiceCreate.copy(kind = "Remboursement", amount = 33, toDriver = Some(DriverName("maé"))))
          allInvoices    <- InvoiceService.getAllInvoices
          _              <- ZIO.log(s"[DEBUG_LOG] Created reimbursement invoice with id: $invoiceId")
          reimbursements <- InvoiceService.getReimbursementProposals

          maeReimbursement      <- TestUtils.findReimbursementByDriver(reimbursements, TestData.maePersonName)
          charlesReimbursement  <- TestUtils.findReimbursementByDriver(reimbursements, TestData.charlesPersonName)
          brigitteReimbursement <- TestUtils.findReimbursementByDriver(reimbursements, TestData.brigittePersonName)
          _                     <- ZIO.log(s"[DEBUG_LOG] Found invoice: ${maeReimbursement.toString}")
        } yield {
          val baseAssertions = assertTrue(
            reimbursements.size == 3,
            maeReimbursement.totalAmount == -33,
            charlesReimbursement.totalAmount == 0,
            brigitteReimbursement.totalAmount == 33
          )

          val maeDistributionAssertion = assertTrue(
            maeReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0,
              DriverName(TestData.charlesPersonName)  -> 0
            )
          )

          val charlesDistributionAssertion = assertTrue(
            charlesReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0,
              DriverName(TestData.maePersonName)      -> 0
            )
          )

          val brigitteDistributionAssertion = assertTrue(
            brigitteReimbursement.to == Map(
              DriverName(TestData.maePersonName)     -> 33,
              DriverName(TestData.charlesPersonName) -> 0
            )
          )

          baseAssertions &&
          maeDistributionAssertion &&
          charlesDistributionAssertion &&
          brigitteDistributionAssertion
        }
      },
      test("La dépense Carburant est associée au driver qui la paie pour un autre") {
        for {
          _              <- InvoiceService.createInvoice(TestData.sampleMaéInvoiceCreate)
          _              <- InvoiceService.createInvoice(
                              TestData.sampleCharlesInvoiceCreate.copy(kind = "Carburant", amount = 33, toDriver = Some(DriverName("maé"))))
          reimbursements <- InvoiceService.getReimbursementProposals

          maeReimbursement      <- TestUtils.findReimbursementByDriver(reimbursements, TestData.maePersonName)
          charlesReimbursement  <- TestUtils.findReimbursementByDriver(reimbursements, TestData.charlesPersonName)
          brigitteReimbursement <- TestUtils.findReimbursementByDriver(reimbursements, TestData.brigittePersonName)
        } yield {
          val baseAssertions = assertTrue(
            reimbursements.size == 3,
            maeReimbursement.totalAmount == -33,
            charlesReimbursement.totalAmount == 0,
            brigitteReimbursement.totalAmount == 33
          )

          val maeDistributionAssertion = assertTrue(
            maeReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0,
              DriverName(TestData.charlesPersonName)  -> 0
            )
          )

          val charlesDistributionAssertion = assertTrue(
            charlesReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0,
              DriverName(TestData.maePersonName)      -> 0
            )
          )

          val brigitteDistributionAssertion = assertTrue(
            brigitteReimbursement.to == Map(
              DriverName(TestData.maePersonName)     -> 33,
              DriverName(TestData.charlesPersonName) -> 0
            )
          )

          baseAssertions &&
          maeDistributionAssertion &&
          charlesDistributionAssertion &&
          brigitteDistributionAssertion
        }
      },
        test("Un driver n'a pas de dépense, un autre driver a fait une dépense pour lui et doit être remboursé (péage) ") {
        for {
          _              <- InvoiceService.createInvoice(
            TestData.sampleCharlesInvoiceCreate.copy(kind = "Carburant", amount = 50, toDriver = Some(DriverName("maé"))))
          reimbursements <- InvoiceService.getReimbursementProposals

          maeReimbursement      <- TestUtils.findReimbursementByDriver(reimbursements, TestData.maePersonName)
          charlesReimbursement  <- TestUtils.findReimbursementByDriver(reimbursements, TestData.charlesPersonName)
          brigitteReimbursement <- TestUtils.findReimbursementByDriver(reimbursements, TestData.brigittePersonName)
        } yield {
          val baseAssertions = assertTrue(
            reimbursements.size == 3,
            maeReimbursement.totalAmount == 50,
            charlesReimbursement.totalAmount == -50,
            brigitteReimbursement.totalAmount == 0
          )

          val maeDistributionAssertion = assertTrue(
            maeReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0,
              DriverName(TestData.charlesPersonName)  -> 50
            )
          )

          val charlesDistributionAssertion = assertTrue(
            charlesReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0,
              DriverName(TestData.maePersonName)      -> 0
            )
          )

          val brigitteDistributionAssertion = assertTrue(
            brigitteReimbursement.to == Map(
              DriverName(TestData.maePersonName)     -> 0,
              DriverName(TestData.charlesPersonName) -> 0
            )
          )

          baseAssertions &&
            maeDistributionAssertion &&
            charlesDistributionAssertion &&
            brigitteDistributionAssertion
        }
      } 
    ) @@ TestAspect.after(
      TestUtils.cleanup.catchAll(e => ZIO.logError(s"[DEBUG_LOG] Cleanup error: ${e.getMessage}"))
    ) @@ TestAspect.sequential).provideShared(testLayers)
}
