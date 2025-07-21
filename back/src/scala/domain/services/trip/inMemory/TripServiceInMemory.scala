package domain.services.trip.inMemory

import domain.models.{Trip, TripCreate, TripId, TripStats}
import domain.services.trip.TripService
import zio.{Task, ULayer, ZIO, ZLayer}

import java.util.UUID

case class TripServiceInMemory() extends TripService {
  // TODO: Implement actual database storage
  private var trips: List[Trip] = List.empty

  private val knownPersons =
    Set("Maé", "Brigitte", "Charles")

  override def createTrip(
    tripCreate: TripCreate
  ): Task[TripId] =
    if (!tripCreate.drivers.subsetOf(knownPersons))
      ZIO.fail(new Exception("Unknown person"))
    else
      ZIO
        .succeed {
          val newTrip = Trip(
            id = TripId(UUID.randomUUID()),
            distance = tripCreate.distance,
            date = tripCreate.date,
            name = tripCreate.name,
            drivers = tripCreate.drivers
          )
          trips = trips :+ newTrip
          newTrip
        }.as(TripId(UUID.randomUUID()))

  override def getAllTrips: Task[List[Trip]] =
    ZIO.succeed {
      trips
    }

  override def getTripStatsByUser(username: String): Task[TripStats] =
    ZIO.succeed {
      val totalKm = trips.map(_.distance).sum
      TripStats(totalKm)
    }

  override def deleteTrip(id: TripId): Task[TripId] = ???

  override def updateTrip(tripUpdate: Trip): Task[UUID] = ???
}

object TripServiceInMemory {
  val layer: ULayer[TripService] = ZLayer.succeed(TripServiceInMemory())
}
