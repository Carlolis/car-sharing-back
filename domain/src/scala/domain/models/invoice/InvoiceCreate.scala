package domain.models.invoice

import zio.json.*

import java.time.LocalDate
import scala.jdk.CollectionConverters.*

case class InvoiceCreate(
  distance: Int,
  date: LocalDate,
  name: String,
  drivers: Set[String],
  fileBytes: Option[Array[Byte]] = None
)

object InvoiceCreate {
  implicit val encoder: JsonEncoder[InvoiceCreate] =
    DeriveJsonEncoder.gen[InvoiceCreate]
  implicit val decoder: JsonDecoder[InvoiceCreate] =
    DeriveJsonDecoder.gen[InvoiceCreate]
}
