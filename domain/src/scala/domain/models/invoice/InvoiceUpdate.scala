package domain.models.invoice

import sttp.tapir.FileRange

import java.time.LocalDate

case class InvoiceUpdate(
  id: InvoiceId,
  name: String,
  amount: BigDecimal,
  date: LocalDate,
  driver: DriverName,
  kind: String,
  mileage: Option[Int],
  fileName: Option[String] = None,
  fileBytes: Option[FileRange] = None,
  isReimbursement: Boolean = false,
  toDriver: Option[DriverName] = None
)

object InvoiceUpdate {
  // JSON encoders/decoders are not needed for multipart form data
}
