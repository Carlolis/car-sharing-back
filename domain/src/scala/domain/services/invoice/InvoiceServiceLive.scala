package domain.services.invoice

import domain.models.invoice.{Invoice, InvoiceCreate, InvoiceId}
import domain.services.invoice.repository.InvoiceRepository
import domain.services.invoice.storage.InvoiceStorage
import zio.*

import java.util.UUID

class InvoiceServiceLive(invoiceExternalStorage: InvoiceStorage, invoiceRepository: InvoiceRepository) extends InvoiceService {
  override def createInvoice(tripCreate: InvoiceCreate): Task[InvoiceId] = ???

  override def getAllInvoices: Task[List[Invoice]] = ???

  override def deleteInvoice(id: UUID): Task[UUID] = ???
}

object InvoiceServiceLive:
  val layer: ZLayer[InvoiceStorage & InvoiceRepository, Nothing, InvoiceServiceLive] =
    ZLayer.fromFunction(InvoiceServiceLive(_, _))
