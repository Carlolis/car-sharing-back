package domain.services.invoice.repository.models.errors

import scala.util.control.NoStackTrace

sealed trait InvoiceRepositoryError extends Exception with NoStackTrace

final case class SaveInvoiceFailed(
  invoiceName: String,
  detail: String,
  underlying: Option[Throwable] = None
) extends InvoiceRepositoryError:
  override def getMessage: String         = s"Failed to save invoice file $invoiceName: $detail"
  override def getCause: Throwable | Null = underlying.orNull
object SaveInvoiceFailed:
  def apply(invoiceName: String, message: String): SaveInvoiceFailed =
    new SaveInvoiceFailed(invoiceName, message, None)

  def fromCause(invoiceName: String, cause: Throwable): SaveInvoiceFailed =
    new SaveInvoiceFailed(invoiceName, Option(cause.getMessage).getOrElse("Unknown error"), Some(cause))

  def apply(cause: Throwable): SaveInvoiceFailed =
    fromCause("<unknown>", cause)
