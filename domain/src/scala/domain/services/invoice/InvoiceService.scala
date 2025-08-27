package domain.services.invoice

import domain.models.invoice.{Invoice, InvoiceCreate, InvoiceId}
import zio.*

trait InvoiceService {
  def createInvoice(tripCreate: InvoiceCreate): Task[InvoiceId]
  def getAllInvoices: Task[List[Invoice]]
  def deleteInvoice(id: InvoiceId): Task[InvoiceId]
  def updateInvoice(invoiceUpdate: Invoice): Task[InvoiceId]
  def download(fileName: String, id: InvoiceId): ZIO[Any, Throwable, Array[Byte]]
}

object InvoiceService:
  def createInvoice(tripCreate: InvoiceCreate): RIO[InvoiceService, InvoiceId]    =
    ZIO.serviceWithZIO[InvoiceService](_.createInvoice(tripCreate))
  def getAllInvoices: RIO[InvoiceService, List[Invoice]]                          =
    ZIO.serviceWithZIO[InvoiceService](_.getAllInvoices)
  def deleteInvoice(id: InvoiceId): RIO[InvoiceService, InvoiceId]                =
    ZIO.serviceWithZIO[InvoiceService](_.deleteInvoice(id))
  def updateInvoice(trip: Invoice): RIO[InvoiceService, InvoiceId]                =
    ZIO.serviceWithZIO[InvoiceService](_.updateInvoice(trip))
  def download(fileName: String, id: InvoiceId): RIO[InvoiceService, Array[Byte]] =
    ZIO.serviceWithZIO[InvoiceService](_.download(fileName, id))
