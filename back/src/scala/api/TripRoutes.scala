package api

import domain.services.person.PersonService
import domain.services.services.AuthService
import domain.services.trip.TripService
import sttp.model.StatusCode
import sttp.tapir.server.interceptor.cors.CORSConfig.AllowedOrigin
import sttp.tapir.server.interceptor.cors.{CORSConfig, CORSInterceptor}
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.*
import zio.*
import zio.http.{Request, Response, Routes}

object TripRoutes:
  // val register: ZServerEndpoint[Any, Any] =
  //   TripEndpoints.registerEndpoint.serverLogic { userCreate =>
  //     authService
  //       .register(userCreate)
  //       .map(Right(_))
  //       .catchAll(err =>
  //         ZIO.succeed(
  //           Left((StatusCode.BadRequest, ErrorResponse(err.getMessage)))
  //         )
  //       )
  //   }

  // val login: ZServerEndpoint[Any, Any] =
  //   TripEndpoints.loginEndpoint.serverLogic { credentials =>
  //     authService
  //       .login(credentials)
  //       .map(Right(_))
  //       .catchAll(err =>
  //         ZIO.succeed(
  //           Left((StatusCode.Unauthorized, ErrorResponse(err.getMessage)))
  //         )
  //       )
  //   }
  val createTrip: ZServerEndpoint[PersonService & AuthService & TripService, Any] =
    TripEndpoints.createTripEndpoint.serverLogic {
      case (token, tripCreate) =>
        (for {
          _    <- AuthService.authenticate(token)
          uuid <- TripService.createTrip(tripCreate)
        } yield uuid)
          .map(Right(_))
          .tapError(error => ZIO.logError(s"Error: $error"))
          .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
    }

  val getUserTrips: ZServerEndpoint[PersonService & AuthService & TripService, Any] =
    TripEndpoints.getUserTripsEndpoint.serverLogic { token =>
      (for {
        // userOpt <- authService.authenticate(token)
        // user <- ZIO
        //   .fromOption(userOpt)
        //   .orElseFail(new Exception("Unauthorized"))
        result <- TripService.getUserTrips("Maé")
      } yield result)
        .map(Right(_))
        .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
    }

  val getTotalStats: ZServerEndpoint[PersonService & AuthService & TripService, Any]        =
    TripEndpoints.getTotalStatsEndpoint.serverLogic { _ =>
      TripService
        .getTotalStats
        .map(Right(_))
        .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
    }
  val createPersonEndpoint: ZServerEndpoint[PersonService & AuthService & TripService, Any] =
    TripEndpoints.createPersonEndpoint.serverLogic {
      case (token, personCreate) =>
        (for {
          // userOpt <- authService.authenticate(token)
          // user <- ZIO
          //   .fromOption(userOpt)
          //   .orElseFail(new Exception("Unauthorized"))
          uuid <- PersonService.createPerson(personCreate)
        } yield uuid)
          .map(Right(_))
          .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
    }
  val loginEndpoint: ZServerEndpoint[PersonService & AuthService & TripService, Any]        =
    TripEndpoints.loginEndpoint.serverLogic { credentials =>
      AuthService
        .login(credentials.name)
        .tap(token => ZIO.logInfo(s"Login success ! $token"))
        .map(Right(_))
        .catchAll(err => ZIO.left(StatusCode.Unauthorized, ErrorResponse(err.getMessage)))
    }

  val tripEndpoints: List[ZServerEndpoint[PersonService & AuthService & TripService, Any]] =
    List(getTotalStats, getUserTrips, createTrip, loginEndpoint)
  // login,
  // register
