package domain.models.invoice

import zio.json.*

import scala.jdk.CollectionConverters.*

case class Reimbursement(
  driverName: DriverName,
  totalAmount: Int,
  to: Map[DriverName, Int]
)

object Reimbursement {
  implicit val encoder: JsonEncoder[Reimbursement] = DeriveJsonEncoder.gen[Reimbursement]
  implicit val decoder: JsonDecoder[Reimbursement] = DeriveJsonDecoder.gen[Reimbursement]
}
