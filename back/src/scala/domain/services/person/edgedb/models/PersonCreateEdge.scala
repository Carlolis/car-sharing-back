package domain.services.person.edgedb.models

import com.edgedb.driver.annotations.{EdgeDBDeserializer, EdgeDBLinkType, EdgeDBType}

@EdgeDBType
case class PersonCreateEdge @EdgeDBDeserializer() (var name: String)

@EdgeDBType
case class PersonEdge @EdgeDBDeserializer() (var name: String, var id: java.util.UUID)
