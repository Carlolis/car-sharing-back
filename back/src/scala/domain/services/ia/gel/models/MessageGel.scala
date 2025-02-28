package domain.services.ia.gel.models

import com.geldata.driver.annotations.{GelDeserializer, GelLinkType, GelType}

import java.util.UUID

@GelType
case class MessageGel @GelDeserializer()(question: String, answer :String)
