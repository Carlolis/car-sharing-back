package domain.services.invoice.gel

import adapters.GelDriverLive
import domain.models.*
import domain.services.invoice.InvoiceService
import domain.services.invoice.gel.models.InvoiceGel
import zio.*

import java.util.UUID

case class InvoiceServiceGel(gelDb: GelDriverLive) extends InvoiceService {
  // TODO: Implement actual database storage
  private val invoices: List[Invoice]              = List.empty
  private val knownPersons                         =
    Set(PersonCreate("Maé"), PersonCreate("Brigitte"), PersonCreate("Charles"))

  override def createInvoice(
    invoiceCreate: InvoiceCreate
  ): Task[UUID] =
    gelDb
      .querySingle(
        classOf[UUID],
        s"""
          |  with new_invoice := (insert InvoiceGel { name := '${invoiceCreate.name}', amount := ${invoiceCreate.distance}, date := cal::to_local_date(${invoiceCreate
            .date.getYear}, ${invoiceCreate
            .date.getMonthValue}, ${invoiceCreate
            .date.getDayOfMonth}), gelDrivers := (select detached default::PersonGel filter .name in ${invoiceCreate
            .drivers.mkString("{'", "','", "'}")}) }) select new_invoice.id;
          |"""
      ).tapBoth(error => ZIO.logError(s"Created invoice with id: $error"), UUID => ZIO.logInfo(s"Created invoice with id: $UUID"))
  override def getAllInvoices: Task[List[Invoice]] =
    gelDb
      .query(
        classOf[InvoiceGel],
        s"""
          | select InvoiceGel { id, amount, date, name, gelDrivers: { name } }  ;
          |"""
      )
      .map(_.map(Invoice.fromInvoiceGel))

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
  /*
  override def getTotalStats: Task[InvoiceStats] = ZIO.succeed(InvoiceStats(List.empty, 0))



  override def updateInvoice(invoiceUpdate: Invoice): Task[UUID] =
    gelDb
      .querySingle(
        classOf[UUID],
        s"""
           | with updated_invoice := (
           |    update InvoiceGel
           |    filter .id = <uuid>'${invoiceUpdate.id}'
           |    set {
           |        name := '${invoiceUpdate.name}',
           |        distance := ${invoiceUpdate.distance},
           |        date := cal::to_local_date(${invoiceUpdate
            .date.getYear}, ${invoiceUpdate.date.getMonthValue}, ${invoiceUpdate.date.getDayOfMonth}),
           |        gelDrivers := (select detached default::PersonGel filter .name in ${invoiceUpdate.drivers.mkString("{'", "','", "'}")})
           |    }
           |)
           |select updated_invoice.id;
           |"""
      ).tapBoth(
        error => ZIO.logError(s"Failed to update invoice with id: ${invoiceUpdate.id}, error: $error"),
        uuid => ZIO.logInfo(s"Updated invoice with id: $uuid")
      )*/
}

object InvoiceServiceGel:
  val layer: ZLayer[GelDriverLive, Nothing, InvoiceService] =
    ZLayer.fromFunction(InvoiceServiceGel(_))
