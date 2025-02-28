package domain.models.ia

import zio.json.*
import java.util.UUID
import domain.services.ia.gel.models.ChatSessionGel
import scala.collection.JavaConverters.*

case class ChatSession(
  name: String,
  id:UUID,
  messages: Set[Message]
) 

object ChatSession {
  implicit val encoder: JsonEncoder[ChatSession] = DeriveJsonEncoder.gen[ChatSession]
  implicit val decoder: JsonDecoder[ChatSession] = DeriveJsonDecoder.gen[ChatSession]

  def fromChatSessionGel(chatSessionGel: ChatSessionGel): ChatSession = {
    ChatSession(chatSessionGel.title, chatSessionGel.id,chatSessionGel.messages.asScala.map(Message.fromMessageGel).toSet)
  }
}
