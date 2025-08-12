import adapters.GelDriver
import domain.models.PersonCreate
import domain.models.invoice.{DriverName, Invoice, InvoiceCreate}
import domain.services.invoice.repository.InvoiceRepository
import domain.services.person.PersonService
import gel.invoice.InvoiceRepositoryGel
import gel.person.PersonRepositoryGel
import zio.test.*
import zio.test.Assertion.*
import zio.{Scope, ZIO, ZLayer}

import java.time.LocalDate

object InvoiceRepositoryTest extends ZIOSpecDefault {
  val personName    = "maé"
  val mae           = PersonCreate(personName)
  val invoiceCreate =
    InvoiceCreate(99, LocalDate.now(), "Business", Set(DriverName(personName)))

  def spec: Spec[TestEnvironment & Scope, Any] =
    (suiteAll("InvoiceServiceTest in Gel") {

      test("Maé createInvoice should create a invoice successfully with Maé") {

        for {

          UUID          <- InvoiceRepository.createInvoice(invoiceCreate)
          invoiceByUser <- InvoiceRepository.getAllInvoices

        } yield assertTrue(UUID != null, invoiceByUser.length == 1)
      }

      test("Maé has one invoice for 99€, Charles has to give 33 to Maé, Brigitte 33 to Maé and Maé 0 to anyone else") {

        for {

          UUID                  <- InvoiceRepository.createInvoice(invoiceCreate)
          reimbursements        <- InvoiceRepository.getReimbursementProposal
          maéReimbursement      <- ZIO.fromOption(reimbursements.find(_.driverName == DriverName(personName)))
          charlesReimbursement  <- ZIO.fromOption(reimbursements.find(_.driverName == DriverName("charles")))
          brigitteReimbursement <- ZIO.fromOption(reimbursements.find(_.driverName == DriverName("brigitte")))

        } yield assertTrue(
          reimbursements.size == 3,
          maéReimbursement.totalAmount == 0,
          charlesReimbursement.totalAmount == 33,
          brigitteReimbursement.totalAmount == 33)
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
            allPersons  <- PersonService.getAll
            _           <- ZIO.foreachDiscard(allPersons)(person => PersonService.deletePerson(person.id))

          } yield ()).catchAll(e => ZIO.logError(e.getMessage))

        }
      @@ TestAspect
        .before {
          val allPersons = Set(PersonCreate("maé"), PersonCreate("brigitte"), PersonCreate("charles"))
          ZIO.foreachPar(allPersons)(person => PersonService.createPerson(person)).catchAll(e => ZIO.logError(e.getMessage))

        }
      @@ TestAspect.sequential).provideShared(
      InvoiceRepositoryGel.layer,
      PersonRepositoryGel.layer,
      GelDriver.testLayer
    )
}
