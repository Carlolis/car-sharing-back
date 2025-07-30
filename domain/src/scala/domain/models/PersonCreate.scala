package domain.models


import zio.json.*

case class PersonCreate(
  name: String
) 

object PersonCreate {
  implicit val encoder: JsonEncoder[PersonCreate] = DeriveJsonEncoder.gen[PersonCreate]
  implicit val decoder: JsonDecoder[PersonCreate] = DeriveJsonDecoder.gen[PersonCreate]
}
