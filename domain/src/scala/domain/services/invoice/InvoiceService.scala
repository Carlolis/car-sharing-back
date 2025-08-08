package domain.services.invoice

import domain.models.invoice.{Invoice, InvoiceCreate, InvoiceId}
import zio.*

import java.util.UUID

trait InvoiceService {
  def createInvoice(tripCreate: InvoiceCreate): Task[InvoiceId]
  def getAllInvoices: Task[List[Invoice]]
  def deleteInvoice(id: UUID): Task[UUID]
}

object InvoiceService:
  def createInvoice(tripCreate: InvoiceCreate): RIO[InvoiceService, InvoiceId] =
    ZIO.serviceWithZIO[InvoiceService](_.createInvoice(tripCreate))
  def getAllInvoices: RIO[InvoiceService, List[Invoice]]                       =
    ZIO.serviceWithZIO[InvoiceService](_.getAllInvoices)
  def deleteInvoice(id: UUID): RIO[InvoiceService, UUID]                       =
    ZIO.serviceWithZIO[InvoiceService](_.deleteInvoice(id))
