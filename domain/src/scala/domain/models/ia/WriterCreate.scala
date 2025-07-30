package domain.models.ia

import domain.services.ia.gel.models.WriterCreateGel
import zio.json.*

case class WriterCreate(
  name: String
) 

object WriterCreate {
  implicit val encoder: JsonEncoder[WriterCreate] = DeriveJsonEncoder.gen[WriterCreate]
  implicit val decoder: JsonDecoder[WriterCreate] = DeriveJsonDecoder.gen[WriterCreate]

  def toWriterGel(writer: WriterCreate): WriterCreateGel = {
    WriterCreateGel(
      name = writer.name
    )
  }
}
