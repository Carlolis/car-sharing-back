package domain.models.invoice

import sttp.tapir.Schema
import sttp.tapir.generic.auto.*
import zio.json.*

case class Reimbursement(
  driverName: DriverName,
  totalAmount: Double,
  to: Map[DriverName, Double]
)

object Reimbursement {
  implicit val encoder: JsonEncoder[Reimbursement] = DeriveJsonEncoder.gen[Reimbursement]
  implicit val decoder: JsonDecoder[Reimbursement] = DeriveJsonDecoder.gen[Reimbursement]

  given Schema[Reimbursement] = Schema.derived[Reimbursement]

  given Schema[Map[DriverName, Double]] = Schema.schemaForMap[DriverName, Double](DriverName.encode)
}
