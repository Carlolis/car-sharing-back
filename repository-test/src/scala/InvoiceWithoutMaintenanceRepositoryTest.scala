import adapters.GelDriver
import domain.models.PersonCreate
import domain.models.invoice.{DriverName, InvoiceCreate}
import domain.services.invoice.repository.InvoiceRepository
import domain.services.maintenance.repository.MaintenanceRepository
import domain.services.person.PersonService
import domain.services.trip.TripService
import gel.invoice.InvoiceRepositoryGel
import gel.maintenance.MaintenanceRepositoryGel
import gel.person.PersonRepositoryGel
import gel.trip.TripRepositoryGel
import zio.*
import zio.test.*

import java.time.LocalDate

object InvoiceWithoutMaintenanceRepositoryTest extends ZIOSpecDefault {
  object TestData {
    val maePersonName      = "maé"
    val charlesPersonName  = "charles"
    val brigittePersonName = "brigitte"

    val mae      = PersonCreate(maePersonName)
    val charles  = PersonCreate(charlesPersonName)
    val brigitte = PersonCreate(brigittePersonName)

    val allPersons = Set(mae, charles, brigitte)

    val invoice1 = InvoiceCreate(
      amount = 50,
      mileage = Some(100),
      date = LocalDate.now(),
      name = "I1",
      driver = DriverName(maePersonName),
      kind = "essence"
    )

    val invoice2 = InvoiceCreate(
      amount = 75,
      mileage = Some(200),
      date = LocalDate.now(),
      name = "I2",
      driver = DriverName(charlesPersonName),
      kind = "maintenance"
    )
  }

  object TestUtils {
    def setupPersons: ZIO[PersonService, Throwable, Unit] =
      ZIO.foreachPar(TestData.allPersons)(p => PersonService.createPerson(p)).unit

    def cleanupAll: ZIO[PersonService & InvoiceRepository & MaintenanceRepository & TripService, Nothing, Unit] =
      (for {
        maintenances <- MaintenanceRepository.getAllMaintenances
        _            <- ZIO.foreachDiscard(maintenances)(m => MaintenanceRepository.deleteMaintenance(m.id))
        invoices     <- InvoiceRepository.getAllInvoices
        _            <- ZIO.foreachDiscard(invoices)(i => InvoiceRepository.deleteInvoice(i.id))
        invoices     <- TripService.getAllTrips
        _            <- ZIO.foreachDiscard(invoices)(i => TripService.deleteTrip(i.id))
        persons      <- PersonService.getAll
        _            <- ZIO.foreachDiscard(persons)(p => PersonService.deletePerson(p.id))
      } yield ()).orDie
  }

  def spec: Spec[TestEnvironment & Scope, Any] = (
    suite("Invoice without maintenance - Gel")(
      test("should return only invoices without maintenance link") {
        for {
          // setup
          id1 <- InvoiceRepository.createInvoice(TestData.invoice1)
          id2 <- InvoiceRepository.createInvoice(TestData.invoice2)
          // create maintenance linked to invoice2
          _   <- MaintenanceRepository.createMaintenance(
                   domain
                     .models.maintenance.MaintenanceCreate(
                       `type` = "Vidange",
                       isCompleted = false,
                       dueMileage = None,
                       dueDate = None,
                       completedDate = None,
                       completedMileage = None,
                       description = Some("link to invoice2"),
                       invoiceId = Some(domain.models.invoice.InvoiceId(id2))
                     )
                 )
          // call method under test
          res <- InvoiceRepository.getAllInvoicesWithoutMaintenance
        } yield assertTrue(res.size == 1 && res.head.name == "I1")
      }
    ) @@ TestAspect.before(
      TestUtils.setupPersons.catchAll(e => ZIO.logError(s"[DEBUG_LOG] setup error: ${e.getMessage}"))
    ) @@ TestAspect.after(
      TestUtils.cleanupAll.unit
    ) @@ TestAspect.sequential
  ).provideShared(
    InvoiceRepositoryGel.layer,
    MaintenanceRepositoryGel.layer,
    PersonRepositoryGel.layer,
    TripRepositoryGel.layer,
    GelDriver.testLayer
  )
}
