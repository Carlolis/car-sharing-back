package domain.services.invoice

import domain.models.invoice.*
import zio.*

trait InvoiceService {
  def createInvoice(tripCreate: InvoiceCreate): Task[InvoiceId]
  def getAllInvoices: Task[List[Invoice]]
  def getAllInvoicesWithoutMaintenance: Task[List[Invoice]]
  def deleteInvoice(id: InvoiceId): Task[InvoiceId]
  def updateInvoice(invoiceUpdate: InvoiceUpdate): Task[InvoiceId]
  def download(fileName: String, id: InvoiceId): ZIO[Any, Throwable, Array[Byte]]
  def getReimbursementProposals: Task[Set[Reimbursement]]
}

object InvoiceService:
  def createInvoice(tripCreate: InvoiceCreate): RIO[InvoiceService, InvoiceId]    =
    ZIO.serviceWithZIO[InvoiceService](_.createInvoice(tripCreate))
  def getAllInvoices: RIO[InvoiceService, List[Invoice]]                          =
    ZIO.serviceWithZIO[InvoiceService](_.getAllInvoices)
  def getAllInvoicesWithoutMaintenance: RIO[InvoiceService, List[Invoice]]        =
    ZIO.serviceWithZIO[InvoiceService](_.getAllInvoicesWithoutMaintenance)
  def deleteInvoice(id: InvoiceId): RIO[InvoiceService, InvoiceId]                =
    ZIO.serviceWithZIO[InvoiceService](_.deleteInvoice(id))
  def updateInvoice(invoiceUpdate: InvoiceUpdate): RIO[InvoiceService, InvoiceId] =
    ZIO.serviceWithZIO[InvoiceService](_.updateInvoice(invoiceUpdate))
  def download(fileName: String, id: InvoiceId): RIO[InvoiceService, Array[Byte]] =
    ZIO.serviceWithZIO[InvoiceService](_.download(fileName, id))
  def getReimbursementProposal: RIO[InvoiceService, Set[Reimbursement]]           =
    ZIO.serviceWithZIO[InvoiceService](_.getReimbursementProposals)
