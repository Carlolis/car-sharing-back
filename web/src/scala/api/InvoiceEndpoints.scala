package api

import domain.models.invoice.*
import domain.services
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import zio.json.*

import java.util.UUID

case class ErrorResponse(message: String) derives JsonEncoder, JsonDecoder

object InvoiceEndpoints:
  val createInvoice: Endpoint[Unit, (String, InvoiceCreate), (StatusCode, ErrorResponse), InvoiceId, Any] = endpoint
    .post
    .in("api" / "trips")
    .in(auth.bearer[String]())
    .in(jsonBody[InvoiceCreate])
    .out(jsonBody[InvoiceId])
    .errorOut(statusCode and jsonBody[ErrorResponse])
  val invoiceEndPoints                                                                                    = List(
    createInvoice
  )
