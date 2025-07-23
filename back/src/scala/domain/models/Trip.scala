package domain.models

import domain.services.trip.gel.models.TripGel
import sttp.tapir.Schema
import zio.json.*

import java.time.LocalDate
import scala.jdk.CollectionConverters.*

case class Trip(
  id: TripId,
  distance: Int,
  startDate: LocalDate,
  endDate: LocalDate,
  name: String,
  drivers: Set[String]
)

object Trip {
  implicit val encoder: JsonEncoder[Trip] = DeriveJsonEncoder.gen[Trip]
  implicit val decoder: JsonDecoder[Trip] = DeriveJsonDecoder.gen[Trip]
  def fromTripGel(tripGel: TripGel): Trip =
    Trip(
      TripId(tripGel.getId),
      tripGel.getDistance,
      tripGel.getStartDate,
      tripGel.getEndDate,
      tripGel.getName,
      tripGel.getDrivers.asScala.map(_.name).toSet
    )

  given Schema[Trip] = Schema.derived[Trip]
}
