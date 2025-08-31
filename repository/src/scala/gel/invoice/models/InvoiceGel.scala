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
  amount: java.math.BigDecimal,
  date: LocalDate,
  name: String,
  @GelLinkType(classOf[PersonCreateGel])
  gelPerson: PersonCreateGel,
  kind: String,
  // Had to put String when mileage is null, otherwise it was not working
  mileage: String | Short,
  fileName: String,
  isReimbursement: Boolean,
  @GelLinkType(classOf[PersonCreateGel])
  toDriver: PersonCreateGel
) {
  def getId: UUID                  = id
  def getAmount: BigDecimal        = {
    println(s"amount type: ${amount.getClass.getName}, value: $amount")
    return amount;
  }
  def getDate: LocalDate           = date
  def getName: String              = name
  def getPerson: PersonCreateGel   = gelPerson
  def getKind: String              = kind
  def getMileage: String | Short   = mileage
  def getFileName: String          = fileName
  def getIsReimbursement: Boolean  = isReimbursement
  def getToDriver: PersonCreateGel = toDriver
}

case class InvoiceGelCreate(
  amount: Double,
  date: LocalDate,
  name: String,
  driver: PersonCreate
)

case class InvoiceGelStats(
  trips: util.Collection[Invoice],
  totalKilometers: Int
)

object InvoiceGel {
  def fromInvoiceGel(invoiceGel: InvoiceGel): Invoice =
    Invoice(
      InvoiceId(invoiceGel.getId),
      invoiceGel.getName,
      invoiceGel.getAmount,
      invoiceGel.getDate,
      DriverName(invoiceGel.getPerson.name),
      invoiceGel.getKind,
      Option(invoiceGel.getMileage).map {
        case s: String => s.toInt
        case l: Short  => l.toInt
      },
      Option(invoiceGel.getFileName).filter(_.nonEmpty),
      invoiceGel.getIsReimbursement,
      Option(invoiceGel.getToDriver).map(person => DriverName(person.name))
    )
}
