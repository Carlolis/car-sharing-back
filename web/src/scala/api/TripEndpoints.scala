package api

import domain.models.*
import sttp.model.{QueryParams, StatusCode}
import sttp.tapir.Endpoint
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import zio.json.*

import java.util.UUID

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

  val createTripEndpoint: Endpoint[Unit, (String, TripCreate), (StatusCode, ErrorResponse), TripId, Any] = endpoint
    .post
    .in("api" / "trips")
    .in(auth.bearer[String]())
    .in(jsonBody[TripCreate])
    .out(jsonBody[TripId])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val updateTripEndpoint: Endpoint[Unit, (String, Trip), (StatusCode, ErrorResponse), UUID, Any] = endpoint
    .put
    .in("api" / "trips")
    .in(auth.bearer[String]())
    .in(jsonBody[Trip])
    .out(jsonBody[UUID])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val getAllTripsEndpoint: Endpoint[Unit, Unit, (StatusCode, ErrorResponse), List[Trip], Any] = endpoint
    .get
    .in("api" / "trips")
    /* .in(auth.bearer[String]())*/
    .out(jsonBody[List[Trip]])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val getTripStatsByUser: Endpoint[Unit, QueryParams, (StatusCode, ErrorResponse), TripStats, Any] = endpoint
    .get
    .in("api" / "trips" / "total")
    .in(queryParams)
    .out(jsonBody[TripStats])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val deleteTripEndpoint: Endpoint[Unit, TripId, (StatusCode, ErrorResponse), TripId, Any] = endpoint
    .delete
    .in("api" / "trips" / path[TripId] / "delete")
    .out(jsonBody[TripId])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val createPersonEndpoint: Endpoint[Unit, (String, PersonCreate), (StatusCode, ErrorResponse), UUID, Any] = endpoint
    .post
    .in("api" / "user")
    .in(auth.bearer[String]())
    .in(jsonBody[PersonCreate])
    .out(jsonBody[UUID])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val healthCheck: Endpoint[Unit, Unit, Unit, Unit, Any] = endpoint
    .get
    .in("api" / "health")

  val tripEndPoints = List(
    loginEndpoint,
    createTripEndpoint,
    getAllTripsEndpoint,
    getTripStatsByUser,
    createPersonEndpoint,
    updateTripEndpoint,
    deleteTripEndpoint
  )
