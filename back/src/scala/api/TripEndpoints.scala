package api

import domain.models.{PersonCreate, TripCreate, TripStats}
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import zio.json.*

import java.util.UUID

object TripEndpoints:
  case class ErrorResponse(message: String) derives JsonEncoder, JsonDecoder

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

  val getUserTripsEndpoint: Endpoint[Unit, String, (StatusCode, ErrorResponse), TripStats, Any] = endpoint
    .get
    .in("api" / "trips" / "user")
    .in(auth.bearer[String]())
    .out(jsonBody[TripStats])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val getTotalStatsEndpoint = endpoint
    .get
    .in("api" / "trips" / "total")
    .out(jsonBody[TripStats])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val createPersonEndpoint = endpoint
    .post
    .in("api" / "user")
    .in(auth.bearer[String]())
    .in(jsonBody[PersonCreate])
    .out(jsonBody[UUID])
    .errorOut(statusCode and jsonBody[ErrorResponse])
