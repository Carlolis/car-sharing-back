package webdav.invoice

import domain.services.invoice.storage.InvoiceStorage
import domain.services.invoice.storage.models.InvoiceFile
import zio.*

import java.io.File

case class InvoiceWebDavImpl() extends InvoiceStorage {
  override def upload(localFile: File, remoteDir: String): ZIO[Any, Throwable, Unit] = ???

  override def list(remoteDir: String): ZIO[Any, Throwable, List[InvoiceFile]] = ???

  override def download(remotePath: String): ZIO[Any, Throwable, Array[Byte]] = ???
}

object InvoiceWebDavImpl:
  val layer: ZLayer[Any, Nothing, InvoiceStorage] =
    ZLayer.succeed(InvoiceWebDavImpl())
