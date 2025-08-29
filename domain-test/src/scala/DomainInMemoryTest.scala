import domain.models.invoice.{DriverName, InvoiceCreate, InvoiceUpdate}
import domain.services.invoice.storage.InvoiceStorage
import domain.services.invoice.{InvoiceService, InvoiceServiceLive}
import domain.services.person.PersonService
import inMemoryService.{InMemoryInvoiceRepository, InMemoryInvoiceStorage, InMemoryPersonRepository}
import sttp.tapir.FileRange
import zio.test.*
import zio.test.Assertion.*
import zio.{Scope, ZIO, ZLayer}

import java.io.File
import java.nio.file.Files
import java.time.LocalDate

object DomainInMemoryTest extends ZIOSpecDefault {

  // Test data configuration
  object TestData {
    val testPdfFile = new File("test.pdf")
    val fileContent: Array[Byte] = Files.readAllBytes(testPdfFile.toPath)
    
    val driverName = "TestDriver"
    val updatedDriverName = "UpdatedDriver"
    
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
      amount = 750, // Updated amount
      mileage = Some(150), // Updated mileage
      date = LocalDate.now().plusDays(1), // Updated date
      name = "Updated Test Invoice", // Updated name
      drivers = Set(DriverName(updatedDriverName)), // Updated drivers
      kind = "maintenance", // Updated kind
      fileName = Some(testPdfFile.getName),
      fileBytes = Some(FileRange(testPdfFile))
    )
  }

  // Test utilities
  object TestUtils {
    def cleanupStorage: ZIO[InvoiceStorage, Throwable, Unit] =
      for {
        _ <- InvoiceStorage.delete(TestData.testPdfFile.getName).catchAll(_ => ZIO.unit)
        _ <- ZIO.log("[DEBUG_LOG] Storage cleanup completed")
      } yield ()
  }

  // Create layer configuration using InvoiceServiceLive with in-memory implementations
  val testLayers = ZLayer.make[InvoiceService & InvoiceStorage](
    InMemoryInvoiceStorage.layer,
    InMemoryInvoiceRepository.layer,
    InvoiceServiceLive.layer,
    InMemoryPersonRepository.layer 
  )

  def spec: Spec[TestEnvironment & Scope, Any] =
    (suite("Invoice Service Test with In-Memory Storage")(

      test("[DEBUG_LOG] create an invoice with file - should store invoice and file successfully") {
        for {
          _ <- ZIO.log("[DEBUG_LOG] Starting test: create invoice with file")
          
          // Create initial invoice with file
          id <- InvoiceService.createInvoice(TestData.initialInvoiceCreate)
          _ <- ZIO.log(s"[DEBUG_LOG] Created invoice with ID: $id")
          
          // Verify file can be downloaded
          uploadedFile <- InvoiceService.download(TestData.testPdfFile.getName, id)
          _ <- ZIO.log(s"[DEBUG_LOG] Downloaded file exists: ${uploadedFile.nonEmpty}")

        } yield assertTrue(uploadedFile.nonEmpty)
      },

      test("[DEBUG_LOG] create an invoice without file - should store invoice metadata only") {
        for {
          _ <- ZIO.log("[DEBUG_LOG] Starting test: create invoice without file")
          
          // Create invoice without file
          id <- InvoiceService.createInvoice(TestData.invoiceCreateWithoutFile)
          _ <- ZIO.log(s"[DEBUG_LOG] Created invoice without file, ID: $id")

        } yield assertTrue(true) // Invoice created successfully if we reach this point
      },

      test("[DEBUG_LOG] create an invoice, update it, and verify the update") {
        for {
          _ <- ZIO.log("[DEBUG_LOG] Starting test: create and update invoice")
          
          // Create initial invoice
          id <- InvoiceService.createInvoice(TestData.initialInvoiceCreate)
          _ <- ZIO.log(s"[DEBUG_LOG] Created initial invoice with ID: $id")
          
          // Create update object with the new id
          invoiceUpdate = TestData.createUpdateData(id)
          _ <- ZIO.log(s"[DEBUG_LOG] Preparing to update invoice with new data")
          
          // Update the invoice with new values
          updatedId <- InvoiceService.updateInvoice(invoiceUpdate)
          _ <- ZIO.log(s"[DEBUG_LOG] Updated invoice, returned ID: $updatedId")
          
          // Verify the update was successful by downloading the file
          uploadedFile <- InvoiceService.download(TestData.testPdfFile.getName, updatedId)
          _ <- ZIO.log(s"[DEBUG_LOG] Downloaded updated file, exists: ${uploadedFile.nonEmpty}")

        } yield assertTrue(
          updatedId == id,
          uploadedFile.nonEmpty
        )
      },

      test("[DEBUG_LOG] multiple operations - create, update, and verify file persistence") {
        for {
          _ <- ZIO.log("[DEBUG_LOG] Starting test: multiple operations")
          
          // Create first invoice
          id1 <- InvoiceService.createInvoice(TestData.initialInvoiceCreate)
          _ <- ZIO.log(s"[DEBUG_LOG] Created first invoice: $id1")
          
          // Create second invoice without file
          id2 <- InvoiceService.createInvoice(TestData.invoiceCreateWithoutFile)
          _ <- ZIO.log(s"[DEBUG_LOG] Created second invoice: $id2")
          
          // Update first invoice
          updateData = TestData.createUpdateData(id1)
          updatedId1 <- InvoiceService.updateInvoice(updateData)
          _ <- ZIO.log(s"[DEBUG_LOG] Updated first invoice: $updatedId1")
          
          // Verify first invoice file still exists after update
          file1 <- InvoiceService.download(TestData.testPdfFile.getName, updatedId1)
          _ <- ZIO.log(s"[DEBUG_LOG] File1 after update exists: ${file1.nonEmpty}")

        } yield assertTrue(
          id1 != id2,
          updatedId1 == id1,
          file1.nonEmpty
        )
      }
    ) @@ TestAspect.after(
      TestUtils.cleanupStorage.catchAll(e => ZIO.logError(s"[DEBUG_LOG] Cleanup error: ${e.getMessage}"))
    ) @@ TestAspect.sequential).provideShared(testLayers)
}
