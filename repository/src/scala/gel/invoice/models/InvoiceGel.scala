package gel.invoice.models

import com.geldata.driver.annotations.{GelDeserializer, GelLinkType, GelType}
import domain.models.PersonCreate
import domain.models.invoice.{DriverName, Invoice, InvoiceId}
import gel.person.models.PersonCreateGel

import java.lang.Short
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
  kind: String,
  // Had to put String when mileage is null, otherwise it was not working
  mileage: String | Short
) {
  def getId: UUID                                  = id
  def getAmount: Int                               = amount
  def getDate: LocalDate                           = date
  def getName: String                              = name
  def getPersons: util.Collection[PersonCreateGel] = gelPersons
  def getKind: String                              = kind
  def getMileage: String | Short                   = mileage
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
      InvoiceId(invoiceGel.getId),
      invoiceGel.getName,
      invoiceGel.getAmount,
      invoiceGel.getDate,
      invoiceGel.getPersons.asScala.map(n => DriverName(n.name)).toSet,
      invoiceGel.getKind,
      Option(invoiceGel.getMileage).map {
        case s: String => s.toInt
        case l: Short  => l.toInt
      }
    )
}
