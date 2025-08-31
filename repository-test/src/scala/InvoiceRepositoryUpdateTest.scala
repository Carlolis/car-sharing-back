import adapters.GelDriver
import domain.models.PersonCreate
import domain.models.invoice.*
import domain.services.invoice.repository.InvoiceRepository
import domain.services.person.PersonService
import gel.invoice.InvoiceRepositoryGel
import gel.person.PersonRepositoryGel
import zio.test.*
import zio.test.Assertion.*
import zio.{Scope, ZIO, ZLayer}

import java.time.LocalDate

object InvoiceRepositoryUpdateTest extends ZIOSpecDefault {
  object TestData {
    val testMae            = "maé"
    val testCharles        = "charles"
    val testBrigitte       = "brigitte"
    val testPersonMae      = PersonCreate(testMae)
    val testPersonCharles  = PersonCreate(testCharles)
    val testPersonBrigitte = PersonCreate(testBrigitte)

    val initialInvoiceCreate = InvoiceCreate(
      amount = 100,
      mileage = Some(50),
      date = LocalDate.now(),
      name = "Initial Invoice",
      driver = DriverName(testMae),
      kind = "test",
      fileName = Some("initial_file.pdf")
    )

    val createInvoiceForUpdate = (invoiceId: InvoiceId) =>
      Invoice(
        id = invoiceId,
        name = "Updated Invoice",
        amount = 150,
        date = LocalDate.now().plusDays(1),
        driver = DriverName(testMae),
        kind = "test_updated",
        mileage = Some(75),
        fileName = Some("updated_file.pdf")
      )

    val createInvoiceWithoutFile = (invoiceId: InvoiceId) =>
      Invoice(
        id = invoiceId,
        name = "Updated Invoice No File",
        amount = 200,
        date = LocalDate.now().plusDays(2),
        driver = DriverName(testMae),
        kind = "test_no_file",
        mileage = Some(100),
        fileName = None
      )
  }

  object TestUtils {
    def cleanupData: ZIO[PersonService & InvoiceRepository, Throwable, Unit] =
      for {
        allInvoices <- InvoiceRepository.getAllInvoices
        _           <- ZIO.foreachDiscard(allInvoices)(invoice => InvoiceRepository.deleteInvoice(invoice.id))
        allPersons  <- PersonService.getAll
        _           <- ZIO.foreachDiscard(allPersons)(person => PersonService.deletePerson(person.id))
      } yield ()

    def setupTestData: ZIO[PersonService, Throwable, Unit] =
      for {
        _ <- PersonService.createPerson(TestData.testPersonMae)
        _ <- PersonService.createPerson(TestData.testPersonCharles)
        _ <- PersonService.createPerson(TestData.testPersonBrigitte)
      } yield ()
  }

  def spec: Spec[TestEnvironment & Scope, Any] =
    (suite("InvoiceRepositoryUpdate - Repository-only Update Tests")(
      test("[DEBUG_LOG] Repository update invoice - should update all fields correctly") {
        for {
          _ <- ZIO.log("[DEBUG_LOG] Starting repository test: Update invoice fields")

          // Create initial invoice via repository
          invoiceUuid <- InvoiceRepository.createInvoice(TestData.initialInvoiceCreate)
          invoiceId    = InvoiceId(invoiceUuid)
          _           <- ZIO.log(s"[DEBUG_LOG] Created initial invoice with ID: $invoiceId")

          // Verify initial invoice
          allInvoices   <- InvoiceRepository.getAllInvoices
          initialInvoice = allInvoices.find(_.id == invoiceId).get
          _             <- ZIO.log(s"[DEBUG_LOG] Initial invoice fileName: ${initialInvoice.fileName}")
          _             <- ZIO.log(s"[DEBUG_LOG] Initial invoice name: ${initialInvoice.name}")

          // Update invoice via repository
          updateData = TestData.createInvoiceForUpdate(invoiceId)
          updatedId <- InvoiceRepository.updateInvoice(updateData)
          _         <- ZIO.log(s"[DEBUG_LOG] Updated invoice ID: $updatedId")

          // Verify update
          updatedInvoices <- InvoiceRepository.getAllInvoices
          updatedInvoice   = updatedInvoices.find(_.id == invoiceId).get
          _               <- ZIO.log(s"[DEBUG_LOG] Updated invoice fileName: ${updatedInvoice.fileName}")
          _               <- ZIO.log(s"[DEBUG_LOG] Updated invoice name: ${updatedInvoice.name}")

        } yield assertTrue(
          updatedId == invoiceId,
          updatedInvoice.name == "Updated Invoice",
          updatedInvoice.amount == 150,
          updatedInvoice.kind == "test_updated",
          updatedInvoice.mileage.contains(75),
          updatedInvoice.fileName.contains("updated_file.pdf")
        )
      },
      test("[DEBUG_LOG] Repository update invoice filename to None - should clear filename") {
        for {
          _ <- ZIO.log("[DEBUG_LOG] Starting repository test: Update invoice to remove filename")

          // Create initial invoice with filename
          invoiceUuid <- InvoiceRepository.createInvoice(TestData.initialInvoiceCreate)
          invoiceId    = InvoiceId(invoiceUuid)
          _           <- ZIO.log(s"[DEBUG_LOG] Created initial invoice with filename, ID: $invoiceId")

          // Update invoice to remove filename
          updateData = TestData.createInvoiceWithoutFile(invoiceId)
          updatedId <- InvoiceRepository.updateInvoice(updateData.copy(amount = 12.3))
          _         <- ZIO.log(s"[DEBUG_LOG] Updated invoice to remove filename, ID: $updatedId")

          // Verify update
          updatedInvoices <- InvoiceRepository.getAllInvoices
          updatedInvoice   = updatedInvoices.find(_.id == invoiceId).get
          _               <- ZIO.log(s"[DEBUG_LOG] Updated invoice fileName: ${updatedInvoice.fileName}")

        } yield assertTrue(
          updatedId == invoiceId,
          updatedInvoice.name == "Updated Invoice No File",
          updatedInvoice.amount == 12.3,
          updatedInvoice.fileName.isEmpty
        )
      },
      test("[DEBUG_LOG] Repository update invoice from no file to filename - should add filename") {
        for {
          _ <- ZIO.log("[DEBUG_LOG] Starting repository test: Update invoice to add filename")

          // Create initial invoice without filename
          initialCreate = TestData.initialInvoiceCreate.copy(fileName = None)
          invoiceUuid  <- InvoiceRepository.createInvoice(initialCreate)
          invoiceId     = InvoiceId(invoiceUuid)
          _            <- ZIO.log(s"[DEBUG_LOG] Created initial invoice without filename, ID: $invoiceId")

          // Update invoice to add filename
          updateData = TestData.createInvoiceForUpdate(invoiceId)
          updatedId <- InvoiceRepository.updateInvoice(updateData)
          _         <- ZIO.log(s"[DEBUG_LOG] Updated invoice to add filename, ID: $updatedId")

          // Verify update
          updatedInvoices <- InvoiceRepository.getAllInvoices
          updatedInvoice   = updatedInvoices.find(_.id == invoiceId).get
          _               <- ZIO.log(s"[DEBUG_LOG] Updated invoice fileName: ${updatedInvoice.fileName}")

        } yield assertTrue(
          updatedId == invoiceId,
          updatedInvoice.fileName.contains("updated_file.pdf")
        )
      },
      test("[DEBUG_LOG] Repository update invoice with all optional fields") {
        for {
          _ <- ZIO.log("[DEBUG_LOG] Starting repository test: Update invoice with optional fields")

          // Create initial invoice
          invoiceUuid <- InvoiceRepository.createInvoice(TestData.initialInvoiceCreate)
          invoiceId    = InvoiceId(invoiceUuid)
          _           <- ZIO.log(s"[DEBUG_LOG] Created initial invoice, ID: $invoiceId")

          // Update with different optional field values
          updateData = Invoice(
                         id = invoiceId,
                         name = "Full Update Test",
                         amount = 500,
                         date = LocalDate.now().plusDays(10),
                         driver = DriverName(TestData.testMae),
                         kind = "full_test",
                         mileage = None, // Remove mileage
                         fileName = Some("full_test.pdf")
                       )
          updatedId <- InvoiceRepository.updateInvoice(updateData)
          _         <- ZIO.log(s"[DEBUG_LOG] Updated invoice with optional fields, ID: $updatedId")

          // Verify update
          updatedInvoices <- InvoiceRepository.getAllInvoices
          updatedInvoice   = updatedInvoices.find(_.id == invoiceId).get
          _               <- ZIO.log(s"[DEBUG_LOG] Updated invoice mileage: ${updatedInvoice.mileage}")

        } yield assertTrue(
          updatedId == invoiceId,
          updatedInvoice.name == "Full Update Test",
          updatedInvoice.amount == 500,
          updatedInvoice.mileage.isEmpty,
          updatedInvoice.fileName.contains("full_test.pdf")
        )
      },
      test("[DEBUG_LOG] Repository update reimbursement") {
        for {

          // Create initial invoice
          invoiceUuid <-
            InvoiceRepository.createInvoice(TestData
              .initialInvoiceCreate.copy(toDriver = Some(DriverName("charles")), isReimbursement = true, kind = "remboursement"))
          invoiceId    = InvoiceId(invoiceUuid)
          _           <- ZIO.log(s"[DEBUG_LOG] Created initial invoice, ID: $invoiceId")

          // Update with different optional field values
          updateData = Invoice(
                         id = invoiceId,
                         name = "Full Update Test",
                         amount = 500,
                         date = LocalDate.now().plusDays(10),
                         driver = DriverName(TestData.testMae),
                         kind = "remboursement",
                         isReimbursement = true,
                         mileage = None, // Remove mileage
                         fileName = Some("full_test.pdf"),
                         toDriver = Some(DriverName("brigitte"))
                       )
          updatedId <- InvoiceRepository.updateInvoice(updateData)
          _         <- ZIO.log(s"[DEBUG_LOG] Updated invoice with optional fields, ID: $updatedId")

          // Verify update
          updatedInvoices <- InvoiceRepository.getAllInvoices
          updatedInvoice   = updatedInvoices.find(_.id == invoiceId).get
          _               <- ZIO.log(s"[DEBUG_LOG] Updated invoice mileage: ${updatedInvoice.mileage}")

        } yield assertTrue(
          updatedId == invoiceId,
          updatedInvoice.name == "Full Update Test",
          updatedInvoice.amount == 500,
          updatedInvoice.mileage.isEmpty,
          updatedInvoice.fileName.contains("full_test.pdf"),
          updatedInvoice.toDriver.contains(DriverName("brigitte")),
          updatedInvoice.isReimbursement
        )
      }
    ) @@ TestAspect.after(
      TestUtils.cleanupData.catchAll(e => ZIO.logError(s"[DEBUG_LOG] Cleanup error: ${e.getMessage}"))
    ) @@ TestAspect.before(
      TestUtils.setupTestData.catchAll(e => ZIO.logError(s"[DEBUG_LOG] Setup error: ${e.getMessage}"))
    ) @@ TestAspect.sequential).provideShared(
      // Only repository-level dependencies
      InvoiceRepositoryGel.layer,
      PersonRepositoryGel.layer,
      GelDriver.testLayer
    )
}
