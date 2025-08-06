package domain.services.invoice.storage

import domain.services.invoice.storage.models.InvoiceFile
import zio.*

import java.io.File

trait InvoiceStorage {
  def upload(invoice: File): ZIO[Any, Throwable, Unit]

  def list: ZIO[Any, Throwable, List[InvoiceFile]]

  def download(remotePath: String): ZIO[Any, Throwable, Array[Byte]]

  def delete(invoiceName: String): ZIO[Any, Throwable, Unit]
}

object InvoiceStorage:
  def upload(invoice: File): ZIO[InvoiceStorage, Throwable, Unit]                =
    ZIO.serviceWithZIO[InvoiceStorage](_.upload(invoice))
  def list: ZIO[InvoiceStorage, Throwable, List[InvoiceFile]]                    =
    ZIO.serviceWithZIO[InvoiceStorage](_.list)
  def download(invoiceName: String): ZIO[InvoiceStorage, Throwable, Array[Byte]] =
    ZIO.serviceWithZIO[InvoiceStorage](_.download(invoiceName))
  def delete(invoiceName: String): ZIO[InvoiceStorage, Throwable, Unit]          =
    ZIO.serviceWithZIO[InvoiceStorage](_.delete(invoiceName))
