import adapters.SardineScalaImpl
import config.AppConfig
import domain.services.invoice.storage.InvoiceStorage
import webdav.invoice.InvoiceWebDavImpl
import zio.test.*
import zio.{Scope, ZIO, ZLayer}

import java.io.File
import java.nio.file.Files

object InvoiceStorageTest extends ZIOSpecDefault {
  // Use the test layer from AppConfig
  // This provides a consistent test configuration across all tests
  val testPdfFile                              = new File("test.pdf")
  var fileContent: Array[Byte]                 = Files.readAllBytes(testPdfFile.toPath)
  def spec: Spec[TestEnvironment & Scope, Any] =
    (suiteAll("InvoiceStorage Test with Webdav") {

      test("upload a test.pdf invoice") {

        for {

          _           <- InvoiceStorage.upload(fileContent, testPdfFile.getName)
          invoiceList <- InvoiceStorage.list
          uploadedFile = invoiceList.find(_.name == testPdfFile.getName)
          _           <- ZIO.logInfo(invoiceList.toString)
        } yield assertTrue(uploadedFile.isDefined)
      }

      test("Delete a test.pdf uploaded invoice") {

        for {

          _           <- InvoiceStorage.upload(fileContent, testPdfFile.getName)
          _           <- InvoiceStorage.delete(testPdfFile.getName)
          invoiceList <- InvoiceStorage.list
          uploadedFile = invoiceList.find(_.name == testPdfFile.getName)

        } yield assertTrue(uploadedFile.isEmpty)
      }

      test("Download a test.pdf uploaded invoice") {
        for {
          _           <- InvoiceStorage.upload(fileContent, testPdfFile.getName)
          invoiceList <- InvoiceStorage.list
          uploadedFile = invoiceList.find(_.name == testPdfFile.getName)
          invoice     <- InvoiceStorage.download(uploadedFile.get.name)
        } yield assertTrue(invoice.length > 0)
      }
    }
      @@ TestAspect
        .after {

          (for {

            allInvoices <- InvoiceStorage.delete(testPdfFile.getName)

          } yield ()).catchAll(e => ZIO.logError(e.getMessage))

        }

      @@ TestAspect.sequential).provideShared(
      AppConfig.layer,
      InvoiceWebDavImpl.layer,
      SardineScalaImpl.layer
    )
}
