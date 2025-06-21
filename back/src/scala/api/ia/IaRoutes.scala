package api.ia

import api.ia.IaEndpoints.ErrorResponse
import domain.services.ia.IAService
import sttp.model.StatusCode
import sttp.tapir.ztapir.*
import zio.*

val createChat: ZServerEndpoint[IAService, Any] =
  IaEndpoints.createChat.serverLogic { createChatSession =>
    (for {
      uuid <- IAService.createChatSession(createChatSession.writerId, createChatSession.name)

    } yield uuid)
      .map(Right(_))
      .tapError(error => ZIO.logError(s"Error: $error"))
      .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
  }

val addMessageToChat: ZServerEndpoint[IAService, Any] =
  IaEndpoints.addMessageToChat.serverLogic { addMessageToChat =>
    (for {
      uuid <- IAService.addMessageToChat(addMessageToChat.chatUuid, addMessageToChat.message)

    } yield uuid)
      .map(Right(_))
      .tapError(error => ZIO.logError(s"Error: $error"))
      .catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
  }

object IaRoutes:
  val iaEndpoints: List[ZServerEndpoint[IAService, Any]] =
    List(createChat, addMessageToChat)
