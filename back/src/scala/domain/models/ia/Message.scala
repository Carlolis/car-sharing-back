package domain.models.ia

import domain.services.ia.gel.models.MessageGel
import zio.json.*

import java.util.UUID

case class Message(
                    question: String, answer :String
) 

object Message {
  implicit val encoder: JsonEncoder[Message] = DeriveJsonEncoder.gen[Message]
  implicit val decoder: JsonDecoder[Message] = DeriveJsonDecoder.gen[Message]

  def fromMessageGel(chatSessionGel: MessageGel): Message = {
    Message(chatSessionGel.question, chatSessionGel.answer)
  }
}
