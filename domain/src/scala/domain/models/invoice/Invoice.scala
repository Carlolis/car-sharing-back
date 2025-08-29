package domain.models.invoice

import zio.json.*

import java.time.LocalDate
import scala.jdk.CollectionConverters.*

case class Invoice(
  id: InvoiceId,
  name: String,
  amount: Long,
  date: LocalDate,
  drivers: Set[DriverName],
  kind: String,
  mileage: Option[Int],
  fileName: Option[String]
)

object Invoice {
  implicit val encoder: JsonEncoder[Invoice] = DeriveJsonEncoder.gen[Invoice]
  implicit val decoder: JsonDecoder[Invoice] = DeriveJsonDecoder.gen[Invoice]
}
