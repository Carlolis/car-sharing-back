package domain.services.trip.edgedb

import adapters.EdgeDbDriverLive
import domain.models.*
import domain.services.trip.TripService
import domain.services.trip.edgedb.models.TripEdge
import zio.*

import java.util.UUID

case class TripServiceEdgeDb(edgeDb: EdgeDbDriverLive) extends TripService {
  // TODO: Implement actual database storage
  private val trips: List[Trip] = List.empty
  private val knownPersons      =
    Set(PersonCreate("Maé"), PersonCreate("Brigitte"), PersonCreate("Charles"))

  override def createTrip(
    tripCreate: TripCreate
  ): Task[UUID] =
    edgeDb
      .querySingle(
        classOf[UUID],
        s"""
          |  with new_trip := (insert TripEdge { name := '${tripCreate.name}', distance := ${tripCreate.distance}, date := cal::to_local_date(${tripCreate
            .date.getYear}, ${tripCreate
            .date.getMonthValue}, ${tripCreate
            .date.getDayOfMonth}), edgeDrivers := (select detached default::PersonEdge filter .name in ${tripCreate
            .drivers.mkString("{'", "','", "'}")}) }) select new_trip.id;
          |"""
      ).tapBoth(error => ZIO.logError(s"Created trip with id: $error"), UUID => ZIO.logInfo(s"Created trip with id: $UUID"))

  override def getUserTrips(personName: String): Task[TripStats] =
    edgeDb
      .query(
        classOf[TripEdge],
        s"""
          | select TripEdge { id, distance, date, name, edgeDrivers: { name } } filter .edgeDrivers.name = <str>'$personName'  ;
          |"""
      )
      .map { tripEdge =>

        val trips   = tripEdge.map(Trip.fromTripEdge)
        val totalKm = trips.map(_.distance).sum

        TripStats(trips, totalKm)
      }

  override def getTotalStats: Task[TripStats] = ZIO.succeed(TripStats(List.empty, 0))

  override def deleteTrip(id: UUID): Task[UUID] =
    edgeDb
      .querySingle(
        classOf[String],
        s"""
           | delete TripEdge filter .id = <uuid>'$id';
           | select '$id';
           |"""
      )
      .map(id => UUID.fromString(id)).zipLeft(ZIO.logInfo(s"Deleted trip with id: $id"))

  override def updateTrip(tripUpdate: Trip): Task[UUID] = ???
}

object TripServiceEdgeDb:
  val layer: ZLayer[EdgeDbDriverLive, Nothing, TripService] =
    ZLayer.fromFunction(TripServiceEdgeDb(_))
