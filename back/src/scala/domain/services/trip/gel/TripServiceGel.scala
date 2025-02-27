package domain.services.trip.gel

import adapters.GelDriverLive
import domain.models.*
import domain.services.trip.TripService
import domain.services.trip.gel.models.TripGel
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

  override def getUserTrips(personName: String): Task[TripStats] =
    gelDb
      .query(
        classOf[TripGel],
        s"""
          | select TripGel { id, distance, date, name, gelDrivers: { name } } filter .gelDrivers.name = <str>'$personName'  ;
          |"""
      )
      .map { tripGel =>

        val trips   = tripGel.map(Trip.fromTripGel)
        val totalKm = trips.map(_.distance).sum

        TripStats(trips, totalKm)
      }

  override def getTotalStats: Task[TripStats] = ZIO.succeed(TripStats(List.empty, 0))

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

  override def updateTrip(tripUpdate: Trip): Task[UUID] = ???
}

object TripServiceGel:
  val layer: ZLayer[GelDriverLive, Nothing, TripService] =
    ZLayer.fromFunction(TripServiceGel(_))
