package api.ia

import api.TripEndpoints.*
import api.ia.IaEndpoints.ErrorResponse
import domain.services.ia.IAService
import domain.services.person.PersonService
import domain.services.services.AuthService
import domain.services.trip.TripService
import sttp.model.StatusCode
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.*
import zio.*

val createChat: ZServerEndpoint[IAService, Any] =
  IaEndpoints.createChat.serverLogic { token =>
    (for {
      uuid <- IAService.createChatSession(token.writerId, token.name)

    } yield uuid)
      .map(Right(_))
      .tapError(error => ZIO.logError(s"Error: $error"))
      .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
  }

object IaRoutes:
  val iaEndpoints: List[ZServerEndpoint[IAService, Any]] =
    List(createChat)
