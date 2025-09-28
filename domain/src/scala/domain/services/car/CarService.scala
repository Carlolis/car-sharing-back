package domain.services.car

import domain.models.car.{Car, CarId}
import zio.*

trait CarService {
  def getCar: Task[Car]
  def updateCar(tripUpdate: Car): Task[CarId]
}

object CarService:
  def getCar: RIO[CarService, Car] =
    ZIO.serviceWithZIO[CarService](_.getCar)

  def updateCar(trip: Car): RIO[CarService, CarId] =
    ZIO.serviceWithZIO[CarService](_.updateCar(trip))
