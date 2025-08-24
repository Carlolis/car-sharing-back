package api

import domain.models.invoice.*
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import zio.json.*

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
  /*  val uploadInvoice                                                                                       = endpoint
    .post
    .in("api" / "invoices" / "upload")
    .in(auth.bearer[String]())
    .in(multipartBody)
    .out(jsonBody[InvoiceId])
    .errorOut(statusCode and jsonBody[ErrorResponse])*/
  val invoiceEndPoints                                                                        = List(
    createInvoice
  )
