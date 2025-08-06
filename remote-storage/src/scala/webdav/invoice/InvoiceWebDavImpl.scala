package webdav.invoice

import adapters.SardineScalaImpl
import domain.services.invoice.storage.InvoiceStorage
import domain.services.invoice.storage.models.InvoiceFile
import zio.*

import java.io.File

case class InvoiceWebDavImpl(sardine: SardineScalaImpl) extends InvoiceStorage {
  override def upload(invoice: File): ZIO[Any, Throwable, Unit] =
    val inputStream = ZIO.attempt(new java.io.FileInputStream(invoice))
    inputStream.flatMap(is => sardine.put(is, "application/pdf", invoice.getName)).tapError(e => ZIO.logError(e.getMessage))

  override def list: ZIO[Any, Throwable, List[InvoiceFile]] =
    for {
      _           <- ZIO.log(s"Listing remote directory: ${sardine.path}")
      remoteFiles <- sardine.list
    } yield remoteFiles
      .filterNot(_.isDirectory)
      /*.filter(_.getName.equals("test"))*/
      .map(r => InvoiceFile(r.getName, r.getHref.toString, 0, 0))

  override def download(invoiceName: String): ZIO[Any, Throwable, Array[Byte]] = for {
    _            <- ZIO.log(s"Downloading invoice '$invoiceName' from: ${sardine.path}")
    inputStream  <- sardine.get(invoiceName)
    fileContents <- ZIO.attempt(inputStream.readAllBytes())
  } yield fileContents

  override def delete(invoiceName: String): ZIO[Any, Throwable, Unit] = for {
    _ <- ZIO.log(s"Deleting remote invoice: ${sardine.path}")
    _ <- sardine.delete(invoiceName)
  } yield ()
}

object InvoiceWebDavImpl:
  val layer: ZLayer[SardineScalaImpl, Nothing, InvoiceStorage] =
    ZLayer.fromFunction(InvoiceWebDavImpl(_))
