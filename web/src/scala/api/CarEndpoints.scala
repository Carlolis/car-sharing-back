package api

import domain.models.*
import domain.models.car.{Car, CarId}
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import zio.json.*

object CarEndpoints:
  val updateCar: Endpoint[Unit, (String, Car), (StatusCode, ErrorResponse), CarId, Any] = endpoint
    .put
    .in("api" / "car")
    .in(auth.bearer[String]())
    .in(jsonBody[Car])
    .out(jsonBody[CarId])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val getCar: Endpoint[Unit, String, (StatusCode, ErrorResponse), Car, Any] = endpoint
    .get
    .in("api" / "car")
    .in(auth.bearer[String]())
    .out(jsonBody[Car])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val carEndPoints = List(
    getCar,
    updateCar
  )
