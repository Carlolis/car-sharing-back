package gel.invoice

import adapters.GelDriverLive
import domain.models.*
import domain.models.invoice.*
import domain.services.invoice.repository.InvoiceRepository
import domain.services.invoice.repository.models.errors.SaveInvoiceFailed
import gel.invoice.models.InvoiceGel
import zio.*

import java.util.UUID

case class InvoiceRepositoryGel(gelDb: GelDriverLive) extends InvoiceRepository {
  private val invoices: List[Invoice] = List.empty
  private val knownPersons            =
    Set(PersonCreate("maé"), PersonCreate("brigitte"), PersonCreate("charles"))

  override def createInvoice(
    invoiceCreate: InvoiceCreate
  ): ZIO[Any, SaveInvoiceFailed, UUID] = {
    println(s"Creating invoice with name: ${invoiceCreate.toDriver}")
    gelDb
      .querySingle(
        classOf[UUID],
        s"""
           |  with new_invoice := (insert InvoiceGel { name := '${invoiceCreate.name}',
           |   amount := ${invoiceCreate.amount},
           |   kind := '${invoiceCreate.kind}',
           |   ${invoiceCreate.mileage.map(mileage => s"mileage := $mileage,").getOrElse("")}
           |   ${invoiceCreate.fileName.map(fileName => s"fileName := '$fileName',").getOrElse("")}
           |   isReimbursement := ${invoiceCreate.isReimbursement},
           |   date := cal::to_local_date(${invoiceCreate.date.getYear},
           |${invoiceCreate.date.getMonthValue},
           |${invoiceCreate.date.getDayOfMonth}),
           |${invoiceCreate
            .toDriver.map(toDriver =>
              s"toDriver := (select detached default::PersonGel filter .name = '${DriverName.unwrap(toDriver)}' limit 1),").getOrElse("")}
           | gelPerson := (select detached default::PersonGel
           | filter .name = '${DriverName.unwrap(invoiceCreate.driver)}' limit 1) }) select new_invoice.id;
           |"""
      ).tapBoth(error => ZIO.logError(s"Created invoice with id: $error"), UUID => ZIO.logInfo(s"Created invoice with id: $UUID")).mapError(
        SaveInvoiceFailed(_))
  }

  override def getAllInvoices: Task[List[Invoice]] =
    gelDb
      .query(
        classOf[InvoiceGel],
        s"""
           | select InvoiceGel { id, amount, date, name,  gelPerson: { name }, kind, mileage, fileName, isReimbursement, toDriver: { name } };
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
           |        ${invoiceUpdate.mileage.map(mileage => s"mileage := $mileage").getOrElse("mileage := <int16>{}")},
           |        ${invoiceUpdate.fileName.map(fileName => s"fileName := '$fileName'").getOrElse("fileName := <str>{}")},
           |        isReimbursement := ${invoiceUpdate.isReimbursement},
           |        date := cal::to_local_date(${invoiceUpdate
            .date.getYear}, ${invoiceUpdate
            .date.getMonthValue}, ${invoiceUpdate.date.getDayOfMonth}),
           |${invoiceUpdate
            .toDriver.map(toDriver =>
              s"toDriver := (select detached default::PersonGel filter .name = '${DriverName.unwrap(toDriver)}' limit 1),").getOrElse("")}
           |        gelPerson := (select detached default::PersonGel filter .name = '${DriverName.unwrap(invoiceUpdate.driver)}' limit 1)
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
  val layer: ZLayer[GelDriverLive, Nothing, InvoiceRepository] =
    ZLayer.fromFunction(InvoiceRepositoryGel(_))
