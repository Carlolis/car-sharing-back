package inMemoryService

import domain.services.invoice.storage.InvoiceStorage
import domain.services.invoice.storage.models.InvoiceFile
import domain.services.invoice.storage.models.errors.UploadFailed
import zio.*

import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

class InMemoryInvoiceStorage extends InvoiceStorage {
  private val files: mutable.Map[String, (Array[Byte], InvoiceFile)] =
    new ConcurrentHashMap[String, (Array[Byte], InvoiceFile)]().asScala

  override def upload(invoice: Array[Byte], name: String): ZIO[Any, UploadFailed, Unit] =
    ZIO
      .attempt {
        val invoiceFile = InvoiceFile(
          name = name,
          path = s"/invoices/$name",
          size = invoice.length.toLong,
          lastModified = java.lang.System.currentTimeMillis()
        )
        files.put(name, (invoice, invoiceFile))
      }.unit.mapError(ex => UploadFailed(name, ex.getMessage))

  override def list: ZIO[Any, Throwable, List[InvoiceFile]] =
    ZIO.succeed(files.values.map(_._2).toList)

  override def download(remotePath: String): ZIO[Any, Throwable, Array[Byte]] = {
    val fileName = remotePath.split("/").lastOption.getOrElse(remotePath)
    ZIO.attempt {
      files.get(fileName) match {
        case Some((bytes, _)) => bytes
        case None             => throw new RuntimeException(s"File '$fileName' not found")
      }
    }
  }

  override def delete(invoiceName: String): ZIO[Any, Throwable, Unit] =
    ZIO.attempt {
      files.remove(invoiceName) match {
        case Some(_) => ()
        case None    => throw new RuntimeException(s"File '$invoiceName' not found")
      }
    }
}

object InMemoryInvoiceStorage {
  val layer: ZLayer[Any, Nothing, InMemoryInvoiceStorage] =
    ZLayer.succeed(new InMemoryInvoiceStorage)
}
