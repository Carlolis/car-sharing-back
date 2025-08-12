package gel.invoice

import adapters.GelDriverLive
import domain.models.*
import domain.models.invoice.{DriverName, Invoice, InvoiceCreate, Reimbursement}
import domain.services.invoice.repository.InvoiceRepository
import domain.services.invoice.repository.models.errors.SaveInvoiceFailed
import domain.services.person.PersonService
import gel.invoice.models.InvoiceGel
import zio.*

import java.util.UUID

case class InvoiceRepositoryGel(gelDb: GelDriverLive, personService: PersonService) extends InvoiceRepository {
  private val invoices: List[Invoice] = List.empty
  private val knownPersons            =
    Set(PersonCreate("Maé"), PersonCreate("Brigitte"), PersonCreate("Charles"))

  override def createInvoice(
    invoiceCreate: InvoiceCreate
  ): ZIO[Any, SaveInvoiceFailed, UUID] = {
    println(s"Creating invoice: ${invoiceCreate.name}, ${invoiceCreate.distance}, ${invoiceCreate.drivers}")
    gelDb
      .querySingle(
        classOf[UUID],
        s"""
          |  with new_invoice := (insert InvoiceGel { name := '${invoiceCreate.name}', amount := ${invoiceCreate.distance}, date := cal::to_local_date(${invoiceCreate
            .date.getYear}, ${invoiceCreate
            .date.getMonthValue}, ${invoiceCreate
            .date.getDayOfMonth}), gelPersons := (select detached default::PersonGel filter .name in ${invoiceCreate
            .drivers.mkString("{'", "','", "'}")}) }) select new_invoice.id;
          |"""
      ).tapBoth(error => ZIO.logError(s"Created invoice with id: $error"), UUID => ZIO.logInfo(s"Created invoice with id: $UUID")).mapError(
        SaveInvoiceFailed(_))
  }

  override def getAllInvoices: Task[List[Invoice]] =
    gelDb
      .query(
        classOf[InvoiceGel],
        s"""
          | select InvoiceGel { id, amount, date, name, gelPersons: { name } }  ;
          |"""
      )
      .map(_.map(InvoiceGel.fromInvoiceGel))

  override def deleteInvoice(id: UUID): Task[UUID] =
    gelDb
      .querySingle(
        classOf[String],
        s"""
           | delete InvoiceGel filter .id = <uuid>'$id';
           | select '$id';
           |"""
      )
      .map(id => UUID.fromString(id)).zipLeft(ZIO.logInfo(s"Deleted invoice with id: $id"))

  override def getReimbursementProposal: Task[Set[Reimbursement]] =
    for {
      allInvoices   <- getAllInvoices
      drivers       <- personService.getAll
      _             <- ZIO.logInfo(s"Got ${drivers.size} drivers")
      reimbursements = drivers.map(d => Reimbursement(DriverName(d.name), 0, Map((DriverName(d.name), 33))))
      _             <- ZIO.logInfo(s"Got $reimbursements ")
    } yield reimbursements
}

object InvoiceRepositoryGel:
  val layer: ZLayer[GelDriverLive & PersonService, Nothing, InvoiceRepository] =
    ZLayer.fromFunction(InvoiceRepositoryGel(_, _))
