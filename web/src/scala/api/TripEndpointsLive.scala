package api

import domain.services.AuthService
import domain.services.person.PersonService
import domain.services.trip.TripService
import sttp.model.StatusCode
import sttp.tapir.ztapir.*
import zio.*

object TripEndpointsLive:
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
  private val createTrip: ZServerEndpoint[PersonService & AuthService & TripService, Any] =
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

  private val updateTrip: ZServerEndpoint[PersonService & AuthService & TripService, Any] =
    TripEndpoints.updateTripEndpoint.serverLogic {
      case (token, trip) =>
        (for {
          _    <- AuthService.authenticate(token)
          _    <- ZIO.logInfo("Updating trip " + trip.toString)
          uuid <- TripService.updateTrip(trip)
          _    <- ZIO.logInfo("Trip updated " + uuid.toString)
        } yield uuid)
          .map(Right(_))
          .tapError(error => ZIO.logError(s"Error: $error"))
          .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
    }

  private val getAllTrips: ZServerEndpoint[PersonService & AuthService & TripService, Any] =
    TripEndpoints.getAllTripsEndpoint.serverLogic { token =>
      (for {
        // userOpt <- authService.authenticate(token)
        // user <- ZIO
        //   .fromOption(userOpt)
        //   .orElseFail(new Exception("Unauthorized"))
        _      <- ZIO.logInfo(
                    "Getting trips "
                  )
        result <- TripService.getAllTrips
        _      <- ZIO.logInfo(
                    "Trips found " + result.toString
                  )
      } yield result)
        .map(Right(_))
        .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
    }

  private val getTripStatsByUser: ZServerEndpoint[PersonService & AuthService & TripService, Any] =
    TripEndpoints.getTripStatsByUser.serverLogic { username =>
      username
        .get("username").map(username =>
          TripService
            .getTripStatsByUser(username)
            .map(Right(_))
            .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))).getOrElse(
          ZIO.left(StatusCode.BadRequest, ErrorResponse("No username in query params")))
    }

  private val deleteTripEndpoint: ZServerEndpoint[PersonService & AuthService & TripService, Any] =
    TripEndpoints.deleteTripEndpoint.serverLogic { tripId =>
      TripService
        .deleteTrip(tripId)
        .map(Right(_))
        .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
    }

  private val createPersonEndpoint: ZServerEndpoint[PersonService & AuthService & TripService, Any] =
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
  private val loginEndpoint: ZServerEndpoint[PersonService & AuthService & TripService, Any]        =
    TripEndpoints.loginEndpoint.serverLogic { credentials =>
      AuthService
        .login(credentials.name)
        .tap(token => ZIO.logInfo(s"Login success ! $token"))
        .map(Right(_))
        .catchAll(err => ZIO.left(StatusCode.Unauthorized, ErrorResponse(err.getMessage)))
    }

  private val healthEndpoint: ZServerEndpoint[PersonService & AuthService & TripService, Any] =
    TripEndpoints.healthCheck.serverLogic(_ => ZIO.right(Right(())))

  val tripEndpoints: List[ZServerEndpoint[PersonService & AuthService & TripService, Any]] =
    List(getTripStatsByUser, getAllTrips, createTrip, loginEndpoint, updateTrip, createPersonEndpoint, healthEndpoint, deleteTripEndpoint)
  // login,
  // register
