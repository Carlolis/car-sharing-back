package gel.trip

import adapters.GelDriverLive
import domain.models.PersonCreate
import domain.models.trip.{Trip, TripCreate, TripId, TripStats}
import domain.services.trip.TripService
import gel.trip.models.*
import zio.*

import java.util.UUID

case class TripRepositoryGel(gelDb: GelDriverLive) extends TripService {
  private val trips: List[Trip] = List.empty
  private val knownPersons      =
    Set(PersonCreate("maé"), PersonCreate("brigitte"), PersonCreate("charles"))

  override def createTrip(
    tripCreate: TripCreate
  ): Task[TripId] =
    gelDb
      .querySingle(
        classOf[UUID],
        s"""
          |  with new_trip := (insert TripGel { name := '${tripCreate.name}',
          |   ${tripCreate.distance.map(d => s"distance := $d,").getOrElse("")}
          |   ${if tripCreate.comments.isDefined then s"comments := '${tripCreate.comments.get.replace("'", "\\'")}'," else ""}
          |    startDate := cal::to_local_date(${tripCreate
            .startDate.getYear}, ${tripCreate
            .startDate.getMonthValue}, ${tripCreate
            .startDate.getDayOfMonth}),
            | endDate := cal::to_local_date(${tripCreate
            .endDate.getYear}, ${tripCreate
            .endDate.getMonthValue}, ${tripCreate
            .endDate.getDayOfMonth}),
            | gelDrivers := (select detached default::PersonGel filter .name in ${tripCreate
            .drivers.mkString("{'", "','", "'}")}) }) select new_trip.id;
          |"""
      ).tapBoth(error => ZIO.logError(s"Created trip with id: $error"), UUID => ZIO.logInfo(s"Created trip with id: $UUID")).map(TripId(_))

  override def getAllTrips: Task[List[Trip]] =
    gelDb
      .query(
        classOf[TripGel],
        s"""
          | select TripGel { id, distance, startDate, endDate, name, comments, gelDrivers: { name } }
          |   order by
          |  .startDate desc then
          |  .id desc;;
          |"""
      )
      .map(_.map(TripGel.fromTripGel))

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
      .map(tripGel => TripStats(tripGel.getTotalKilometers.toInt)).tap(t => ZIO.logInfo(t.totalKilometers.toString))

  override def deleteTrip(id: TripId): Task[TripId] =
    gelDb
      .querySingle(
        classOf[String],
        s"""
           | delete TripGel filter .id = <uuid>'$id';
           | select '$id';
           |"""
      )
      .map(id => UUID.fromString(id)).zipLeft(ZIO.logInfo(s"Deleted trip with id: $id")).map(TripId(_))

  override def updateTrip(tripUpdate: Trip): Task[TripId] =
    gelDb
      .querySingle(
        classOf[UUID],
        s"""
           | with updated_trip := (
           |    update TripGel
           |    filter .id = <uuid>'${tripUpdate.id}'
           |    set {
           |        name := '${tripUpdate.name}',
           |        ${tripUpdate.distance.map(d => s"distance := $d,").getOrElse("")}
           |        ${if tripUpdate.comments.isDefined then s"comments := '${tripUpdate.comments.get.replace("'", "\\'")}'," else ""}
           |        startDate := cal::to_local_date(${tripUpdate
            .startDate.getYear}, ${tripUpdate
            .startDate.getMonthValue}, ${tripUpdate.startDate.getDayOfMonth}),endDate := cal::to_local_date(${tripUpdate
            .endDate.getYear}, ${tripUpdate.endDate.getMonthValue}, ${tripUpdate.endDate.getDayOfMonth}),
           |        gelDrivers := (select detached default::PersonGel filter .name in ${tripUpdate.drivers.mkString("{'", "','", "'}")})
           |    }
           |)
           |select updated_trip.id;
           |"""
      ).map(TripId(_)).tapBoth(
        error => ZIO.logError(s"Failed to update trip with id: ${tripUpdate.id}, error: $error"),
        uuid => ZIO.logInfo(s"Updated trip with id: $uuid")
      )
}

object TripRepositoryGel:
  val layer: ZLayer[GelDriverLive, Nothing, TripService] =
    ZLayer.fromFunction(TripRepositoryGel(_))
