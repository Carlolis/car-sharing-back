package gel.ia.models

import com.geldata.driver.annotations.{GelDeserializer, GelType}

@GelType
case class MessageGel @GelDeserializer() (question: String, answer: String)
