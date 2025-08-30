package domain.models.invoice

import sttp.tapir.Schema
import sttp.tapir.generic.auto.*
import zio.json.*

case class Reimbursement(
  driverName: DriverName,
  totalAmount: Long,
  to: Map[DriverName, Long]
)

object Reimbursement {
  implicit val encoder: JsonEncoder[Reimbursement] = DeriveJsonEncoder.gen[Reimbursement]
  implicit val decoder: JsonDecoder[Reimbursement] = DeriveJsonDecoder.gen[Reimbursement]

  given Schema[Reimbursement] = Schema.derived[Reimbursement]

  given Schema[Map[DriverName, Long]] = Schema.schemaForMap[DriverName, Long](DriverName.encode)
}
