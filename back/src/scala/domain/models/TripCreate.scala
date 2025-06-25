package domain.models

import domain.services.trip.gel.models.TripGel
import zio.json.*

import java.time.LocalDate
import java.util.UUID
import scala.collection.JavaConverters.*


case class TripCreate(
  distance: Int,
  date: LocalDate,
  name: String,
  drivers: Set[String]
)

object TripCreate {
  implicit val encoder: JsonEncoder[TripCreate] =
    DeriveJsonEncoder.gen[TripCreate]
  implicit val decoder: JsonDecoder[TripCreate] =
    DeriveJsonDecoder.gen[TripCreate]
}