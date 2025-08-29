package domain.models.invoice

import sttp.tapir.Schema
import sttp.tapir.generic.auto.*
import zio.json.*

case class Reimbursement(
  driverName: DriverName,
  totalAmount: Float,
  to: Map[DriverName, Float]
)

object Reimbursement {
  implicit val encoder: JsonEncoder[Reimbursement] = DeriveJsonEncoder.gen[Reimbursement]
  implicit val decoder: JsonDecoder[Reimbursement] = DeriveJsonDecoder.gen[Reimbursement]

  given Schema[Reimbursement] = Schema.derived[Reimbursement]

  given Schema[Map[DriverName, Float]] = Schema.schemaForMap[DriverName, Float](DriverName.encode)
}
