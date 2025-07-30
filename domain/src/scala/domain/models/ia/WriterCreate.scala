package domain.models.ia


import zio.json.*

case class WriterCreate(
  name: String
) 

object WriterCreate {
  implicit val encoder: JsonEncoder[WriterCreate] = DeriveJsonEncoder.gen[WriterCreate]
  implicit val decoder: JsonDecoder[WriterCreate] = DeriveJsonDecoder.gen[WriterCreate]

}
