package webdav.invoice

import adapters.SardineScalaImpl
import domain.services.invoice.storage.InvoiceStorage
import domain.services.invoice.storage.models.InvoiceFile
import domain.services.invoice.storage.models.errors.UploadFailed
import zio.*

case class InvoiceWebDavImpl(sardine: SardineScalaImpl) extends InvoiceStorage {
  override def upload(invoice: Array[Byte], name: String): ZIO[Any, UploadFailed, Unit] =
    ZIO.scoped {
      sardine
        .put(invoice, "application/pdf", name)
        .tapError(e => ZIO.logError(e.getMessage))
        .mapError(UploadFailed.apply)
    }

  override def list: ZIO[Any, Throwable, List[InvoiceFile]] =
    ZIO.scoped {
      for {

        remoteFiles <- sardine.list
      } yield remoteFiles
        .filterNot(_.isDirectory)
        .map(r => InvoiceFile(r.getName, r.getHref.toString, 0, 0))
    }

  override def download(invoiceName: String): ZIO[Any, Throwable, Array[Byte]] =
    ZIO.scoped {
      for {

        inputStream  <- sardine.get(invoiceName)
        fileContents <- ZIO.attempt(inputStream.readAllBytes())
      } yield fileContents
    }

  override def delete(invoiceName: String): ZIO[Any, Throwable, Unit] =
    ZIO.scoped {
      for {

        _ <- sardine.delete(invoiceName)
      } yield ()
    }
}

object InvoiceWebDavImpl:
  val layer: ZLayer[SardineScalaImpl, Nothing, InvoiceStorage] =
    ZLayer.fromFunction(InvoiceWebDavImpl(_))
