package domain.models.invoice

import sttp.tapir.FileRange

import java.time.LocalDate
import scala.jdk.CollectionConverters.*

case class InvoiceCreate(
  amount: Double,
  mileage: Option[Int],
  date: LocalDate,
  name: String,
  driver: DriverName,
  kind: String,
  fileBytes: Option[FileRange] = None,
  fileName: Option[String] = None,
  isReimbursement: Boolean = false,
  toDriver: Option[DriverName] = None
)

object InvoiceCreate {
  /*implicit val encoder: JsonEncoder[InvoiceCreate] =
    DeriveJsonEncoder.gen[InvoiceCreate]*/
  /*  implicit val decoder: JsonDecoder[InvoiceCreate] =
    DeriveJsonDecoder.gen[InvoiceCreate]*/
}
