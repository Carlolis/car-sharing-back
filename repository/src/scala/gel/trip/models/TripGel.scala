package gel.trip.models

import com.geldata.driver.annotations.{GelDeserializer, GelLinkType, GelType}
import domain.models.PersonCreate
import domain.models.trip.{Trip, TripId}
import gel.person.models.PersonCreateGel

import java.time.LocalDate
import java.util
import java.util.UUID
import scala.jdk.CollectionConverters.*

@GelType
class TripGel @GelDeserializer() (
  id: UUID,
  distance: Int,
  startDate: LocalDate,
  endDate: LocalDate,
  name: String,
  @GelLinkType(classOf[PersonCreateGel])
  gelDrivers: util.Collection[PersonCreateGel]
) {
  def getId: UUID                                  = id
  def getDistance: Int                             = distance
  def getStartDate: LocalDate                      = startDate
  def getEndDate: LocalDate                        = endDate
  def getName: String                              = name
  def getDrivers: util.Collection[PersonCreateGel] = gelDrivers
}

object TripGel {
  def fromTripGel(tripGel: TripGel): Trip =
    Trip(
      TripId(tripGel.getId),
      tripGel.getDistance,
      tripGel.getStartDate,
      tripGel.getEndDate,
      tripGel.getName,
      tripGel.getDrivers.asScala.map(_.name).toSet
    )
}

case class TripGelCreate(
  distance: Double,
  startDate: LocalDate,
  endDate: LocalDate,
  name: String,
  drivers: util.Collection[PersonCreate]
)

case class TripGelStats(
  trips: util.Collection[Trip],
  totalKilometers: Double
)
