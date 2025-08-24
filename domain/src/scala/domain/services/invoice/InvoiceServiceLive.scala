package domain.services.invoice

import domain.models.invoice.{Invoice, InvoiceCreate, InvoiceId}
import domain.services.invoice.repository.InvoiceRepository
import domain.services.invoice.repository.models.errors.SaveInvoiceFailed
import domain.services.invoice.storage.InvoiceStorage
import sttp.tapir.FileRange
import zio.*

import java.nio.file.Files
import java.util.UUID

class InvoiceServiceLive(invoiceExternalStorage: InvoiceStorage, invoiceRepository: InvoiceRepository) extends InvoiceService:
  def sanitizeName(raw: String): String =
    val base  = Option(raw).getOrElse("invoice")
    val clean = base.replaceAll("""[\\/:*?"<>|]""", "").replaceAll("\\s+", "").trim()
    clean.take(255) // simple limit

  override def createInvoice(invoiceCreate: InvoiceCreate): Task[InvoiceId] =
    val sanitizedName = sanitizeName(invoiceCreate.fileName.getOrElse("No file provided"))

    ZIO.log(
      s"Creating invoice: originalName='${invoiceCreate.fileName}', sanitizedName='$sanitizedName', distance=${invoiceCreate.distance}, date=${invoiceCreate.date}") *>
      (for
        // Upload only if a file is provided

        // Persist invoice regardless of file presence
        id       <- invoiceRepository
                      .createInvoice(invoiceCreate.copy(name = sanitizedName))
                      .map(InvoiceId(_))
                      .tapBoth(
                        err => ZIO.logError(s"Persistence failed for '$sanitizedName': ${err.getMessage}"),
                        id => ZIO.log(s"Persistence succeeded: id=$id for '$sanitizedName'")
                      )
        uploaded <- invoiceCreate.fileBytes match
                      case Some(bytes) =>
                        ZIO.log(s"File provided (${bytes.range} bytes)")
                        val path = invoiceCreate.fileBytes.get.file.toPath
                        val is   = Files.newInputStream(path).readAllBytes()
                        ZIO.log(s"Uploading file '$sanitizedName' with id $id") *>
                          invoiceExternalStorage
                            .upload(is, id.toString + "_" + sanitizedName)
                            .tapBoth(
                              err => ZIO.logError(s"Upload failed for '$sanitizedName': ${err.getMessage}"),
                              _ => ZIO.log(s"Upload succeeded for '$sanitizedName'")
                            )
                            .as(true)
                      case None        =>
                        ZIO.logWarning("No file provided; skipping upload").as(false)
      yield (id, uploaded))
        .tap { case (id, _) => ZIO.log(s"Invoice created: id=$id, file='$sanitizedName'") }
        .tapErrorCause(cause => ZIO.logErrorCause(s"Error while creating invoice (file='$sanitizedName')", cause))
        // Compensate only if upload actually happened
        .onError {
          case zio.Cause.Fail(_: SaveInvoiceFailed, _) =>
            ZIO.logWarning(s"Compensation: deleting orphan file '$sanitizedName'") *>
              invoiceExternalStorage
                .delete(sanitizedName)
                .tapBoth(
                  err => ZIO.logWarning(s"Compensation failed (delete '$sanitizedName'): ${err.getMessage}"),
                  _ => ZIO.logWarning(s"Compensation succeeded: '$sanitizedName' deleted")
                )
                .ignore
          case _                                       => ZIO.unit

        }
        .map(_._1)

  override def getAllInvoices: Task[List[Invoice]] = ???

  override def deleteInvoice(id: UUID): Task[UUID] = ???
object InvoiceServiceLive:
  val layer: ZLayer[InvoiceStorage & InvoiceRepository, Nothing, InvoiceServiceLive] =
    ZLayer.fromFunction(InvoiceServiceLive(_, _))
