package domain.models.ia

import zio.json.*
import java.util.UUID
import domain.services.ia.edgedb.models.ChatSessionEdge

case class ChatSession(
  name: String,
  id:UUID
) 

object ChatSession {
  implicit val encoder: JsonEncoder[ChatSession] = DeriveJsonEncoder.gen[ChatSession]
  implicit val decoder: JsonDecoder[ChatSession] = DeriveJsonDecoder.gen[ChatSession]

  def fromChatSessionEdge(chatSessionEdge: ChatSessionEdge): ChatSession = {
    ChatSession(chatSessionEdge.title, chatSessionEdge.id)
  }
}
