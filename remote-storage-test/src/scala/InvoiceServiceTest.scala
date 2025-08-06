import adapters.SardineScalaImpl
import domain.services.invoice.storage.InvoiceStorage
import webdav.invoice.InvoiceWebDavImpl
import zio.test.*
import zio.{ZIO, ZLayer}

import java.io.File

object InvoiceStorageTest extends ZIOSpecDefault {
  val testPdfFile = new File("test.pdf")
  def spec        =
    (suiteAll("InvoiceStorage Test with Webdav") {

      test("upload a test.pdf invoice") {

        for {

          _           <- InvoiceStorage.upload(testPdfFile)
          invoiceList <- InvoiceStorage.list
          uploadedFile = invoiceList.find(_.name == testPdfFile.getName)
          _           <- ZIO.logInfo(invoiceList.toString)
        } yield assertTrue(uploadedFile.isDefined)
      }

      test("Delete a test.pdf uploaded invoice") {

        for {

          _           <- InvoiceStorage.upload(testPdfFile)
          _           <- InvoiceStorage.delete(testPdfFile.getName)
          invoiceList <- InvoiceStorage.list
          uploadedFile = invoiceList.find(_.name == testPdfFile.getName)

        } yield assertTrue(uploadedFile.isEmpty)
      }

      test("Download a test.pdf uploaded invoice") {
        for {
          _           <- InvoiceStorage.upload(testPdfFile)
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
      InvoiceWebDavImpl.layer,
      SardineScalaImpl.testLayer
    )
}
