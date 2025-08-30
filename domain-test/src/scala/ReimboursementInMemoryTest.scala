import domain.models.PersonCreate
import domain.models.invoice.{DriverName, InvoiceCreate, InvoiceUpdate, Reimbursement}
import domain.services.invoice.repository.InvoiceRepository
import domain.services.invoice.storage.InvoiceStorage
import domain.services.invoice.{InvoiceService, InvoiceServiceLive}
import domain.services.person.PersonService
import inMemoryService.{InMemoryInvoiceRepository, InMemoryInvoiceStorage, InMemoryPersonRepository}
import sttp.tapir.FileRange
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
      99,
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
      amount = 750,                                 // Updated amount
      mileage = Some(150),                          // Updated mileage
      date = LocalDate.now().plusDays(1),           // Updated date
      name = "Updated Test Invoice",                // Updated name
      driver = DriverName(updatedDriverName), // Updated drivers
      kind = "maintenance",                         // Updated kind
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
    def cleanup: ZIO[InvoiceService & PersonService, Throwable, Unit]                                                                  =
      for {
        allInvoices <- InvoiceService.getAllInvoices
        _           <- ZIO.foreachPar(allInvoices)(invoice => InvoiceService.deleteInvoice(invoice.id)).catchAll(_ => ZIO.unit)
        allPersons <- PersonService.getAll
        _          <- ZIO.foreachPar(allPersons)(person => PersonService.deletePerson(person.id)).catchAll(_ => ZIO.unit)
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
          reimbursements <- InvoiceService.getReimbursementProposal

          maeReimbursement      <- TestUtils.findReimbursementByDriver(reimbursements, TestData.maePersonName)
          charlesReimbursement  <- TestUtils.findReimbursementByDriver(reimbursements, TestData.charlesPersonName)
          brigitteReimbursement <- TestUtils.findReimbursementByDriver(reimbursements, TestData.brigittePersonName)

        } yield {
          val baseAssertions = assertTrue(
            reimbursements.size == 3,
            maeReimbursement.totalAmount == 0,
            charlesReimbursement.totalAmount == TestData.expectedReimbursementAmount,
            brigitteReimbursement.totalAmount == TestData.expectedReimbursementAmount
          )

          val maeDistributionAssertion = assertTrue(
            maeReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0f,
              DriverName(TestData.charlesPersonName)  -> 0f
            )
          )

          val charlesDistributionAssertion = assertTrue(
            charlesReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0f,
              DriverName(TestData.maePersonName)      -> TestData.expectedReimbursementAmount
            )
          )

          val brigitteDistributionAssertion = assertTrue(
            brigitteReimbursement.to == Map(
              DriverName(TestData.maePersonName)     -> TestData.expectedReimbursementAmount,
              DriverName(TestData.charlesPersonName) -> 0f
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
          reimbursements <- InvoiceService.getReimbursementProposal

          maeReimbursement      <- TestUtils.findReimbursementByDriver(reimbursements, TestData.maePersonName)
          charlesReimbursement  <- TestUtils.findReimbursementByDriver(reimbursements, TestData.charlesPersonName)
          brigitteReimbursement <- TestUtils.findReimbursementByDriver(reimbursements, TestData.brigittePersonName)

        } yield {
          val baseAssertions = assertTrue(
            reimbursements.size == 3,
            maeReimbursement.totalAmount == 0,
            charlesReimbursement.totalAmount == 0,
            brigitteReimbursement.totalAmount == 57f
          )

          val maeDistributionAssertion = assertTrue(
            maeReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0f,
              DriverName(TestData.charlesPersonName)  -> 0f
            )
          )

          val charlesDistributionAssertion = assertTrue(
            charlesReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0f,
              DriverName(TestData.maePersonName)      -> 0f
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
          reimbursements <- InvoiceService.getReimbursementProposal

          maeReimbursement      <- TestUtils.findReimbursementByDriver(reimbursements, TestData.maePersonName)
          charlesReimbursement  <- TestUtils.findReimbursementByDriver(reimbursements, TestData.charlesPersonName)
          brigitteReimbursement <- TestUtils.findReimbursementByDriver(reimbursements, TestData.brigittePersonName)

        } yield {
          val baseAssertions = assertTrue(
            reimbursements.size == 3,
            maeReimbursement.totalAmount == 0,
            charlesReimbursement.totalAmount == 31f,
            brigitteReimbursement.totalAmount == 34f
          )

          val maeDistributionAssertion = assertTrue(
            maeReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0f,
              DriverName(TestData.charlesPersonName)  -> 0f
            )
          )

          val charlesDistributionAssertion = assertTrue(
            charlesReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0f,
              DriverName(TestData.maePersonName)      -> 31f
            )
          )

          val brigitteDistributionAssertion = assertTrue(
            brigitteReimbursement.to == Map(
              DriverName(TestData.maePersonName)     -> 34f,
              DriverName(TestData.charlesPersonName) -> 0f
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
          reimbursements <- InvoiceService.getReimbursementProposal

          maeReimbursement      <- TestUtils.findReimbursementByDriver(reimbursements, TestData.maePersonName)
          charlesReimbursement  <- TestUtils.findReimbursementByDriver(reimbursements, TestData.charlesPersonName)
          brigitteReimbursement <- TestUtils.findReimbursementByDriver(reimbursements, TestData.brigittePersonName)

        } yield {
          val baseAssertions = assertTrue(
            reimbursements.size == 3,
            maeReimbursement.totalAmount == 0,
            charlesReimbursement.totalAmount == 33f,
            brigitteReimbursement.totalAmount == 30f
          )

          val maeDistributionAssertion = assertTrue(
            maeReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0f,
              DriverName(TestData.charlesPersonName)  -> 0f
            )
          )

          val charlesDistributionAssertion = assertTrue(
            charlesReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0f,
              DriverName(TestData.maePersonName)      -> 33f
            )
          )

          val brigitteDistributionAssertion = assertTrue(
            brigitteReimbursement.to == Map(
              DriverName(TestData.maePersonName)     -> 30f,
              DriverName(TestData.charlesPersonName) -> 0f
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
          reimbursements <- InvoiceService.getReimbursementProposal

          maeReimbursement      <- TestUtils.findReimbursementByDriver(reimbursements, TestData.maePersonName)
          charlesReimbursement  <- TestUtils.findReimbursementByDriver(reimbursements, TestData.charlesPersonName)
          brigitteReimbursement <- TestUtils.findReimbursementByDriver(reimbursements, TestData.brigittePersonName)

        } yield {
          val baseAssertions = assertTrue(
            reimbursements.size == 3,
            maeReimbursement.totalAmount == 0,
            charlesReimbursement.totalAmount == 0,
            brigitteReimbursement.totalAmount == 51f
          )

          val maeDistributionAssertion = assertTrue(
            maeReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0f,
              DriverName(TestData.charlesPersonName)  -> 0f
            )
          )

          val charlesDistributionAssertion = assertTrue(
            charlesReimbursement.to == Map(
              DriverName(TestData.brigittePersonName) -> 0f,
              DriverName(TestData.maePersonName)      -> 0f
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
      test("Create reimbursement invoice and verify it is stored correctly") {
        val reimbursementCreate = domain.models.invoice.ReimbursementInvoiceCreate(
          amount = 50L,
          date = LocalDate.now(),
          name = "Test Reimbursement",
          fromDriver = DriverName(TestData.maePersonName),
          toDriver = DriverName(TestData.charlesPersonName),
          description = "Reimbursement for shared expenses"
        )
        for {
          invoiceId            <- InvoiceService.createReimbursementInvoice(reimbursementCreate)
          allInvoices          <- InvoiceService.getAllInvoices
          _                    <- ZIO.log(s"[DEBUG_LOG] Created reimbursement invoice with id: $invoiceId")
          reimbursementInvoice <- ZIO.fromOption(allInvoices.find(_.id == invoiceId))
                                    .mapError(_ => new RuntimeException("Reimbursement invoice not found"))
          _                    <- ZIO.log(s"[DEBUG_LOG] Found invoice: ${reimbursementInvoice.toString}")
        } yield assertTrue(
          reimbursementInvoice.isReimbursement,
          reimbursementInvoice.amount == 50L,
          reimbursementInvoice.name == "Test Reimbursement",
          reimbursementInvoice.driver == DriverName(TestData.maePersonName),
          reimbursementInvoice.kind == "reimbursement"
        )
      }
    ) @@ TestAspect.after(
      TestUtils.cleanup.catchAll(e => ZIO.logError(s"[DEBUG_LOG] Cleanup error: ${e.getMessage}"))
    ) @@ TestAspect.sequential).provideShared(testLayers)
}
