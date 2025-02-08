package api

import zio.json.*

import java.util.UUID

case class Token(
  token: String
)

object Token {
  implicit val encoder: JsonEncoder[Token] = DeriveJsonEncoder.gen[Token]
  implicit val decoder: JsonDecoder[Token] = DeriveJsonDecoder.gen[Token]
}
