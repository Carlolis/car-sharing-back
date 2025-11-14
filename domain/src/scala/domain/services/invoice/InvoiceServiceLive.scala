package domain.services.invoice

import domain.models.invoice.*
import domain.services.invoice.repository.InvoiceRepository
import domain.services.invoice.repository.models.errors.SaveInvoiceFailed
import domain.services.invoice.storage.InvoiceStorage
import domain.services.person.PersonService
import sttp.tapir.FileRange
import zio.*

import java.nio.file.Files
import scala.math.BigDecimal.RoundingMode

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

  override def getAllInvoicesWithoutMaintenance: Task[List[Invoice]] =
    invoiceRepository.getAllInvoicesWithoutMaintenance

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
                                   _ <- deleteFileIfExists(existingInvoice)

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
                               invoiceUpdate.fileName match
                                 case None =>
                                   // Delete old file if it exists and is different from the new one
                                   deleteFileIfExists(existingInvoice)
                                 case _   =>
                                   ZIO.log("No new file provided; keeping existing file").unit
                           }

        // Update invoice metadata in repository
        updatedInvoice   = Invoice(
                             id = invoiceUpdate.id,
                             name = invoiceUpdate.name,
                             amount = invoiceUpdate.amount,
                             date = invoiceUpdate.date,
                             driver = invoiceUpdate.driver,
                             kind = invoiceUpdate.kind,
                             mileage = invoiceUpdate.mileage,
                             fileName = sanitizedName,
                             toDriver = invoiceUpdate.toDriver
                           )

        id <- invoiceRepository
                .updateInvoice(updatedInvoice)
                .tapBoth(
                  err => ZIO.logError(s"Repository update failed for invoice ${invoiceUpdate.id}: ${err.getMessage}"),
                  _ => ZIO.log(s"Repository update succeeded for invoice ${invoiceUpdate.id}")
                )
      } yield invoiceUpdate.id)
        .zipLeft(ZIO.log(s"Invoice updated: id=${invoiceUpdate.id}, file='$sanitizedName'"))
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

  private def deleteFileIfExists(invoice: Invoice): Task[Unit] =
    invoice.fileName match {
      case Some(oldFileName) =>
        ZIO.log(s"Deleting old file: ${invoice.id}_$oldFileName") *>
          invoiceExternalStorage
            .delete(invoice.id.toString + "_" + oldFileName)
            .tapBoth(
              err => ZIO.logWarning(s"Failed to delete old file '$oldFileName': ${err.getMessage}"),
              _ => ZIO.log(s"Successfully deleted old file '$oldFileName'")
            )
            .ignore // Don't fail the whole operation if old file deletion fails
      case _                 => ZIO.unit}

  override def download(fileName: String, id: InvoiceId): ZIO[Any, Throwable, Array[Byte]] = {
    val sanitizeFileName = sanitizeName(fileName)

    invoiceExternalStorage.download(id.toString + "_" + sanitizeFileName)
  }

  override def getReimbursementProposals: Task[Set[Reimbursement]] =
    for {
      allInvoices <- getAllInvoices
      drivers     <- personService.getAll
      _           <- ZIO.logInfo(s"Got ${drivers.size} drivers")
      // Calculate the total amount from all invoices, excluding specific kinds (Remboursement, Carburant, Péage)
      // This represents the total pool of money to be shared among drivers.
      totalReimbursableAmount  =
        allInvoices.foldLeft(BigDecimal(0.0))((total, invoice) =>
          if (invoice.kind == "Remboursement" || invoice.kind == "Carburant" || invoice.kind == "Péage") total else invoice.amount + total)

      // Calculate the amount each driver should ideally contribute or receive,
      // based on the total reimbursable amount divided equally among all drivers.
      individualShareAmount  = (totalReimbursableAmount / BigDecimal(drivers.size))
                                         .setScale(2, RoundingMode.HALF_UP).doubleValue()
      // Calculate the net expenses for each driver.
      // This involves summing up invoices where the driver is the payer and subtracting invoices
      // where the driver is designated to be reimbursed.
      driverNetExpenses                  =
        drivers.map(d =>
          (
            d.name,
            allInvoices.foldLeft(BigDecimal(0.0)) { (total, invoice) =>
              if (invoice.toDriver.contains(d.name))
                total - invoice.amount
              else if (invoice.driver.toString == d.name)
                invoice.amount + total
              else total
            }))
      // Count how many drivers have a net expense greater than their individual share.
      // This helps in determining the reimbursement logic, especially for edge cases.
      driversOwingCount = driverNetExpenses.foldLeft(0) { (acc, driverAmount) =>
                                         if (driverAmount._2 > individualShareAmount) acc + 1
                                         else acc
                                       }
      reimbursements                 = driverNetExpenses.map { (driverName, currentDriverNetExpense) =>
                                         val totalToReimburse = individualShareAmount - currentDriverNetExpense

                                         // Calculate how much each other driver contributes to or receives from the current driver's reimbursement.
                                         // This map represents the individual transactions needed to balance the accounts.
                                         val contributionsFromOtherDrivers: Map[DriverName, BigDecimal] =
                                           driverNetExpenses
                                             .filter(_._1 != driverName) // Consider only other drivers
                                             .foldLeft(Map.empty[DriverName, BigDecimal]) {
                                               case (acc, (otherDriverName, otherDriverNetExpense)) =>
                                                 // If the current driver is owed money (currentDriverNetExpense < 0.0),
                                                 // or if the current driver has already paid more than their share,
                                                 // other drivers don't directly contribute to this driver's reimbursement in this specific calculation.
                                                 if ((currentDriverNetExpense >= individualShareAmount) || (currentDriverNetExpense >= otherDriverNetExpense)) acc + (DriverName(otherDriverName) -> BigDecimal(0.0))
                                                 else if (driversOwingCount == 1)
                                                   // Special case: if only one driver has a net expense above the individual share,
                                                   // that driver is the primary payer.
                                                   if (otherDriverNetExpense > individualShareAmount)
                                                     // If this 'other' driver also has a net expense above the individual share,
                                                     // they contribute the difference to balance the current driver.
                                                     acc + (DriverName(otherDriverName) -> (individualShareAmount - currentDriverNetExpense))
                                                   else
                                                     // Otherwise, this 'other' driver doesn't contribute to the current driver's reimbursement.
                                                     acc + (DriverName(otherDriverName) -> BigDecimal(0.0))
                                                 else
                                                   // General case: calculate the difference between the other driver's net expense
                                                   // and the individual share. This represents their contribution or deficit.
                                                   acc + (DriverName(otherDriverName) -> (otherDriverNetExpense - individualShareAmount))
                                             }

                                         Reimbursement(DriverName(driverName), totalToReimburse, contributionsFromOtherDrivers)
                                       }
      _                             <- ZIO.logInfo(s"Got $reimbursements ")
    } yield reimbursements
object InvoiceServiceLive:
  val layer: ZLayer[InvoiceStorage & InvoiceRepository & PersonService, Nothing, InvoiceServiceLive] =
    ZLayer.fromFunction(InvoiceServiceLive(_, _, _))
