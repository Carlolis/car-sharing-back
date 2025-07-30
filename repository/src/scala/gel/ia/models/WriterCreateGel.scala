package gel.ia.models

import com.geldata.driver.annotations.{GelDeserializer, GelLinkType, GelType}
import domain.models.ia.*

@GelType
case class WriterCreateGel @GelDeserializer() (var name: String)

object WriterCreateGel {
  def toWriterGel(writer: WriterCreate): WriterCreateGel =
    WriterCreateGel(
      name = writer.name
    )
}

@GelType
case class WriterGel @GelDeserializer() (var name: String, var id: java.util.UUID)

object WriterGel {
  def toWriterGel(person: Writer): WriterGel =
    WriterGel(person.name, person.id)

  def fromWriterGel(personGel: WriterGel): Writer =
    Writer(personGel.name, personGel.id)
}
