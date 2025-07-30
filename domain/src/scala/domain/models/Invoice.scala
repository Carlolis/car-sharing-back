package domain.models

import zio.json.*

import java.time.LocalDate
import java.util.UUID
import scala.collection.JavaConverters.*

case class Invoice(
  id: UUID,
  distance: Int,
  date: LocalDate,
  name: String,
  drivers: Set[String]
)

object Invoice {
  implicit val encoder: JsonEncoder[Invoice] = DeriveJsonEncoder.gen[Invoice]
  implicit val decoder: JsonDecoder[Invoice] = DeriveJsonDecoder.gen[Invoice]
 
}

case class InvoiceStats(
  invoices: List[Invoice],
  totalKilometers: Double
)

object InvoiceStats {
  implicit val encoder: JsonEncoder[InvoiceStats] =
    DeriveJsonEncoder.gen[InvoiceStats]
  implicit val decoder: JsonDecoder[InvoiceStats] =
    DeriveJsonDecoder.gen[InvoiceStats]
}
