package gel.invoice

import adapters.GelDriverLive
import domain.models.*
import domain.models.invoice.*
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
  ): ZIO[Any, SaveInvoiceFailed, UUID] =
    gelDb
      .querySingle(
        classOf[UUID],
        s"""
          |  with new_invoice := (insert InvoiceGel { name := '${invoiceCreate.name}',
          |   amount := ${invoiceCreate.amount},
          |   kind := '${invoiceCreate.kind}',
          |   ${invoiceCreate.mileage.map(mileage => s"mileage := $mileage,").getOrElse("")}
          |   date := cal::to_local_date(${invoiceCreate.date.getYear},
          |${invoiceCreate.date.getMonthValue},
          |${invoiceCreate.date.getDayOfMonth}),
          | gelPersons := (select detached default::PersonGel
          | filter .name in ${invoiceCreate.drivers.mkString("{'", "','", "'}")}) }) select new_invoice.id;
          |"""
      ).tapBoth(error => ZIO.logError(s"Created invoice with id: $error"), UUID => ZIO.logInfo(s"Created invoice with id: $UUID")).mapError(
        SaveInvoiceFailed(_))

  override def getAllInvoices: Task[List[Invoice]] =
    gelDb
      .query(
        classOf[InvoiceGel],
        s"""
          | select InvoiceGel { id, amount, date, name,  gelPersons: { name }, kind, mileage };
          |"""
      )
      .map(invoice => invoice.map(InvoiceGel.fromInvoiceGel))

  override def deleteInvoice(id: InvoiceId): Task[InvoiceId] =
    gelDb
      .querySingle(
        classOf[String],
        s"""
           | delete InvoiceGel filter .id = <uuid>'${id.toString}';
           | select '${id.toString}';
           |"""
      )
      .map(id => InvoiceId(UUID.fromString(id))).zipLeft(ZIO.logInfo(s"Deleted invoice with id: $id"))

  override def getReimbursementProposal: Task[Set[Reimbursement]] =
    for {
      allInvoices   <- getAllInvoices
      drivers       <- personService.getAll
      _             <- ZIO.logInfo(s"Got ${drivers.size} drivers")
      totalAmount    = allInvoices.foldLeft(0)((total, invoice) => invoice.amount + total)
      driversAmount  =
        drivers.map(d =>
          (
            d.name,
            allInvoices.foldLeft(0)((total, invoice) => if (invoice.drivers.head.toString == d.name) invoice.amount + total else total)))
      reimbursements = driversAmount.map { (driverName, total) =>

                         val othersDriverMapReimbursement: Map[DriverName, Int] =
                           driversAmount
                             .filter(_._1 != driverName)
                             .foldLeft(Map.empty[DriverName, Int]) {
                               case (acc, (name, amount)) =>
                                 if (amount <= total) acc + (DriverName(name) -> 0)
                                 else acc + (DriverName(name)                 -> amount / drivers.size)
                             }
                         var totalToReimburse                                   = othersDriverMapReimbursement.values.sum

                         Reimbursement(DriverName(driverName), totalToReimburse, othersDriverMapReimbursement)
                       }
      _ <- ZIO.logInfo(s"Got $reimbursements ")
    } yield reimbursements

  override def updateInvoice(invoiceUpdate: Invoice): Task[InvoiceId] =
    gelDb
      .querySingle(
        classOf[UUID],
        s"""
           | with updated_invoice := (
           |    update InvoiceGel
           |    filter .id = <uuid>'${invoiceUpdate.id}'
           |    set {
           |        name := '${invoiceUpdate.name}',
           |        amount := ${invoiceUpdate.amount},
           |        kind := '${invoiceUpdate.kind}',
           |        ${invoiceUpdate.mileage.map(mileage => s"mileage := $mileage,").getOrElse("")}
           |        date := cal::to_local_date(${invoiceUpdate
            .date.getYear}, ${invoiceUpdate
            .date.getMonthValue}, ${invoiceUpdate.date.getDayOfMonth}),
           |        gelPersons := (select detached default::PersonGel filter .name in ${invoiceUpdate.drivers.mkString("{'", "','", "'}")})
           |    }
           |)
           |select updated_invoice.id;
           |"""
      ).map(InvoiceId(_)).tapBoth(
        error => ZIO.logError(s"Failed to update invoice with id: ${invoiceUpdate.id}, error: $error"),
        uuid => ZIO.logInfo(s"Updated invoice with id: $uuid")
      )
}

object InvoiceRepositoryGel:
  val layer: ZLayer[GelDriverLive & PersonService, Nothing, InvoiceRepository] =
    ZLayer.fromFunction(InvoiceRepositoryGel(_, _))
