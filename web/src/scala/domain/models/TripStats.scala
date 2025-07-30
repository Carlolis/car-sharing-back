package domain.models

import domain.services.trip.gel.models.TripGel
import zio.json.*

import java.time.LocalDate
import java.util.UUID
import scala.jdk.CollectionConverters.*



case class TripStats(
  totalKilometers: Double
)

object TripStats {
  implicit val encoder: JsonEncoder[TripStats] =
    DeriveJsonEncoder.gen[TripStats]
  implicit val decoder: JsonDecoder[TripStats] =
    DeriveJsonDecoder.gen[TripStats]
}
