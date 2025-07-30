package gel.trip.models

import com.geldata.driver.annotations.{GelDeserializer, GelType}

@GelType
class TripStatsEdge @GelDeserializer() (
  totalKilometers: Double
) {
  def getTotalKilometers: Double = totalKilometers
}
