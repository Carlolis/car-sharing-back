package gel.car

import adapters.GelDriverLive
import domain.models.PersonCreate
import domain.models.car.{Car, CarId}
import domain.services.car.CarService
import gel.car.models.*
import zio.*

import java.util.UUID

case class CarRepositoryGel(gelDb: GelDriverLive) extends CarService {
  private val cars: List[Car] = List.empty
  private val knownPersons    =
    Set(PersonCreate("maé"), PersonCreate("brigitte"), PersonCreate("charles"))

  override def getCar: Task[Car] =
    gelDb
      .querySingle(
        classOf[CarGel],
        s"""
          | select CarGel { id, name, mileage};
          |"""
      )
      .map(CarGel.toCar)

  override def updateCar(carUpdate: Car): Task[CarId] =
    gelDb
      .querySingle(
        classOf[UUID],
        s"""
           | with updated_car := (
           |    update CarGel
           |    filter .id = <uuid>'${carUpdate.id}'
           |    set {
           |        name := '${carUpdate.name}',
           |        mileage := ${carUpdate.mileage}
           |    }
           |)
           |select updated_car.id;
           |"""
      ).map(CarId(_)).tapBoth(
        error => ZIO.logError(s"Failed to update car with id: ${carUpdate.id}, error: $error"),
        uuid => ZIO.logInfo(s"Updated car with id: $uuid")
      )
}

object CarRepositoryGel:
  val layer: ZLayer[GelDriverLive, Nothing, CarService] =
    ZLayer.fromFunction(CarRepositoryGel(_))
