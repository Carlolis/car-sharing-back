package domain.services.trip.gel.models

import com.geldata.driver.annotations.{GelDeserializer, GelLinkType, GelType}
import domain.models.{PersonCreate, Trip}
import domain.services.person.gel.models.PersonCreateGel

import java.time.LocalDate
import java.util
import java.util.UUID

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
