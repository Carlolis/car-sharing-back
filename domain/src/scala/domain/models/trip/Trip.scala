package domain.models.trip

import sttp.tapir.Schema
import zio.json.*

import java.time.LocalDate
import scala.jdk.CollectionConverters.*

case class Trip(
  id: TripId,
  startDate: LocalDate,
  endDate: LocalDate,
  name: String,
  drivers: Set[String],
  comments: Option[String],
  distance: Int
)

object Trip {
  implicit val encoder: JsonEncoder[Trip] = DeriveJsonEncoder.gen[Trip]
  implicit val decoder: JsonDecoder[Trip] = DeriveJsonDecoder.gen[Trip]

  given Schema[Trip] = Schema.derived[Trip]
}
