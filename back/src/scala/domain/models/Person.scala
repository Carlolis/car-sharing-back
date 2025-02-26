package domain.models

import domain.services.person.gel.models.PersonGel
import zio.json.*

import java.util.UUID

case class Person(
  name: String,
  id: UUID
)

object Person {
  implicit val encoder: JsonEncoder[Person] = DeriveJsonEncoder.gen[Person]
  implicit val decoder: JsonDecoder[Person] = DeriveJsonDecoder.gen[Person]

  def toPersonGel(person: Person): PersonGel =
    PersonGel(person.name, person.id)

  def fromPersonGel(personGel: PersonGel): Person =
    Person(personGel.name, personGel.id)
}
