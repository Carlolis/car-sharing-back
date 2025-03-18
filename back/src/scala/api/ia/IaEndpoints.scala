package api.ia

import sttp.tapir.EndpointIO.annotations.jsonbody
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import zio.json.*

import java.util.UUID

@jsonbody
case class CreateCHatSession(
  writerId: UUID,
  name: String
)

object CreateCHatSession {
  implicit val encoder: JsonEncoder[CreateCHatSession] = DeriveJsonEncoder.gen[CreateCHatSession]
  implicit val decoder: JsonDecoder[CreateCHatSession] = DeriveJsonDecoder.gen[CreateCHatSession]
}

object IaEndpoints:
  case class ErrorResponse(message: String) derives JsonEncoder, JsonDecoder

  val createChat = endpoint
    .post
    .in("api" / "createChat")
    .in(jsonBody[CreateCHatSession])
    .out(jsonBody[UUID])
    .errorOut(statusCode and jsonBody[ErrorResponse])

  val iaEndpoints = List(createChat)
