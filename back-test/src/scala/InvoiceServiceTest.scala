import adapters.GelDriver
import domain.models.{Invoice, InvoiceCreate, PersonCreate}
import domain.services.invoice.InvoiceService
import domain.services.invoice.gel.InvoiceServiceGel
import domain.services.person.PersonService
import domain.services.person.gel.PersonServiceGel
import zio.test.*
import zio.test.Assertion.*
import zio.{ZIO, ZLayer}

import java.time.LocalDate

object InvoiceServiceTest extends ZIOSpecDefault {
  val personName    = "Maé"
  val maé           = PersonCreate(personName)
  val invoiceCreate =
    InvoiceCreate(100, LocalDate.now(), "Business", Set(personName))

  def spec =
    (suiteAll("InvoiceServiceTest in Gel") {

      test("Maé createInvoice should create a invoice successfully with Maé") {

        for {

          UUID          <- InvoiceService.createInvoice(invoiceCreate)
          invoiceByUser <- InvoiceService.getAllInvoices

        } yield assertTrue(UUID != null, invoiceByUser.length == 1)
      }
      /*test("Charles createInvoice should create a invoice successfully with Charles") {
        val personName = "Charles"

        for {

          UUID       <- InvoiceService.createInvoice(invoiceCreate.copy(drivers = Set(personName)))
          invoiceByUser <- InvoiceService.getAllInvoices

        } yield assertTrue(UUID != null, invoiceByUser.invoices.length == 1)
      }
      test("deleteInvoice should delete a invoice successfully with Maé") {

        for {

          UUID       <- InvoiceService.createInvoice(invoiceCreate)
          _          <- InvoiceService.deleteInvoice(UUID)
          invoiceByUser <- InvoiceService.getAllInvoices

        } yield assertTrue(UUID != null, invoiceByUser.invoices.isEmpty)
      }

      test("updateInvoice should update a invoice successfully with Maé") {
        val updatedInvoiceName = "Updated Business Invoice"
        val updatedDistance = 200

        for {
          uuid       <- InvoiceService.createInvoice(invoiceCreate)
          updatedInvoice = Invoice(uuid, updatedDistance, LocalDate.now(), updatedInvoiceName, Set(personName))
          _          <- InvoiceService.updateInvoice(updatedInvoice)
          invoiceByUser <- InvoiceService.getAllInvoices
        } yield assertTrue(
          invoiceByUser.invoices.exists(invoice => invoice.id == uuid && invoice.name == updatedInvoiceName && invoice.distance == updatedDistance),
          invoiceByUser.invoices.length == 1)
      }*/
    }
    /* @@ TestAspect
        .after {

          (for {

            allInvoices <- InvoiceService.getAllInvoices.map(_.invoices)
            _           <- ZIO
                             .foreachDiscard(allInvoices)(invoice => InvoiceService.deleteInvoice(invoice.id))

          } yield ()).catchAll(e => ZIO.logError(e.getMessage))

        }*/
      @@ TestAspect
        .before {
          val allPersons = Set(PersonCreate("Maé"), PersonCreate("Brigitte"), PersonCreate("Charles"))
          ZIO.foreachPar(allPersons)(person => PersonService.createPerson(person)).catchAll(e => ZIO.logError(e.getMessage))

        }
      @@ TestAspect.sequential).provideShared(
      InvoiceServiceGel.layer,
      PersonServiceGel.layer,
      GelDriver.testLayer
    )
}
