package domain.services.invoice

import domain.models.invoice.{Invoice, InvoiceCreate, InvoiceId}
import domain.services.invoice.repository.InvoiceRepository
import domain.services.invoice.repository.models.errors.SaveInvoiceFailed
import domain.services.invoice.storage.InvoiceStorage
import sttp.tapir.FileRange
import zio.*

import java.nio.file.Files

class InvoiceServiceLive(invoiceExternalStorage: InvoiceStorage, invoiceRepository: InvoiceRepository) extends InvoiceService:
  def sanitizeName(raw: String): String =
    val base  = Option(raw).getOrElse("invoice")
    // Remove or replace URL-unsafe characters including spaces, parentheses, and other special chars
    val clean = base
      .replaceAll("""[\\/:*?"<>|()\s]""", "_") // Replace URL-unsafe chars with underscore
      .replaceAll("_{2,}", "_")                // Replace multiple consecutive underscores with single underscore
      .replaceAll("^_|_$", "")                 // Remove leading/trailing underscores
      .trim()
    val result = if (clean.isEmpty) "invoice" else clean
    result.take(255) // simple limit

  override def createInvoice(invoiceCreate: InvoiceCreate): Task[InvoiceId] =
    val sanitizedName = invoiceCreate.fileName.map(sanitizeName)

    ZIO.log(
      s"Creating invoice: originalName='${invoiceCreate.fileName}', sanitizedName='$sanitizedName', mileage=${invoiceCreate.mileage}, date=${invoiceCreate.date}") *>
      (for
        // Upload only if a file is provided

        // Persist invoice regardless of file presence
        id       <- invoiceRepository
                      .createInvoice(invoiceCreate.copy(fileName = sanitizedName))
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
                            .upload(is, id.toString + "_" + sanitizedName.getOrElse(""))
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
            sanitizedName match
              case Some(name) =>
                ZIO.logWarning(s"Compensation: deleting orphan file '$sanitizedName'") *>
                  invoiceExternalStorage
                    .delete(name)
                    .tapBoth(
                      err => ZIO.logWarning(s"Compensation failed (delete '$sanitizedName'): ${err.getMessage}"),
                      _ => ZIO.logWarning(s"Compensation succeeded: '$sanitizedName' deleted")
                    )
                    .ignore
              case None       =>
                ZIO.logWarning("No file provided; skipping upload").as(false)
          case _                                       => ZIO.unit

        }
        .map(_._1)

  override def getAllInvoices: Task[List[Invoice]] = invoiceRepository.getAllInvoices

  override def deleteInvoice(id: InvoiceId): Task[InvoiceId] = invoiceRepository.deleteInvoice(id)
object InvoiceServiceLive:
  val layer: ZLayer[InvoiceStorage & InvoiceRepository, Nothing, InvoiceServiceLive] =
    ZLayer.fromFunction(InvoiceServiceLive(_, _))
