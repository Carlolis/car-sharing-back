package domain.models.trip

import zio.json.*

import java.time.LocalDate
import scala.jdk.CollectionConverters.*

case class TripCreate(
  distance: Int,
  startDate: LocalDate,
  endDate: LocalDate,
  name: String,
  drivers: Set[String]
)

object TripCreate {
  implicit val encoder: JsonEncoder[TripCreate] =
    DeriveJsonEncoder.gen[TripCreate]
  implicit val decoder: JsonDecoder[TripCreate] =
    DeriveJsonDecoder.gen[TripCreate]
}
