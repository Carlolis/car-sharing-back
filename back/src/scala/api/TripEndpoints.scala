package api

import domain.models.*
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import zio.json.*

import java.util.UUID

case class ErrorResponse(message: String) derives JsonEncoder, JsonDecoder

object TripEndpoints:

  // val registerEndpoint = endpoint.post
  //   .in("api" / "register")
  //   .in(jsonBody[UserCreate])
  //   .out(jsonBody[Person])
  //   .errorOut(statusCode and jsonBody[ErrorResponse])
  val loginEndpoint: Endpoint[Unit, PersonCreate, (StatusCode, ErrorResponse), Token, Any] = endpoint
    .post
    .in("api" / "login")
    .in(jsonBody[PersonCreate])
    .out(jsonBody[Token])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val createTripEndpoint: Endpoint[Unit, (String, TripCreate), (StatusCode, ErrorResponse), UUID, Any] = endpoint
    .post
    .in("api" / "trips")
    .in(auth.bearer[String]())
    .in(jsonBody[TripCreate])
    .out(jsonBody[UUID])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val updateTripEndpoint: Endpoint[Unit, (String, Trip), (StatusCode, ErrorResponse), UUID, Any] = endpoint
    .put
    .in("api" / "trips")
    .in(auth.bearer[String]())
    .in(jsonBody[Trip])
    .out(jsonBody[UUID])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val getAllTripsEndpoint = endpoint
    .get
    .in("api" / "trips")
    /* .in(auth.bearer[String]())*/
    .out(jsonBody[List[Trip]])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val getTotalStatsEndpoint = endpoint
    .get
    .in("api" / "trips" / "total")
    .in(queryParams)
    .out(jsonBody[TripStats])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val createPersonEndpoint = endpoint
    .post
    .in("api" / "user")
    .in(auth.bearer[String]())
    .in(jsonBody[PersonCreate])
    .out(jsonBody[UUID])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val healthCheck = endpoint
    .get
    .in("api" / "health")

  val tripEndPoints = List(
    loginEndpoint,
    createTripEndpoint,
    getAllTripsEndpoint,
    getTotalStatsEndpoint,
    createPersonEndpoint,
    updateTripEndpoint
  )
