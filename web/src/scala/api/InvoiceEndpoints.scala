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

  val getAllInvoicesWithoutMaintenance: Endpoint[Unit, String, (StatusCode, ErrorResponse), List[Invoice], Any] = endpoint
    .get
    .in("api" / "invoices-without-maintenance")
    .in(auth.bearer[String]())
    .out(jsonBody[List[Invoice]])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val updateInvoice: Endpoint[Unit, (String, InvoiceUpdate), (StatusCode, ErrorResponse), InvoiceId, Any] = endpoint
    .put
    .in("api" / "invoices")
    .in(auth.bearer[String]())
    .in(multipartBody[InvoiceUpdate])
    .out(jsonBody[InvoiceId])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val deleteInvoice: Endpoint[Unit, (InvoiceId, String), (StatusCode, ErrorResponse), InvoiceId, Any] = endpoint
    .delete
    .in("api" / "invoices" / path[InvoiceId])
    .in(auth.bearer[String]())
    .out(jsonBody[InvoiceId])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val downloadInvoiceFile: Endpoint[Unit, (String, InvoiceId, String), (StatusCode, ErrorResponse), (Array[Byte], String), Any] = endpoint
    .get
    .in("api" / "invoices" / "download" / path[String]("fileName") / path[InvoiceId]("invoiceId"))
    .in(auth.bearer[String]())
    .out(byteArrayBody)
    .out(header[String]("Content-Type"))
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val getReimbursementProposals: Endpoint[Unit, String, (StatusCode, ErrorResponse), Set[Reimbursement], Any] = endpoint
    .get
    .in("api" / "invoices" / "reimbursements")
    .in(auth.bearer[String]())
    .out(jsonBody[Set[Reimbursement]])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val invoiceEndPoints = List(
    createInvoice,
    getAllInvoices,
    getAllInvoicesWithoutMaintenance,
    deleteInvoice,
    updateInvoice,
    downloadInvoiceFile,
    getReimbursementProposals
  )
