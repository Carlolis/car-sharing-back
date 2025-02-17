package domain.services.ia.edgedb.models

import com.edgedb.driver.annotations.{EdgeDBDeserializer, EdgeDBLinkType, EdgeDBType}

@EdgeDBType
case class WriterCreateEdge @EdgeDBDeserializer() (var name: String)

@EdgeDBType
case class WriterEdge @EdgeDBDeserializer() (var name: String, var id: java.util.UUID)
