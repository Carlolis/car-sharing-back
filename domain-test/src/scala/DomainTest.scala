import config.AppConfig
import domain.models.invoice.{InvoiceCreate, DriverName}
import domain.services.invoice.{InvoiceService, InvoiceServiceLive}
import domain.services.invoice.storage.InvoiceStorage
import inMemoryService.{InMemoryInvoiceRepository, InMemoryInvoiceStorage}
import sttp.tapir.FileRange
import zio.test.*
import zio.{Scope, ZIO, ZLayer}

import java.io.File
import java.nio.file.Files
import java.time.LocalDate

object DomainTest extends ZIOSpecDefault {
  val testPdfFile = new File("test.pdf")
  val fileContent: Array[Byte] = Files.readAllBytes(testPdfFile.toPath)
  
  // Create layer configuration using InvoiceServiceLive with in-memory implementations
  val testLayers = ZLayer.make[InvoiceService & InvoiceStorage](
    InMemoryInvoiceStorage.layer,
    InMemoryInvoiceRepository.layer,
    InvoiceServiceLive.layer
  )
  
  def spec: Spec[TestEnvironment & Scope, Any] =
    (suite("Invoice Service Test with In-Memory Storage") {
      val invoiceCreate = InvoiceCreate(
        amount = 500,
        mileage = Some(100),
        date = LocalDate.now(),
        name = "Test Invoice",
        drivers = Set(DriverName("TestDriver")),
        kind = "fuel",
        fileBytes = Some(FileRange(testPdfFile)),
        fileName = Some(testPdfFile.getName)
      )
      
      test("create an invoice and get it, should have the invoice in storage") {
        for {
          _           <- InvoiceService.createInvoice(invoiceCreate)
          invoiceList <- InvoiceStorage.list
          uploadedFile = invoiceList.find(_.name.contains(testPdfFile.getName))
          _           <- ZIO.logInfo(s"[DEBUG_LOG] Invoice list: ${invoiceList.toString}")
          _           <- ZIO.logInfo(s"[DEBUG_LOG] Looking for file containing: ${testPdfFile.getName}")
          _           <- ZIO.logInfo(s"[DEBUG_LOG] Found file: ${uploadedFile.isDefined}")
        } yield assertTrue(uploadedFile.isDefined)
      }
    } @@ TestAspect.after {
      InvoiceStorage.delete(testPdfFile.getName).orElse(ZIO.unit)
    } @@ TestAspect.sequential).provideShared(testLayers)
}
