package gel.maintenance.models

import com.geldata.driver.annotations.{GelDeserializer, GelLinkType, GelType}
import domain.models.maintenance.{Maintenance, MaintenanceId}
import gel.invoice.models.InvoiceGel

import java.lang.Short
import java.time.LocalDate
import java.util.UUID

@GelType
class MaintenanceGel @GelDeserializer() (
  id: UUID,
  `type`: String,
  isCompleted: Boolean,
  // Had to put String when field is null, otherwise it was not working
  dueMileage: String | Short,
  dueDate: LocalDate,
  completedDate: LocalDate,
  // Had to put String when field is null, otherwise it was not working
  completedMileage: String | Short,
  description: String,
  @GelLinkType(classOf[InvoiceGel])
  invoice: InvoiceGel
) {
  def getId: UUID                         = id
  def getType: String                     = `type`
  def getIsCompleted: Boolean             = isCompleted
  def getDueMileage: String | Short       = dueMileage
  def getDueDate: LocalDate               = dueDate
  def getCompletedDate: LocalDate         = completedDate
  def getCompletedMileage: String | Short = completedMileage
  def getDescription: String              = description
  def getInvoice: InvoiceGel              = {
    println(s"invoice value: ${Option(invoice).map(_.getPerson).getOrElse("null")}")
    invoice
  }
}

object MaintenanceGel {
  def fromMaintenanceGel(maintenanceGel: MaintenanceGel): Maintenance =
    Maintenance(
      MaintenanceId(maintenanceGel.getId),
      maintenanceGel.getType,
      maintenanceGel.getIsCompleted,
      Option(maintenanceGel.getDueMileage).map {
        case s: String => s.toInt
        case l: Short  => l.toInt
      },
      Option(maintenanceGel.getDueDate),
      Option(maintenanceGel.getCompletedDate),
      Option(maintenanceGel.getCompletedMileage).map {
        case s: String => s.toInt
        case l: Short  => l.toInt
      },
      Option(maintenanceGel.getDescription).filter(_.nonEmpty),
      Option(maintenanceGel.getInvoice).map(invoiceGel => gel.invoice.models.InvoiceGel.fromInvoiceGel(invoiceGel))
    )
}
