package domain.models.ia

import domain.services.ia.gel.models.WriterGel
import zio.json.*

import java.util.UUID

case class Writer(
  name: String,
  id: UUID
)

object Writer {
  implicit val encoder: JsonEncoder[Writer] = DeriveJsonEncoder.gen[Writer]
  implicit val decoder: JsonDecoder[Writer] = DeriveJsonDecoder.gen[Writer]

  def toWriterGel(person: Writer): WriterGel =
    WriterGel(person.name, person.id)

  def fromWriterGel(personGel: WriterGel): Writer =
    Writer(personGel.name, personGel.id)
}
