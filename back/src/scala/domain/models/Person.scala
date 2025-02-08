package domain.models

import domain.services.person.edgedb.models.PersonEdge
import zio.json.*

import java.util.UUID

case class Person(
  name: String,
  id: UUID
)

object Person {
  implicit val encoder: JsonEncoder[Person] = DeriveJsonEncoder.gen[Person]
  implicit val decoder: JsonDecoder[Person] = DeriveJsonDecoder.gen[Person]

  def toPersonEdge(person: Person): PersonEdge =
    PersonEdge(person.name, person.id)

  def fromPersonEdge(personEdge: PersonEdge): Person =
    Person(personEdge.name, personEdge.id)
}
