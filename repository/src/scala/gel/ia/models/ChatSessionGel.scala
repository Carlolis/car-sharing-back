package gel.ia.models

import com.geldata.driver.annotations.{GelDeserializer, GelLinkType, GelType}
import domain.models.ia.{ChatSession, Message}

import java.util
import java.util.UUID
import scala.collection.JavaConverters.collectionAsScalaIterableConverter

@GelType
case class ChatSessionGel @GelDeserializer() (
  var title: String,
  var id: UUID,
  @GelLinkType(classOf[MessageGel])
  messages: util.Collection[MessageGel])

object ChatSessionGel {
  def fromChatSessionGel(chatSessionGel: ChatSessionGel): ChatSession =
    ChatSession(chatSessionGel.title, chatSessionGel.id, chatSessionGel.messages.asScala.map(MessageGel.fromMessageGel).toSet)
}
