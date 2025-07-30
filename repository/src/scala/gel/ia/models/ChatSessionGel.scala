package gel.ia.models

import com.geldata.driver.annotations.{GelDeserializer, GelLinkType, GelType}

import java.util
import java.util.UUID

@GelType
case class ChatSessionGel @GelDeserializer() (
  var title: String,
  var id: UUID,
  @GelLinkType(classOf[MessageGel])
  messages: util.Collection[MessageGel])
