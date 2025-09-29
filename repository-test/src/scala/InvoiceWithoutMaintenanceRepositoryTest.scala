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
  import common.TestData
  import common.TestUtils

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
