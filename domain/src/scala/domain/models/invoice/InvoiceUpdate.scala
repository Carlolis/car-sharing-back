package domain.models.invoice

import sttp.tapir.FileRange

import java.time.LocalDate

case class InvoiceUpdate(
  id: InvoiceId,
  name: String,
  amount: Int,
  date: LocalDate,
  drivers: Set[DriverName],
  kind: String,
  mileage: Option[Int],
  fileName: Option[String] = None,
  fileBytes: Option[FileRange] = None
)

object InvoiceUpdate {
  // JSON encoders/decoders are not needed for multipart form data
}