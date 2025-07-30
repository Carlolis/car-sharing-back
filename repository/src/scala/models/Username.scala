package models

import zio.json.*

import java.util.UUID

case class Username(name: String)

object Username {
  implicit val encoder: JsonEncoder[Username] = DeriveJsonEncoder.gen[Username]
  implicit val decoder: JsonDecoder[Username] = DeriveJsonDecoder.gen[Username]
}
