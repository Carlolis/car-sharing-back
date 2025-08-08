package domain.services.invoice

import domain.models.invoice.{Invoice, InvoiceCreate, InvoiceId}
import domain.services.invoice.repository.InvoiceRepository
import domain.services.invoice.storage.InvoiceStorage
import domain.services.invoice.storage.models.errors.UploadFailed
import zio.*

import java.util.UUID

class InvoiceServiceLive(invoiceExternalStorage: InvoiceStorage, invoiceRepository: InvoiceRepository) extends InvoiceService:
  def sanitizeName(raw: String): String =
    val base  = Option(raw).getOrElse("invoice")
    val clean = base.replaceAll("""[\\/:*?"<>|]""", "_").replaceAll("\\s+", "_")
    val named = if clean.toLowerCase.endsWith(".pdf") then clean else s"$clean.pdf"
    named.take(255) // simple limit

  override def createInvoice(tripCreate: InvoiceCreate): Task[InvoiceId] =
    val sanitizedName = sanitizeName(tripCreate.name)

    ZIO.log(
      s"Creating invoice: originalName='${tripCreate.name}', sanitizedName='$sanitizedName', distance=${tripCreate.distance}, date=${tripCreate.date}") *>
      (for
        // Upload only if a file is provided
        uploaded <- tripCreate.fileBytes match
                      case Some(bytes) =>
                        ZIO.log(s"File provided (${bytes.length} bytes)")

                        ZIO.log(s"Uploading file '$sanitizedName'") *>
                          invoiceExternalStorage
                            .upload(bytes, sanitizedName)
                            .tapBoth(
                              err => ZIO.logError(s"Upload failed for '$sanitizedName': ${err.getMessage}"),
                              _ => ZIO.log(s"Upload succeeded for '$sanitizedName'")
                            )
                            .as(true)
                      case None        =>
                        ZIO.log("No file provided; skipping upload").as(false)

        // Persist invoice regardless of file presence
        id       <- invoiceRepository
                      .createInvoice(tripCreate.copy(name = sanitizedName))
                      .map(InvoiceId(_))
                      .tapBoth(
                        err => ZIO.logError(s"Persistence failed for '$sanitizedName': ${err.getMessage}"),
                        id => ZIO.log(s"Persistence succeeded: id=$id for '$sanitizedName'")
                      )
      yield (id, uploaded))
        .tap { case (id, _) => ZIO.log(s"Invoice created: id=$id, file='$sanitizedName'") }
        .tapErrorCause(cause => ZIO.logErrorCause(s"Error while creating invoice (file='$sanitizedName')", cause))
        // Compensate only if upload actually happened
        .onError {
          case zio.Cause.Fail(_: UploadFailed, _) =>
            ZIO.logWarning(s"Compensation: deleting orphan file '$sanitizedName'") *>
              invoiceExternalStorage
                .delete(sanitizedName)
                .tapBoth(
                  err => ZIO.logWarning(s"Compensation failed (delete '$sanitizedName'): ${err.getMessage}"),
                  _ => ZIO.logWarning(s"Compensation succeeded: '$sanitizedName' deleted")
                )
                .ignore
          case _                                  => ZIO.unit

        }
        .map(_._1)

  override def getAllInvoices: Task[List[Invoice]] = ???

  override def deleteInvoice(id: UUID): Task[UUID] = ???
object InvoiceServiceLive:
  val layer: ZLayer[InvoiceStorage & InvoiceRepository, Nothing, InvoiceServiceLive] =
    ZLayer.fromFunction(InvoiceServiceLive(_, _))
