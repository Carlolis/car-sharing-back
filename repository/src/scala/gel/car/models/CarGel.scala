package gel.car.models

import com.geldata.driver.annotations.{GelDeserializer, GelType}
import domain.models.car.{Car, CarId}

import java.util
import java.util.UUID

@GelType
class CarGel @GelDeserializer() (
  id: UUID,
  name: String,
  mileage: Long
) {
  def getId: UUID     = id
  def getName: String = name
  def getMileage: Long = mileage
}

object CarGel {
  def toCar(carGel: CarGel): Car =
    Car(
      CarId(carGel.getId),
      carGel.getName,
      carGel.getMileage
    )
}
