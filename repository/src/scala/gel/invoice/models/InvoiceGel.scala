package gel.invoice.models

import com.geldata.driver.annotations.{GelDeserializer, GelLinkType, GelType}
import domain.models.PersonCreate
import domain.services.invoice.models.Invoice
import gel.person.models.PersonCreateGel

import java.time.LocalDate
import java.util
import java.util.UUID
import scala.jdk.CollectionConverters.*

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

object InvoiceGel {
  def fromInvoiceGel(invoiceGel: InvoiceGel): Invoice =
    Invoice(
      invoiceGel.getId,
      invoiceGel.getAmount,
      invoiceGel.getDate,
      invoiceGel.getName,
      invoiceGel.getDrivers.asScala.map(_.name).toSet
    )
}
