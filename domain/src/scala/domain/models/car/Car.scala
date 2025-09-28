package domain.models.car

import sttp.tapir.Schema
import zio.json.*

case class Car(
  id: CarId,
  name: String,
  mileage: Long
)

object Car {
  implicit val encoder: JsonEncoder[Car] = DeriveJsonEncoder.gen[Car]
  implicit val decoder: JsonDecoder[Car] = DeriveJsonDecoder.gen[Car]

  given Schema[Car] = Schema.derived[Car]
}
