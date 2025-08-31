package domain.models.trip

import zio.json.*

import scala.jdk.CollectionConverters.*

case class TripStats(
  totalKilometers: Int
)

object TripStats {
  implicit val encoder: JsonEncoder[TripStats] =
    DeriveJsonEncoder.gen[TripStats]
  implicit val decoder: JsonDecoder[TripStats] =
    DeriveJsonDecoder.gen[TripStats]
}
