package domain.models

import zio.json.*

import java.util.UUID

case class Person(
  name: String,
  id: UUID
)

object Person {
  implicit val encoder: JsonEncoder[Person] = DeriveJsonEncoder.gen[Person]
  implicit val decoder: JsonDecoder[Person] = DeriveJsonDecoder.gen[Person]
}
