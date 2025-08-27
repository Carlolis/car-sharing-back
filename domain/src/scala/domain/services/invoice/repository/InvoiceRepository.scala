package domain.services.invoice.repository

import domain.models.invoice.{Invoice, InvoiceCreate, InvoiceId, Reimbursement}
import domain.services.invoice.repository.models.errors.SaveInvoiceFailed
import zio.*

import java.util.UUID

trait InvoiceRepository {
  def createInvoice(tripCreate: InvoiceCreate): ZIO[Any, SaveInvoiceFailed, UUID]
  def getAllInvoices: Task[List[Invoice]]
  def deleteInvoice(id: InvoiceId): Task[InvoiceId]
  def getReimbursementProposal: Task[Set[Reimbursement]]
  def updateInvoice(invoiceUpdate: Invoice): Task[InvoiceId]
}

object InvoiceRepository:
  def createInvoice(tripCreate: InvoiceCreate): RIO[InvoiceRepository, UUID] =
    ZIO.serviceWithZIO[InvoiceRepository](_.createInvoice(tripCreate))
  def getAllInvoices: RIO[InvoiceRepository, List[Invoice]]                  =
    ZIO.serviceWithZIO[InvoiceRepository](_.getAllInvoices)
  def deleteInvoice(id: InvoiceId): RIO[InvoiceRepository, InvoiceId]        =
    ZIO.serviceWithZIO[InvoiceRepository](_.deleteInvoice(id))
  def getReimbursementProposal: RIO[InvoiceRepository, Set[Reimbursement]]   =
    ZIO.serviceWithZIO[InvoiceRepository](_.getReimbursementProposal)
  def updateInvoice(trip: Invoice): RIO[InvoiceRepository, InvoiceId]        =
    ZIO.serviceWithZIO[InvoiceRepository](_.updateInvoice(trip))
