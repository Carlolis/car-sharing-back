package models

import gel.person.models.PersonCreateGel
import zio.json.*

case class PersonCreate(
  name: String
) {
  def toPersonGel: PersonCreateGel =
    PersonCreateGel(name)
}

object PersonCreate {
  implicit val encoder: JsonEncoder[PersonCreate] = DeriveJsonEncoder.gen[PersonCreate]
  implicit val decoder: JsonDecoder[PersonCreate] = DeriveJsonDecoder.gen[PersonCreate]
}
