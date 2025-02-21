package domain.services.ia.edgedb.models

import com.edgedb.driver.annotations.{EdgeDBDeserializer, EdgeDBLinkType, EdgeDBType}
import java.util.UUID

@EdgeDBType
case class ChatSessionEdge @EdgeDBDeserializer() (var title: String, var id :UUID)
