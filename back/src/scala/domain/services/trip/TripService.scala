package domain.services.trip

import domain.models.{Trip, TripCreate, TripStats}
import zio.*

import java.util.UUID

trait TripService {
  def createTrip(tripCreate: TripCreate): Task[UUID]
  def getAllTrips: Task[TripStats]
  def getTotalStats: Task[TripStats]
  def deleteTrip(id: UUID): Task[UUID]
  def updateTrip(tripUpdate: Trip): Task[UUID]
}

object TripService:
  def createTrip(tripCreate: TripCreate): RIO[TripService, UUID] =
    ZIO.serviceWithZIO[TripService](_.createTrip(tripCreate))

  def getAllTrips: RIO[TripService, TripStats] =
    ZIO.serviceWithZIO[TripService](_.getAllTrips)

  def getTotalStats: RIO[TripService, TripStats] =
    ZIO.serviceWithZIO[TripService](_.getTotalStats)

  def deleteTrip(id: UUID): RIO[TripService, UUID] =
    ZIO.serviceWithZIO[TripService](_.deleteTrip(id))

  def updateTrip(trip: Trip): RIO[TripService, UUID] =
    ZIO.serviceWithZIO[TripService](_.updateTrip(trip))
