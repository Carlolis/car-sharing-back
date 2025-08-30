package domain.models.invoice

import zio.json.*
import java.time.LocalDate

case class ReimbursementInvoiceCreate(
  amount: Long,
  date: LocalDate,
  name: String,
  fromDriver: DriverName,
  toDriver: DriverName,
  description: String
)

object ReimbursementInvoiceCreate {
  implicit val encoder: JsonEncoder[ReimbursementInvoiceCreate] = DeriveJsonEncoder.gen[ReimbursementInvoiceCreate]
  implicit val decoder: JsonDecoder[ReimbursementInvoiceCreate] = DeriveJsonDecoder.gen[ReimbursementInvoiceCreate]
}