package api

import domain.models.invoice.*
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
    .in("api" / "invoices")
    .in(auth.bearer[String]())
    .in(multipartBody[InvoiceCreate])
    .out(jsonBody[InvoiceId])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val getAllInvoices: Endpoint[Unit, String, (StatusCode, ErrorResponse), List[Invoice], Any] = endpoint
    .get
    .in("api" / "invoices")
    .in(auth.bearer[String]())
    .out(jsonBody[List[Invoice]])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val deleteInvoiceEndpoint: Endpoint[Unit, (InvoiceId, String), (StatusCode, ErrorResponse), InvoiceId, Any] = endpoint
    .delete
    .in("api" / "trips" / path[InvoiceId])
    .in(auth.bearer[String]())
    .out(jsonBody[InvoiceId])
    .errorOut(statusCode and jsonBody[ErrorResponse])
  val invoiceEndPoints                                                                                        = List(
    createInvoice,
    getAllInvoices,
    deleteInvoiceEndpoint
  )
