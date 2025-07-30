package domain.models.ia


import zio.json.*

import java.util.UUID

case class Writer(
  name: String,
  id: UUID
)

object Writer {
  implicit val encoder: JsonEncoder[Writer] = DeriveJsonEncoder.gen[Writer]
  implicit val decoder: JsonDecoder[Writer] = DeriveJsonDecoder.gen[Writer]

 
}
