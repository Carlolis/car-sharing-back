package gel.invoice.models

import com.geldata.driver.annotations.{GelDeserializer, GelLinkType, GelType}
import domain.models.PersonCreate
import domain.models.invoice.{DriverName, Invoice}
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
  gelPersons: util.Collection[PersonCreateGel],
  kind: String
) {
  def getId: UUID                                  = id
  def getAmount: Int                               = amount
  def getDate: LocalDate                           = date
  def getName: String                              = name
  def getPersons: util.Collection[PersonCreateGel] = gelPersons
  def getKind: String                              = kind
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
      invoiceGel.getName,
      invoiceGel.getAmount,
      invoiceGel.getDate,
      invoiceGel.getPersons.asScala.map(n => DriverName(n.name)).toSet,
      invoiceGel.getKind
    )
}
