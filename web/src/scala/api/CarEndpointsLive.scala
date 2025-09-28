package api

import domain.services.AuthService
import domain.services.person.PersonService
import domain.services.car.CarService
import sttp.model.StatusCode
import sttp.tapir.ztapir.*
import zio.*

object CarEndpointsLive:




  private val updateCar: ZServerEndpoint[AuthService & CarService, Any] =
    CarEndpoints.updateCar.serverLogic {
      case (token, car) =>
        (for {
          _    <- AuthService.authenticate(token)
          _    <- ZIO.logInfo("Updating car " + car.toString)
          uuid <- CarService.updateCar(car)
          _    <- ZIO.logInfo("Car updated " + uuid.toString)
        } yield uuid)
          .map(Right(_))
          .tapError(error => ZIO.logError(s"Error: $error"))
          .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
    }

  private val getCar: ZServerEndpoint[AuthService & CarService, Any] =
    CarEndpoints.getCar.serverLogic { token =>
      (for {

        _      <- AuthService.authenticate(token)
        _      <- ZIO.logInfo(
                    "Getting cars "
                  )
        result <- CarService.getCar
        _      <- ZIO.logInfo(
                    "Cars found " + result.toString
                  )
      } yield result)
        .map(Right(_))
        .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
    }

 

  val carEndpointsLive: List[ZServerEndpoint[AuthService & CarService, Any]] =
    List( getCar,   updateCar)
