package domain.models

import domain.services.invoice.gel.models.InvoiceGel
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
  def fromInvoiceGel(invoiceGel: InvoiceGel): Invoice =
    Invoice(
      invoiceGel.getId,
      invoiceGel.getAmount,
      invoiceGel.getDate,
      invoiceGel.getName,
      invoiceGel.getDrivers.asScala.map(_.name).toSet
    )
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
