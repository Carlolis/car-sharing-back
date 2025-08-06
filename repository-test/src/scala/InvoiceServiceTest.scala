import adapters.GelDriver
import domain.models.PersonCreate
import domain.services.invoice.models.{Invoice, InvoiceCreate}
import domain.services.invoice.repository.InvoiceRepository
import domain.services.person.PersonService
import gel.invoice.InvoiceRepositoryGel
import gel.person.PersonRepositoryGel
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

          UUID          <- InvoiceRepository.createInvoice(invoiceCreate)
          invoiceByUser <- InvoiceRepository.getAllInvoices

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
      @@ TestAspect
        .after {

          (for {

            allInvoices <- InvoiceRepository.getAllInvoices
            _           <- ZIO
                             .foreachDiscard(allInvoices)(invoice => InvoiceRepository.deleteInvoice(invoice.id))

          } yield ()).catchAll(e => ZIO.logError(e.getMessage))

        }
      @@ TestAspect
        .before {
          val allPersons = Set(PersonCreate("Maé"), PersonCreate("Brigitte"), PersonCreate("Charles"))
          ZIO.foreachPar(allPersons)(person => PersonService.createPerson(person)).catchAll(e => ZIO.logError(e.getMessage))

        }
      @@ TestAspect.sequential).provideShared(
      InvoiceRepositoryGel.layer,
      PersonRepositoryGel.layer,
      GelDriver.testLayer
    )
}
