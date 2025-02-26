package domain.services.trip.gel.models

import com.geldata.driver.annotations.{GelDeserializer, GelLinkType, GelType}
import domain.models.{PersonCreate, Trip}
import domain.services.person.gel.models.PersonCreateGel
import zio.json.*

import java.time.LocalDate
import java.util
import java.util.UUID

@GelType
class TripGel @GelDeserializer() (
  id: UUID,
  distance: Int,
  date: LocalDate,
  name: String,
  @GelLinkType(classOf[PersonCreateGel])
  edgeDrivers: util.Collection[PersonCreateGel]
) {
  def getId: UUID                                   = id
  def getDistance: Int                              = distance
  def getDate: LocalDate                            = date
  def getName: String                               = name
  def getDrivers: util.Collection[PersonCreateGel] = edgeDrivers
}

case class TripGelCreate(
  distance: Double,
  date: LocalDate,
  name: String,
  drivers: util.Collection[PersonCreate]
)

case class TripGelStats(
  trips: util.Collection[Trip],
  totalKilometers: Double
)
