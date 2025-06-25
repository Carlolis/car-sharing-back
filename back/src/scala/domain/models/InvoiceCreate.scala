package domain.models

import domain.services.trip.gel.models.InvoiceGel
import zio.json.*

import java.time.LocalDate
import java.util.UUID
import scala.collection.JavaConverters.*



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


