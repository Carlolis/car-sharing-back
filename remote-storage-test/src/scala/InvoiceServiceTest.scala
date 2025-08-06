import adapters.SardineScalaImpl
import domain.services.invoice.storage.InvoiceStorage
import webdav.invoice.InvoiceWebDavImpl
import zio.test.*
import zio.test.Assertion.*
import zio.{ZIO, ZLayer}

import java.io.File

object InvoiceStorageTest extends ZIOSpecDefault {
  def spec =
    (suiteAll("InvoiceStorage Test with Webdav") {
      val testPdfFile = new File("test.pdf")

      test("upload a test.pdf invoice at repository root") {

        for {
          invoiceList <- InvoiceStorage.list
          _           <- ZIO.logInfo(invoiceList.toString)
          _           <- InvoiceStorage.upload(testPdfFile)
          invoiceList <- InvoiceStorage.list
          uploadedFile = invoiceList.find(_.name == "test.pdf")
          _           <- ZIO.logInfo(invoiceList.toString)
        } yield assertTrue(invoiceList.length == 1)
      }
    }
    /*      @@ TestAspect
        .after {

          (for {

            allInvoices <- InvoiceStorage.getAllInvoices
            _           <- ZIO
                             .foreachDiscard(allInvoices)(invoice => InvoiceStorage.deleteInvoice(invoice.id))

          } yield ()).catchAll(e => ZIO.logError(e.getMessage))

        }
      @@ TestAspect
        .before {
          val allPersons = Set(PersonCreate("Maé"), PersonCreate("Brigitte"), PersonCreate("Charles"))
          ZIO.foreachPar(allPersons)(person => PersonService.createPerson(person)).catchAll(e => ZIO.logError(e.getMessage))

        }*/
      @@ TestAspect.sequential).provideShared(
      InvoiceWebDavImpl.layer,
      SardineScalaImpl.testLayer
    )
}
