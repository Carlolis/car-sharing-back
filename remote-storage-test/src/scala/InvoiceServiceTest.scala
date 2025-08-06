import domain.models.PersonCreate
import domain.services.invoice.models.{Invoice, InvoiceCreate}
import domain.services.invoice.storage.InvoiceStorage
import domain.services.person.PersonService
import webdav.invoice.InvoiceWebDavImpl
import zio.test.*
import zio.test.Assertion.*
import zio.{ZIO, ZLayer}

import java.io.File
import java.time.LocalDate

object InvoiceStorageTest extends ZIOSpecDefault {
  val personName    = "Maé"
  val maé           = PersonCreate(personName)
  val invoiceCreate =
    InvoiceCreate(100, LocalDate.now(), "Business", Set(personName))

  def spec =
    (suiteAll("InvoiceStorage Test with Webdav") {
      val defaultPath = "http://localhost:8080/webdav/invoices/"
      val localPdf    = File.createTempFile("test", ".pdf")
      test("upload a pdf invoice") {

        for {

          _           <- InvoiceStorage.upload(localPdf, defaultPath)
          invoiceList <- InvoiceStorage.list(defaultPath)

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
      InvoiceWebDavImpl.layer
    )
}
