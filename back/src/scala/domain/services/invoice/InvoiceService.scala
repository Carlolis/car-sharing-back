package domain.services.invoice

import domain.models.{Invoice, InvoiceCreate}
import zio.*

import java.util.UUID

trait InvoiceService {
  def createInvoice(tripCreate: InvoiceCreate): Task[UUID]
  def getAllInvoices: Task[List[Invoice]]
  def deleteInvoice(id: UUID): Task[UUID]
  // def getTotalStats: Task[InvoiceStats]

  // def updateInvoice(tripUpdate: Invoice): Task[UUID]
}

object InvoiceService:
  def createInvoice(tripCreate: InvoiceCreate): RIO[InvoiceService, UUID] =
    ZIO.serviceWithZIO[InvoiceService](_.createInvoice(tripCreate))
  def getAllInvoices: RIO[InvoiceService, List[Invoice]]                  =
    ZIO.serviceWithZIO[InvoiceService](_.getAllInvoices)
  def deleteInvoice(id: UUID): RIO[InvoiceService, UUID]                  =
    ZIO.serviceWithZIO[InvoiceService](_.deleteInvoice(id))
/*
def getTotalStats: RIO[InvoiceService, InvoiceStats] =
  ZIO.serviceWithZIO[InvoiceService](_.getTotalStats)



def updateInvoice(trip: Invoice): RIO[InvoiceService, UUID] =
  ZIO.serviceWithZIO[InvoiceService](_.updateInvoice(trip))*/
