package domain.services.invoice.gel.models

import com.geldata.driver.annotations.{GelDeserializer, GelLinkType, GelType}
import domain.models.{PersonCreate, Invoice}
import domain.services.person.gel.models.PersonCreateGel

import java.time.LocalDate
import java.util
import java.util.UUID

@GelType
class InvoiceGel @GelDeserializer() (
  id: UUID,
  amount: Int,
  date: LocalDate,
  name: String,
  @GelLinkType(classOf[PersonCreateGel])
  gelDrivers: util.Collection[PersonCreateGel]
) {
  def getId: UUID                                  = id
  def getAmount: Int                               = amount
  def getDate: LocalDate                           = date
  def getName: String                              = name
  def getDrivers: util.Collection[PersonCreateGel] = gelDrivers
}

case class InvoiceGelCreate(
  amount: Double,
  date: LocalDate,
  name: String,
  drivers: util.Collection[PersonCreate]
)

case class InvoiceGelStats(
  trips: util.Collection[Invoice],
  totalKilometers: Double
)
