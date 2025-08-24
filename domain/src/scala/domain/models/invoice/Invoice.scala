package domain.models.invoice

import zio.json.*

import java.time.LocalDate
import java.util.UUID
import scala.jdk.CollectionConverters.*

case class Invoice(
  id: UUID,
  amount: Int,
  date: LocalDate,
  name: String,
  drivers: Set[DriverName],
  kind: String
)

object Invoice {
  implicit val encoder: JsonEncoder[Invoice] = DeriveJsonEncoder.gen[Invoice]
  implicit val decoder: JsonDecoder[Invoice] = DeriveJsonDecoder.gen[Invoice]
}
