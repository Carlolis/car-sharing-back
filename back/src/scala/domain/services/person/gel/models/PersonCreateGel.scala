package domain.services.person.gel.models

import com.geldata.driver.annotations.{GelDeserializer, GelLinkType, GelType}

@GelType
case class PersonCreateGel @GelDeserializer()(var name: String)

@GelType
case class PersonGel @GelDeserializer() (var name: String, var id: java.util.UUID)
