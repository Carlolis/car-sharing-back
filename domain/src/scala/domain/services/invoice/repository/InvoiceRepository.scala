package domain.services.invoice.repository

import domain.services.invoice.models.{Invoice, InvoiceCreate}
import zio.*

import java.util.UUID

trait InvoiceRepository {
  def createInvoice(tripCreate: InvoiceCreate): Task[UUID]
  def getAllInvoices: Task[List[Invoice]]
  def deleteInvoice(id: UUID): Task[UUID]
}

object InvoiceRepository:
  def createInvoice(tripCreate: InvoiceCreate): RIO[InvoiceRepository, UUID] =
    ZIO.serviceWithZIO[InvoiceRepository](_.createInvoice(tripCreate))
  def getAllInvoices: RIO[InvoiceRepository, List[Invoice]]                  =
    ZIO.serviceWithZIO[InvoiceRepository](_.getAllInvoices)
  def deleteInvoice(id: UUID): RIO[InvoiceRepository, UUID]                  =
    ZIO.serviceWithZIO[InvoiceRepository](_.deleteInvoice(id))
