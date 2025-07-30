package domain.models.ia


import zio.json.*

case class Message(
  question: String,
  answer: String
)

object Message {
  implicit val encoder: JsonEncoder[Message] = DeriveJsonEncoder.gen[Message]
  implicit val decoder: JsonDecoder[Message] = DeriveJsonDecoder.gen[Message]

}
