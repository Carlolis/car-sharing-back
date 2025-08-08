package domain.services.invoice.storage.models.errors

import scala.util.control.NoStackTrace

sealed trait StorageError extends Exception with NoStackTrace

final case class UploadFailed(
  fileName: String,
  detail: String,
  underlying: Option[Throwable] = None
) extends StorageError:
  override def getMessage: String         = s"Failed to upload file $fileName: $detail"
  override def getCause: Throwable | Null = underlying.orNull
object UploadFailed:
  def apply(fileName: String, message: String): UploadFailed =
    new UploadFailed(fileName, message, None)

  def fromCause(fileName: String, cause: Throwable): UploadFailed =
    new UploadFailed(fileName, Option(cause.getMessage).getOrElse("Unknown error"), Some(cause))

  def apply(cause: Throwable): UploadFailed =
    fromCause("<unknown>", cause)
