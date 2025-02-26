package domain.services.ia.gel.models

import com.geldata.driver.annotations.{GelDeserializer, GelLinkType, GelType}

@GelType
case class WriterCreateGel @GelDeserializer() (var name: String)

@GelType
case class WriterGel @GelDeserializer() (var name: String, var id: java.util.UUID)
