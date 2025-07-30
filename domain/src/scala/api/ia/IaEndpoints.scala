package api.ia

import domain.models.ia.Message
import sttp.tapir.EndpointIO.annotations.jsonbody
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import zio.json.*

import java.util.UUID

@jsonbody
case class CreateChatSession(
  writerId: UUID,
  name: String
)

object CreateChatSession {
  implicit val encoder: JsonEncoder[CreateChatSession] = DeriveJsonEncoder.gen[CreateChatSession]
  implicit val decoder: JsonDecoder[CreateChatSession] = DeriveJsonDecoder.gen[CreateChatSession]
}

@jsonbody
case class AddMessageToChat(
  chatUuid: UUID,
  message: Message
)

object AddMessageToChat {
  implicit val encoder: JsonEncoder[AddMessageToChat] = DeriveJsonEncoder.gen[AddMessageToChat]
  implicit val decoder: JsonDecoder[AddMessageToChat] = DeriveJsonDecoder.gen[AddMessageToChat]
}



object IaEndpoints:
  case class ErrorResponse(message: String) derives JsonEncoder, JsonDecoder

  val createChat = endpoint
    .post
    .in("api" / "ia" / "createChat")
    .in(jsonBody[CreateChatSession])
    .out(jsonBody[UUID])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val addMessageToChat = endpoint
    .post
    .in("api" / "ia" / "addMessage")
    .in(jsonBody[AddMessageToChat])
    .out(jsonBody[UUID])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val iaEndpoints = List(createChat, addMessageToChat)
