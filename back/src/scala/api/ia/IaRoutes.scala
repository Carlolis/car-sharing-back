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

class IaRoutes(iaService: IAService):
  val createChat: ZServerEndpoint[Any, Any] =
    IaEndpoints.createChat.serverLogic { token =>
      (for {
        uuid <- iaService.createChatSession(token.writerId, token.name)

      } yield uuid)
        .map(Right(_))
        .tapError(error => ZIO.logError(s"Error: $error"))
        .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
    }

  def docsEndpoints(
    apiEndpoints: List[ZServerEndpoint[Any, Any]]
  ): List[ZServerEndpoint[Any, Any]] = SwaggerInterpreter()
    .fromServerEndpoints[Task](apiEndpoints, "car-sharing", "0.1.0")

  val endpoints: List[ZServerEndpoint[Any, Any]] = {
    val all = List(
      createChat
      // login,
      // register
    )
    all ++ docsEndpoints(all)
  }
object IaRoutes:
  val live: ZLayer[IAService, Nothing, IaRoutes] =
    ZLayer.fromFunction(new IaRoutes(_))
