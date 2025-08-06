package domain.models.ia

import zio.json.*

import java.util.UUID
import scala.jdk.CollectionConverters.*

case class ChatSession(
  name: String,
  id: UUID,
  messages: Set[Message]
)

object ChatSession {
  implicit val encoder: JsonEncoder[ChatSession] = DeriveJsonEncoder.gen[ChatSession]
  implicit val decoder: JsonDecoder[ChatSession] = DeriveJsonDecoder.gen[ChatSession]
}
