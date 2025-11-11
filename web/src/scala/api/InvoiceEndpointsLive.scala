package api

import domain.services.AuthService
import domain.services.invoice.InvoiceService
import domain.services.person.PersonService
import sttp.model.StatusCode
import sttp.tapir.ztapir.*
import zio.*

object InvoiceEndpointsLive:
  private type Env = PersonService & AuthService & InvoiceService

  private val createInvoice: ZServerEndpoint[Env, Any] =
    InvoiceEndpoints.createInvoice.serverLogic {
      case (token, invoiceCreate) =>
        (for {
          _    <- AuthService.authenticate(token)
          uuid <- InvoiceService.createInvoice(invoiceCreate)
        } yield uuid)
          .map(Right(_))
          .tapError(error => ZIO.logError(s"Error: $error"))
          .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
    }

  private val getAllInvoices: ZServerEndpoint[Env, Any] =
    InvoiceEndpoints.getAllInvoices.serverLogic { token =>
      (for {
        _      <- AuthService.authenticate(token)
        _      <- ZIO.logInfo("Getting invoices")
        result <- InvoiceService.getAllInvoices
        _      <- ZIO.logInfo("Invoices found " + result.toString)
      } yield result)
        .map(Right(_))
        .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
    }

  private val getAllInvoicesWithoutMaintenance: ZServerEndpoint[Env, Any] =
    InvoiceEndpoints.getAllInvoicesWithoutMaintenance.serverLogic { token =>
      (for {
        _      <- AuthService.authenticate(token)
        _      <- ZIO.logInfo("Getting invoices without maintenance")
        result <- InvoiceService.getAllInvoicesWithoutMaintenance
        _      <- ZIO.logInfo("Invoices without maintenance found " + result.toString)
      } yield result)
        .map(Right(_))
        .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
    }

  private val deleteInvoiceEndpoint: ZServerEndpoint[Env, Any] =
    InvoiceEndpoints.deleteInvoice.serverLogic {
      case (invoiceId, token) =>
        (for {
          _         <- AuthService.authenticate(token)
          _         <- ZIO.logInfo("Deleting invoice " + invoiceId)
          invoiceId <- InvoiceService.deleteInvoice(invoiceId)
        } yield invoiceId)
          .map(Right(_))
          .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
    }

  private val updateInvoice: ZServerEndpoint[Env, Any] =
    InvoiceEndpoints.updateInvoice.serverLogic {
      case (token, invoiceUpdate) =>
        (for {
          _    <- AuthService.authenticate(token)
          _    <- ZIO.logInfo("Updating invoice " + invoiceUpdate.toString)
          uuid <- InvoiceService.updateInvoice(invoiceUpdate)
          _    <- ZIO.logInfo("Invoice updated " + uuid.toString)
        } yield uuid)
          .map(Right(_))
          .tapError(error => ZIO.logError(s"Error: $error"))
          .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
    }

  private val downloadInvoiceFile: ZServerEndpoint[Env, Any] =
    InvoiceEndpoints.downloadInvoiceFile.serverLogic {
      case (fileName, id, token) =>
        (for {
          _         <- AuthService.authenticate(token)
          _         <- ZIO.logInfo(s"Downloading invoice file: $fileName")
          fileBytes <- InvoiceService.download(fileName, id)
          _         <- ZIO.logInfo(s"Successfully downloaded file: $fileName, size: ${fileBytes.length} bytes")
        } yield (fileBytes, "application/octet-stream"))
          .map(Right(_))
          .tapError(error => ZIO.logError(s"Error downloading file $fileName: $error"))
          .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
    }

  private val getReimbursementProposals: ZServerEndpoint[Env, Any] =
    InvoiceEndpoints.getReimbursementProposals.serverLogic { token =>
      (for {
        _                      <- AuthService.authenticate(token)
        _                      <- ZIO.logInfo("Getting reimbursement proposal")
        reimbursementProposals <- InvoiceService.getReimbursementProposals
        _                      <- ZIO.logInfo("Successfully generated reimbursement proposal")
      } yield reimbursementProposals)
        .map(Right(_))
        .tapError(error => ZIO.logError(s"Error generating reimbursement proposal: $error"))
        .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
    }

  val invoiceEndpointsLive: List[ZServerEndpoint[Env, Any]] =
    List(createInvoice, getAllInvoices, getAllInvoicesWithoutMaintenance, deleteInvoiceEndpoint, updateInvoice, downloadInvoiceFile, getReimbursementProposals)
