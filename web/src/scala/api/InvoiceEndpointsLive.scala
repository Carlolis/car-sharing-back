import api.{ErrorResponse, InvoiceEndpoints}
import domain.services.AuthService
import domain.services.invoice.InvoiceService
import domain.services.invoice.storage.InvoiceStorage
import domain.services.person.PersonService
import sttp.model.StatusCode
import sttp.tapir.ztapir.*
import zio.*

object InvoiceEndpointsLive:
  type Env = PersonService & AuthService & InvoiceService & InvoiceStorage

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
      case (token, trip) =>
        (for {
          _    <- AuthService.authenticate(token)
          _    <- ZIO.logInfo("Updating trip " + trip.toString)
          uuid <- InvoiceService.updateInvoice(trip)
          _    <- ZIO.logInfo("Invoice updated " + uuid.toString)
        } yield uuid)
          .map(Right(_))
          .tapError(error => ZIO.logError(s"Error: $error"))
          .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
    }

  private val downloadInvoiceFile: ZServerEndpoint[Env, Any] =
    InvoiceEndpoints.downloadInvoiceFile.serverLogic {
      case (fileName, token) =>
        (for {
          _         <- AuthService.authenticate(token)
          _         <- ZIO.logInfo(s"Downloading invoice file: $fileName")
          fileBytes <- InvoiceStorage.download(fileName)
          _         <- ZIO.logInfo(s"Successfully downloaded file: $fileName, size: ${fileBytes.length} bytes")
        } yield (fileBytes, "application/octet-stream"))
          .map(Right(_))
          .tapError(error => ZIO.logError(s"Error downloading file $fileName: $error"))
          .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
    }

  val invoiceEndpointsLive: List[ZServerEndpoint[Env, Any]] =
    List(createInvoice, getAllInvoices, deleteInvoiceEndpoint, updateInvoice, downloadInvoiceFile)
