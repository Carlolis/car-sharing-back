package domain.models.ia

import domain.services.ia.edgedb.models.WriterCreateEdge
import zio.json.*

case class WriterCreate(
  name: String
) {
  def toPersonEdge: WriterCreateEdge =
    WriterCreateEdge(name)
}

object PersonCreate {
  implicit val encoder: JsonEncoder[WriterCreate] = DeriveJsonEncoder.gen[WriterCreate]
  implicit val decoder: JsonDecoder[WriterCreate] = DeriveJsonDecoder.gen[WriterCreate]
}
