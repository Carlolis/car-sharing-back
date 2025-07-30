package gel.ia.models

import com.geldata.driver.annotations.{GelDeserializer, GelType}
import domain.models.ia.Message

@GelType
case class MessageGel @GelDeserializer() (question: String, answer: String)

object MessageGel {
  def fromMessageGel(chatSessionGel: MessageGel): Message =
    Message(chatSessionGel.question, chatSessionGel.answer)
}
