package domain.services.trip

import domain.models.{Trip, TripCreate, TripId, TripStats}
import zio.*

import java.util.UUID

trait TripService {
  def createTrip(tripCreate: TripCreate): Task[TripId]
  def getAllTrips: Task[List[Trip]]
  def getTripStatsByUser(username: String): Task[TripStats]
  def deleteTrip(id: TripId): Task[TripId]
  def updateTrip(tripUpdate: Trip): Task[UUID]
}

object TripService:
  def createTrip(tripCreate: TripCreate): RIO[TripService, TripId] =
    ZIO.serviceWithZIO[TripService](_.createTrip(tripCreate))

  def getAllTrips: RIO[TripService, List[Trip]] =
    ZIO.serviceWithZIO[TripService](_.getAllTrips)

  def getTripStatsByUser(username: String): RIO[TripService, TripStats] =
    ZIO.serviceWithZIO[TripService](_.getTripStatsByUser(username))

  def deleteTrip(id: TripId): RIO[TripService, TripId] =
    ZIO.serviceWithZIO[TripService](_.deleteTrip(id))

  def updateTrip(trip: Trip): RIO[TripService, UUID] =
    ZIO.serviceWithZIO[TripService](_.updateTrip(trip))
