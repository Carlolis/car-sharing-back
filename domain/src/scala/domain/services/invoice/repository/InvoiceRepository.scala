package domain.services.invoice.repository

import domain.models.invoice.{Invoice, InvoiceCreate, Reimbursement}
import domain.services.invoice.repository.models.errors.SaveInvoiceFailed
import zio.*

import java.util.UUID

trait InvoiceRepository {
  def createInvoice(tripCreate: InvoiceCreate): ZIO[Any, SaveInvoiceFailed, UUID]
  def getAllInvoices: Task[List[Invoice]]
  def deleteInvoice(id: UUID): Task[UUID]
  def getReimbursementProposal: Task[Set[Reimbursement]]
}

object InvoiceRepository:
  def createInvoice(tripCreate: InvoiceCreate): RIO[InvoiceRepository, UUID] =
    ZIO.serviceWithZIO[InvoiceRepository](_.createInvoice(tripCreate))
  def getAllInvoices: RIO[InvoiceRepository, List[Invoice]]                  =
    ZIO.serviceWithZIO[InvoiceRepository](_.getAllInvoices)
  def deleteInvoice(id: UUID): RIO[InvoiceRepository, UUID]                  =
    ZIO.serviceWithZIO[InvoiceRepository](_.deleteInvoice(id))
  def getReimbursementProposal: RIO[InvoiceRepository, Set[Reimbursement]]   =
    ZIO.serviceWithZIO[InvoiceRepository](_.getReimbursementProposal)
