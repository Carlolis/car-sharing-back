package common

import domain.models.PersonCreate
import domain.models.invoice.{DriverName, InvoiceCreate}
import domain.models.maintenance.MaintenanceCreate
import java.time.LocalDate

object TestData {
  // Common person names
  val maePersonName: String      = "maé"
  val charlesPersonName: String  = "charles"
  val brigittePersonName: String = "brigitte"

  // Common persons
  val mae: PersonCreate      = PersonCreate(maePersonName)
  val charles: PersonCreate  = PersonCreate(charlesPersonName)
  val brigitte: PersonCreate = PersonCreate(brigittePersonName)

  val allPersons: Set[PersonCreate] = Set(mae, charles, brigitte)

  // Invoices commonly used
  var kind: String = "péage"

  val sampleInvoiceCreate: InvoiceCreate = InvoiceCreate(
    amount = 99,
    mileage = Some(99),
    date = LocalDate.now(),
    name = "Business",
    driver = DriverName(maePersonName),
    kind = kind
  )

  val sampleInvoiceCreateWithFileName: InvoiceCreate = InvoiceCreate(
    amount = 150,
    mileage = Some(120),
    date = LocalDate.now(),
    name = "Business with file",
    driver = DriverName(charlesPersonName),
    kind = kind,
    fileName = Some("test_invoice.pdf")
  )

  val invoice1: InvoiceCreate = InvoiceCreate(
    amount = 50,
    mileage = Some(100),
    date = LocalDate.now(),
    name = "I1",
    driver = DriverName(maePersonName),
    kind = "essence"
  )

  val invoice2: InvoiceCreate = InvoiceCreate(
    amount = 75,
    mileage = Some(200),
    date = LocalDate.now(),
    name = "I2",
    driver = DriverName(charlesPersonName),
    kind = "maintenance"
  )

  // Maintenances commonly used
  val maintenanceCreate1: MaintenanceCreate = MaintenanceCreate(
    `type` = "Vidange",
    isCompleted = false,
    dueMileage = Some(10000),
    dueDate = Some(LocalDate.now().plusMonths(1)),
    completedDate = None,
    completedMileage = None,
    description = Some("Vidange moteur scheduled"),
    invoiceId = None
  )

  val maintenanceCreate2: MaintenanceCreate = MaintenanceCreate(
    `type` = "Contrôle Technique",
    isCompleted = true,
    dueMileage = None,
    dueDate = Some(LocalDate.now().minusMonths(1)),
    completedDate = Some(LocalDate.now().minusDays(5)),
    completedMileage = Some(15000),
    description = Some("Contrôle technique passed"),
    invoiceId = None
  )

  val expectedReimbursementAmount: Long = 33

  // Specific invoice used with maintenance tests
  val invoiceCreateForMaintenance: InvoiceCreate = InvoiceCreate(
    amount = BigDecimal(120.50),
    mileage = Some(15000),
    date = LocalDate.now(),
    name = "Test Invoice for Maintenance",
    driver = DriverName(maePersonName),
    kind = "maintenance",
    fileBytes = None,
    fileName = Some("maintenance_invoice.pdf"),
    toDriver = None
  )
}
