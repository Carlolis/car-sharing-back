package api

import domain.services.AuthService
import domain.services.invoice.InvoiceService
import domain.services.person.PersonService
import domain.services.trip.TripService
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

  val invoiceEndpoints: List[ZServerEndpoint[PersonService & AuthService & InvoiceService, Any]] =
    List(createInvoice)
  // login,
  // register
