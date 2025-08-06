package domain.services.invoice.models

import zio.json.*

import java.time.LocalDate
import java.util.UUID
import scala.jdk.CollectionConverters.*

case class InvoiceCreate(
  distance: Int,
  date: LocalDate,
  name: String,
  drivers: Set[String]
)

object InvoiceCreate {
  implicit val encoder: JsonEncoder[InvoiceCreate] =
    DeriveJsonEncoder.gen[InvoiceCreate]
  implicit val decoder: JsonDecoder[InvoiceCreate] =
    DeriveJsonDecoder.gen[InvoiceCreate]
}
