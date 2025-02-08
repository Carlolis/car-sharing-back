package domain.models

import domain.services.person.edgedb.models.PersonCreateEdge
import zio.json.*

case class PersonCreate(
  name: String
) {
  def toPersonEdge: PersonCreateEdge =
    PersonCreateEdge(name)
}

object PersonCreate {
  implicit val encoder: JsonEncoder[PersonCreate] = DeriveJsonEncoder.gen[PersonCreate]
  implicit val decoder: JsonDecoder[PersonCreate] = DeriveJsonDecoder.gen[PersonCreate]
}
