package domain.services.invoice

import domain.models.invoice.*
import domain.services.invoice.repository.InvoiceRepository
import domain.services.invoice.repository.models.errors.SaveInvoiceFailed
import domain.services.invoice.storage.InvoiceStorage
import domain.services.person.PersonService
import sttp.tapir.FileRange
import zio.*

import java.nio.file.Files

class InvoiceServiceLive(invoiceExternalStorage: InvoiceStorage, invoiceRepository: InvoiceRepository, personService: PersonService)
    extends InvoiceService:
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

  override def updateInvoice(invoiceUpdate: InvoiceUpdate): Task[InvoiceId] =
    val sanitizedName = invoiceUpdate.fileName.map(sanitizeName)

    ZIO.log(
      s"Updating invoice: id=${invoiceUpdate.id}, originalName='${invoiceUpdate.fileName}', sanitizedName='$sanitizedName', mileage=${invoiceUpdate.mileage}, date=${invoiceUpdate.date}") *>
      (for {
        // Get existing invoice to check for old file
        existingInvoice <- invoiceRepository
                             .getAllInvoices
                             .map(_.find(_.id == invoiceUpdate.id))
                             .someOrFail(new RuntimeException(s"Invoice ${invoiceUpdate.id} not found"))

        // Handle file operations if a new file is provided
        _               <- invoiceUpdate.fileBytes match {
                             case Some(bytes) =>
                               ZIO.log(s"New file provided (${bytes.range} bytes)") *>
                                 (for {
                                   // Delete old file if it exists and is different from the new one
                                   _ <- existingInvoice.fileName match {
                                          case Some(oldFileName) =>
                                            ZIO.log(s"Deleting old file: ${existingInvoice.id}_$oldFileName") *>
                                              invoiceExternalStorage
                                                .delete(existingInvoice.id.toString + "_" + oldFileName)
                                                .tapBoth(
                                                  err => ZIO.logWarning(s"Failed to delete old file '$oldFileName': ${err.getMessage}"),
                                                  _ => ZIO.log(s"Successfully deleted old file '$oldFileName'")
                                                )
                                                .ignore // Don't fail the whole operation if old file deletion fails
                                          case _                 => ZIO.unit
                                        }

                                   // Upload new file
                                   _ <- {
                                     val path      = invoiceUpdate.fileBytes.get.file.toPath
                                     val fileBytes = Files.newInputStream(path).readAllBytes()
                                     ZIO.log(s"Uploading new file '$sanitizedName' with id ${invoiceUpdate.id}") *>
                                       invoiceExternalStorage
                                         .upload(fileBytes, invoiceUpdate.id.toString + "_" + sanitizedName.getOrElse(""))
                                         .tapBoth(
                                           err => ZIO.logError(s"Upload failed for '$sanitizedName': ${err.getMessage}"),
                                           _ => ZIO.log(s"Upload succeeded for '$sanitizedName'")
                                         )
                                   }
                                 } yield ())
                             case None        =>
                               ZIO.log("No new file provided; keeping existing file").unit
                           }

        // Update invoice metadata in repository
        updatedInvoice   = Invoice(
                             id = invoiceUpdate.id,
                             name = invoiceUpdate.name,
                             amount = invoiceUpdate.amount,
                             date = invoiceUpdate.date,
                             drivers = invoiceUpdate.drivers,
                             kind = invoiceUpdate.kind,
                             mileage = invoiceUpdate.mileage,
                             fileName = sanitizedName.orElse(existingInvoice.fileName)
                           )

        id <- invoiceRepository
                .updateInvoice(updatedInvoice)
                .tapBoth(
                  err => ZIO.logError(s"Repository update failed for invoice ${invoiceUpdate.id}: ${err.getMessage}"),
                  _ => ZIO.log(s"Repository update succeeded for invoice ${invoiceUpdate.id}")
                )
      } yield invoiceUpdate.id)
        .tap(_ => ZIO.log(s"Invoice updated: id=${invoiceUpdate.id}, file='$sanitizedName'"))
        .tapErrorCause(cause => ZIO.logErrorCause(s"Error while updating invoice (id=${invoiceUpdate.id}, file='$sanitizedName')", cause))
        // Compensation logic: if repository update fails after file upload, try to delete the uploaded file
        .onError {
          case zio.Cause.Fail(_: SaveInvoiceFailed, _) =>
            invoiceUpdate.fileBytes match {
              case Some(_) =>
                sanitizedName match {
                  case Some(name) =>
                    ZIO.logWarning(s"Compensation: deleting uploaded file after repository failure '$name'") *>
                      invoiceExternalStorage
                        .delete(invoiceUpdate.id.toString + "_" + name)
                        .tapBoth(
                          err => ZIO.logWarning(s"Compensation failed (delete '$name'): ${err.getMessage}"),
                          _ => ZIO.logWarning(s"Compensation succeeded: '$name' deleted")
                        )
                        .ignore
                  case None       => ZIO.unit
                }
              case None    => ZIO.unit
            }
          case _                                       => ZIO.unit
        }

  override def download(fileName: String, id: InvoiceId): ZIO[Any, Throwable, Array[Byte]] = {
    val sanitizeFileName = sanitizeName(fileName)

    invoiceExternalStorage.download(id.toString + "_" + sanitizeFileName)
  }

  override def getReimbursementProposal: Task[Set[Reimbursement]] =
    for {
      allInvoices                   <- getAllInvoices
      drivers                       <- personService.getAll
      _                             <- ZIO.logInfo(s"Got ${drivers.size} drivers")
      totalAmount                    = allInvoices.foldLeft(0L)((total, invoice) => invoice.amount + total)
      eachPart                       = totalAmount / drivers.size
      driversAmount                  =
        drivers.map(d =>
          (
            d.name,
            allInvoices.foldLeft(0L)((total, invoice) => if (invoice.drivers.head.toString == d.name) invoice.amount + total else total)))
      amountAboveEachPartDriverCount = driversAmount.foldLeft(0) { (acc, driverAmount) =>
                                         if (driverAmount._2 > eachPart) acc + 1
                                         else acc
                                       }
      reimbursements                 = driversAmount.map { (driverName, total) =>

                                         val othersDriverMapReimbursement: Map[DriverName, Float] =
                                           driversAmount
                                             .filter(_._1 != driverName)
                                             .foldLeft(Map.empty[DriverName, Float]) {
                                               case (acc, (name, amount)) =>
                                                 if ((total >= eachPart) || (total >= amount)) acc + (DriverName(name) -> 0L)
                                                 else if (amountAboveEachPartDriverCount == 1) acc + (DriverName(name) -> eachPart)
                                                 else acc + (DriverName(name)                                          -> (amount - eachPart))
                                             }
                                         val totalToReimburse                                     = othersDriverMapReimbursement.values.sum

                                         Reimbursement(DriverName(driverName), totalToReimburse, othersDriverMapReimbursement)
                                       }
      _ <- ZIO.logInfo(s"Got $reimbursements ")
    } yield reimbursements
object InvoiceServiceLive:
  val layer: ZLayer[InvoiceStorage & InvoiceRepository & PersonService, Nothing, InvoiceServiceLive] =
    ZLayer.fromFunction(InvoiceServiceLive(_, _, _))
