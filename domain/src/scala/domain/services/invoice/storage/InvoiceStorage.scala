package domain.services.invoice.storage

import domain.services.invoice.storage.models.InvoiceFile
import zio.*

import java.io.File

trait InvoiceStorage {
  def upload(localFile: File, remoteDir: String): ZIO[Any, Throwable, Unit]

  def list(remoteDir: String): ZIO[Any, Throwable, List[InvoiceFile]]

  def download(remotePath: String): ZIO[Any, Throwable, Array[Byte]]
}

object InvoiceStorage:
  def upload(localFile: File, remoteDir: String): ZIO[InvoiceStorage, Throwable, Unit] =
    ZIO.serviceWithZIO[InvoiceStorage](_.upload(localFile, remoteDir))
  def list(remoteDir: String): ZIO[InvoiceStorage, Throwable, List[InvoiceFile]]       =
    ZIO.serviceWithZIO[InvoiceStorage](_.list(remoteDir))
  def download(remotePath: String): ZIO[InvoiceStorage, Throwable, Array[Byte]]        =
    ZIO.serviceWithZIO[InvoiceStorage](_.download(remotePath))
