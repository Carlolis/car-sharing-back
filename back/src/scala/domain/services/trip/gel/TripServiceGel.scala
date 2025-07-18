package domain.services.trip.gel

import adapters.GelDriverLive
import domain.models.*
import domain.services.trip.TripService
import domain.services.trip.gel.models.{TripGel, TripStatsEdge}
import zio.*

import java.util.UUID

case class TripServiceGel(gelDb: GelDriverLive) extends TripService {
  // TODO: Implement actual database storage
  private val trips: List[Trip] = List.empty
  private val knownPersons      =
    Set(PersonCreate("Maé"), PersonCreate("Brigitte"), PersonCreate("Charles"))

  override def createTrip(
    tripCreate: TripCreate
  ): Task[UUID] =
    gelDb
      .querySingle(
        classOf[UUID],
        s"""
          |  with new_trip := (insert TripGel { name := '${tripCreate.name}', distance := ${tripCreate.distance}, date := cal::to_local_date(${tripCreate
            .date.getYear}, ${tripCreate
            .date.getMonthValue}, ${tripCreate
            .date.getDayOfMonth}), gelDrivers := (select detached default::PersonGel filter .name in ${tripCreate
            .drivers.mkString("{'", "','", "'}")}) }) select new_trip.id;
          |"""
      ).tapBoth(error => ZIO.logError(s"Created trip with id: $error"), UUID => ZIO.logInfo(s"Created trip with id: $UUID"))

  override def getAllTrips: Task[List[Trip]] =
    gelDb
      .query(
        classOf[TripGel],
        s"""
          | select TripGel { id, distance, date, name, gelDrivers: { name } }
          |   order by
          |  .date desc then
          |  .id desc;;
          |"""
      )
      .map(_.map(Trip.fromTripGel))

  override def getTripStatsByUser(username: String): Task[TripStats] =
    gelDb
      .querySingle(
        classOf[TripStatsEdge],
        s"""
         |  select {
         |  totalKilometers := sum(
         |    (select TripGel { distance, gelDrivers: { name }  } filter '$username' in .gelDrivers.name
         |    ).distance
         |  )
         |};
         |"""
      )
      .map(tripGel => TripStats(tripGel.getTotalKilometers)).tap(t => ZIO.logInfo(t.totalKilometers.toString()))

  override def deleteTrip(id: UUID): Task[UUID] =
    gelDb
      .querySingle(
        classOf[String],
        s"""
           | delete TripGel filter .id = <uuid>'$id';
           | select '$id';
           |"""
      )
      .map(id => UUID.fromString(id)).zipLeft(ZIO.logInfo(s"Deleted trip with id: $id"))

  override def updateTrip(tripUpdate: Trip): Task[UUID] =
    gelDb
      .querySingle(
        classOf[UUID],
        s"""
           | with updated_trip := (
           |    update TripGel
           |    filter .id = <uuid>'${tripUpdate.id}'
           |    set {
           |        name := '${tripUpdate.name}',
           |        distance := ${tripUpdate.distance},
           |        date := cal::to_local_date(${tripUpdate
            .date.getYear}, ${tripUpdate.date.getMonthValue}, ${tripUpdate.date.getDayOfMonth}),
           |        gelDrivers := (select detached default::PersonGel filter .name in ${tripUpdate.drivers.mkString("{'", "','", "'}")})
           |    }
           |)
           |select updated_trip.id;
           |"""
      ).tapBoth(
        error => ZIO.logError(s"Failed to update trip with id: ${tripUpdate.id}, error: $error"),
        uuid => ZIO.logInfo(s"Updated trip with id: $uuid")
      )
}

object TripServiceGel:
  val layer: ZLayer[GelDriverLive, Nothing, TripService] =
    ZLayer.fromFunction(TripServiceGel(_))
