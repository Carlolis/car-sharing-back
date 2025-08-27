package api

import domain.services.AuthService
import domain.services.invoice.InvoiceService
import domain.services.person.PersonService
import sttp.model.StatusCode
import sttp.tapir.ztapir.*
import zio.*

object InvoiceEndpointsLive:
  private val createInvoice: ZServerEndpoint[PersonService & AuthService & InvoiceService, Any] =
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

  private val getAllInvoices: ZServerEndpoint[PersonService & AuthService & InvoiceService, Any] =
    InvoiceEndpoints.getAllInvoices.serverLogic { token =>
      (for {

        _      <- AuthService.authenticate(token)
        // user <- ZIO
        //   .fromOption(userOpt)
        //   .orElseFail(new Exception("Unauthorized"))
        _      <- ZIO.logInfo(
                    "Getting invoices "
                  )
        result <- InvoiceService.getAllInvoices
        _      <- ZIO.logInfo(
                    "Invoices found " + result.toString
                  )
      } yield result)
        .map(Right(_))
        .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
    }

  private val deleteInvoiceEndpoint: ZServerEndpoint[PersonService & AuthService & InvoiceService, Any] =
    InvoiceEndpoints.deleteInvoice.serverLogic {
      case (invoiceId, token) =>
        (for {

          _         <- AuthService.authenticate(token)
          // user <- ZIO
          //   .fromOption(userOpt)
          //   .orElseFail(new Exception("Unauthorized"))
          _         <- ZIO.logInfo(
                         "Getting invoices "
                       )
          invoiceId <- InvoiceService
                         .deleteInvoice(invoiceId)

        } yield invoiceId)
          .map(Right(_))
          .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
    }

  private val updateInvoice: ZServerEndpoint[PersonService & AuthService & InvoiceService, Any] =
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

  val invoiceEndpoints: List[ZServerEndpoint[PersonService & AuthService & InvoiceService, Any]] =
    List(createInvoice, getAllInvoices, deleteInvoiceEndpoint, updateInvoice)
