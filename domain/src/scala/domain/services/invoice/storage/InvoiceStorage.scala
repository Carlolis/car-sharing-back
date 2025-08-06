package domain.services.invoice.storage

import domain.services.invoice.storage.models.InvoiceFile
import zio.*

import java.io.File

trait InvoiceStorage {
  def upload(localFile: File): ZIO[Any, Throwable, Unit]

  def list: ZIO[Any, Throwable, List[InvoiceFile]]

  def download(remotePath: String): ZIO[Any, Throwable, Array[Byte]]
}

object InvoiceStorage:
  def upload(localFile: File): ZIO[InvoiceStorage, Throwable, Unit]             =
    ZIO.serviceWithZIO[InvoiceStorage](_.upload(localFile))
  def list: ZIO[InvoiceStorage, Throwable, List[InvoiceFile]]                   =
    ZIO.serviceWithZIO[InvoiceStorage](_.list)
  def download(remotePath: String): ZIO[InvoiceStorage, Throwable, Array[Byte]] =
    ZIO.serviceWithZIO[InvoiceStorage](_.download(remotePath))
