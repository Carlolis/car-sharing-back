import domain.models.PersonCreate
import domain.models.invoice.{DriverName, InvoiceCreate, InvoiceUpdate, Reimbursement}
import domain.services.invoice.repository.InvoiceRepository
import domain.services.invoice.storage.InvoiceStorage
import domain.services.invoice.{InvoiceService, InvoiceServiceLive}
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

    val driverName           = "TestDriver"
    val updatedDriverName    = "UpdatedDriver"
    val sampleInvoiceCreate  = InvoiceCreate(
      99,
      mileage = Some(99),
      date = LocalDate.now(),
      name = "Business",
      drivers = Set(DriverName(maePersonName)),
      kind
    )
    val initialInvoiceCreate = InvoiceCreate(
      amount = 500,
      mileage = Some(100),
      date = LocalDate.now(),
      name = "Test Invoice",
      drivers = Set(DriverName(driverName)),
      kind = "fuel",
      fileBytes = Some(FileRange(testPdfFile)),
      fileName = Some(testPdfFile.getName)
    )

    val invoiceCreateWithoutFile = InvoiceCreate(
      amount = 300,
      mileage = Some(75),
      date = LocalDate.now(),
      name = "Invoice Without File",
      drivers = Set(DriverName(driverName)),
      kind = "parking"
    )

    def createUpdateData(invoiceId: domain.models.invoice.InvoiceId) = InvoiceUpdate(
      id = invoiceId,
      amount = 750,                                 // Updated amount
      mileage = Some(150),                          // Updated mileage
      date = LocalDate.now().plusDays(1),           // Updated date
      name = "Updated Test Invoice",                // Updated name
      drivers = Set(DriverName(updatedDriverName)), // Updated drivers
      kind = "maintenance",                         // Updated kind
      fileName = Some(testPdfFile.getName),
      fileBytes = Some(FileRange(testPdfFile))
    )

    val expectedReimbursementAmount = 33
  }

  // Test utilities
  object TestUtils {
    def findReimbursementByDriver(reimbursements: Set[Reimbursement], driverName: String): IO[Option[Nothing], Reimbursement] =
      ZIO.fromOption(
        reimbursements.find(r =>
          // Adaptation nécessaire selon la structure réelle de vos objets de remboursement
          r.driverName == DriverName(driverName)))
    def cleanupStorage: ZIO[InvoiceStorage, Throwable, Unit]                                                                  =
      for {
        _ <- InvoiceStorage.delete(TestData.testPdfFile.getName).catchAll(_ => ZIO.unit)
        _ <- ZIO.log("[DEBUG_LOG] Storage cleanup completed")
      } yield ()
  }

  // Create layer configuration using InvoiceServiceLive with in-memory implementations
  // Create layer configuration using InvoiceServiceLive with in-memory implementations
  val testLayers = ZLayer.make[InvoiceService & InvoiceStorage](
    InMemoryInvoiceStorage.layer,
    InMemoryInvoiceRepository.layer,
    InvoiceServiceLive.layer,
    InMemoryPersonRepository.layer
  )

  def spec: Spec[TestEnvironment & Scope, Any] =
    (suite("Invoice Service Test with In-Memory Storage")(
      test("Calcul des remboursements - Distribution équitable entre 3 conducteurs") {
        for {
          _              <- InvoiceService.createInvoice(TestData.sampleInvoiceCreate)
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

          val maeDistributionAssertion = assert(
            maeReimbursement.to
          )(
            equalTo(
              Map(
                DriverName(TestData.brigittePersonName) -> 0,
                DriverName(TestData.charlesPersonName)  -> 0
              )))

          val charlesDistributionAssertion = assert(
            charlesReimbursement.to
          )(
            equalTo(
              Map(
                DriverName(TestData.brigittePersonName) -> 0,
                DriverName(TestData.maePersonName)      -> TestData.expectedReimbursementAmount
              )))

          val brigitteDistributionAssertion = assert(
            brigitteReimbursement.to
          )(
            equalTo(
              Map(
                DriverName(TestData.maePersonName)     -> TestData.expectedReimbursementAmount,
                DriverName(TestData.charlesPersonName) -> 0
              )))

          baseAssertions &&
          maeDistributionAssertion &&
          charlesDistributionAssertion &&
          brigitteDistributionAssertion
        }
      }
    ) @@ TestAspect.after(
      TestUtils.cleanupStorage.catchAll(e => ZIO.logError(s"[DEBUG_LOG] Cleanup error: ${e.getMessage}"))
    ) @@ TestAspect.sequential).provideShared(testLayers)
}
