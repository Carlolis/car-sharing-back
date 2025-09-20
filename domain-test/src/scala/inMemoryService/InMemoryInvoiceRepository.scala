package inMemoryService

import domain.models.invoice.{Invoice, InvoiceCreate, InvoiceId}
import domain.services.invoice.repository.InvoiceRepository
import domain.services.invoice.repository.models.errors.SaveInvoiceFailed
import zio.*

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

class InMemoryInvoiceRepository extends InvoiceRepository {
  private val invoices: mutable.Map[UUID, Invoice] = new ConcurrentHashMap[UUID, Invoice]().asScala

  override def createInvoice(invoiceCreate: InvoiceCreate): ZIO[Any, SaveInvoiceFailed, UUID] = {
    val id      = UUID.randomUUID()
    val invoice = Invoice(
      id = InvoiceId(id),
      name = invoiceCreate.name,
      amount = invoiceCreate.amount,
      date = invoiceCreate.date,
      driver = invoiceCreate.driver,
      kind = invoiceCreate.kind,
      mileage = invoiceCreate.mileage,
      fileName = invoiceCreate.fileName,
      toDriver = invoiceCreate.toDriver
    )

    ZIO
      .attempt {
        invoices.put(id, invoice)
        id
      }.mapError(ex => SaveInvoiceFailed("invoice", ex.getMessage))
  }

  override def getAllInvoices: Task[List[Invoice]] =
    ZIO.succeed(invoices.values.toList)

  // In-memory has no maintenance state; treat all invoices as without maintenance
  override def getAllInvoicesWithoutMaintenance: Task[List[Invoice]] =
    getAllInvoices

  override def deleteInvoice(id: InvoiceId): Task[InvoiceId] =
    ZIO.attempt {
      val uuid = UUID.fromString(id.toString)
      invoices.remove(uuid) match {
        case Some(_) => id
        case None    => throw new RuntimeException(s"Invoice with id $id not found")
      }
    }

  override def updateInvoice(invoiceUpdate: Invoice): Task[InvoiceId] =
    ZIO.attempt {
      val uuid = UUID.fromString(invoiceUpdate.id.toString)
      invoices.get(uuid) match {
        case Some(_) =>
          invoices.put(uuid, invoiceUpdate)
          invoiceUpdate.id
        case None    =>
          throw new RuntimeException(s"Invoice with id ${invoiceUpdate.id} not found")
      }
    }
}

object InMemoryInvoiceRepository {
  val layer: ZLayer[Any, Nothing, InMemoryInvoiceRepository] =
    ZLayer.succeed(new InMemoryInvoiceRepository)
}
