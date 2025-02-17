package domain.models.ia

import domain.services.ia.edgedb.models.WriterEdge
import zio.json.*

import java.util.UUID

case class Writer(
  name: String,
  id: UUID
)

object Writer {
  implicit val encoder: JsonEncoder[Writer] = DeriveJsonEncoder.gen[Writer]
  implicit val decoder: JsonDecoder[Writer] = DeriveJsonDecoder.gen[Writer]

  def toWriterEdge(person: Writer): WriterEdge =
    WriterEdge(person.name, person.id)

  def fromWriterEdge(personEdge: WriterEdge): Writer =
    Writer(personEdge.name, personEdge.id)
}
