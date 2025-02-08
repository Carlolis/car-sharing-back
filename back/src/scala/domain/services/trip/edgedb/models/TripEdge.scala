package domain.services.trip.edgedb.models

import com.edgedb.driver.annotations.{EdgeDBDeserializer, EdgeDBLinkType, EdgeDBType}
import domain.models.{PersonCreate, Trip}
import domain.services.person.edgedb.models.PersonCreateEdge
import zio.json.*

import java.time.LocalDate
import java.util
import java.util.UUID

@EdgeDBType
class TripEdge @EdgeDBDeserializer() (
  id: UUID,
  distance: Int,
  date: LocalDate,
  name: String,
  @EdgeDBLinkType(classOf[PersonCreateEdge])
  edgeDrivers: util.Collection[PersonCreateEdge]
) {
  def getId: UUID                                   = id
  def getDistance: Int                              = distance
  def getDate: LocalDate                            = date
  def getName: String                               = name
  def getDrivers: util.Collection[PersonCreateEdge] = edgeDrivers
}

case class TripEdgeCreate(
  distance: Double,
  date: LocalDate,
  name: String,
  drivers: util.Collection[PersonCreate]
)

case class TripEdgeStats(
  trips: util.Collection[Trip],
  totalKilometers: Double
)
