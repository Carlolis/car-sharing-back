package domain.services.invoice.storage

import domain.services.invoice.storage.models.InvoiceFile
import zio.*

trait InvoiceStorage {
  def upload(invoice: Array[Byte], name: String): ZIO[Any, Throwable, Unit]

  def list: ZIO[Any, Throwable, List[InvoiceFile]]

  def download(remotePath: String): ZIO[Any, Throwable, Array[Byte]]

  def delete(invoiceName: String): ZIO[Any, Throwable, Unit]
}

object InvoiceStorage:
  def upload(invoice: Array[Byte], name: String): ZIO[InvoiceStorage, Throwable, Unit] =
    ZIO.serviceWithZIO[InvoiceStorage](_.upload(invoice, name))
  def list: ZIO[InvoiceStorage, Throwable, List[InvoiceFile]]                          =
    ZIO.serviceWithZIO[InvoiceStorage](_.list)
  def download(invoiceName: String): ZIO[InvoiceStorage, Throwable, Array[Byte]]       =
    ZIO.serviceWithZIO[InvoiceStorage](_.download(invoiceName))
  def delete(invoiceName: String): ZIO[InvoiceStorage, Throwable, Unit]                =
    ZIO.serviceWithZIO[InvoiceStorage](_.delete(invoiceName))
