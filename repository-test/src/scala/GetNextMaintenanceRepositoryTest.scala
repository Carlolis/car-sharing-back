import adapters.GelDriver
import domain.services.maintenance.repository.MaintenanceRepository
import gel.invoice.InvoiceRepositoryGel
import gel.maintenance.MaintenanceRepositoryGel
import gel.person.PersonRepositoryGel
import zio.*
import zio.test.*
import zio.test.TestAspect.*

object GetNextMaintenanceRepositoryTest extends ZIOSpecDefault {
  import common.{TestData, TestUtils}

  def spec: Spec[TestEnvironment & Scope, Any] =
    (suite("Get New Maintenances Repository Test")(
      test("should create one none completed maintenance, should get it") {
        for {
          maintenanceId                   <- MaintenanceRepository.createMaintenance(TestData.maintenanceCreate1)
          maintenancesOption              <- MaintenanceRepository.getNextMaintenances
          (maintenanceOne, maintenanceTwo) = maintenancesOption.get
          _                               <- ZIO.logInfo(s"[DEBUG_LOG] Found maintenance: $maintenanceOne and $maintenanceTwo")
        } yield assertTrue(
          maintenanceTwo.isEmpty,
          maintenanceOne.`type` == TestData.maintenanceCreate1.`type`,
          maintenanceOne.dueMileage == TestData.maintenanceCreate1.dueMileage,
          maintenanceOne.description == TestData.maintenanceCreate1.description
        )
      },
      test("should create one completed maintenance, do not get it") {
        for {
          maintenanceId      <- MaintenanceRepository.createMaintenance(TestData.maintenanceCreate1.copy(isCompleted = true))
          maintenancesOption <- MaintenanceRepository.getNextMaintenances
        } yield assertTrue(
          maintenancesOption.isEmpty
        )
      }
    ) @@ TestAspect.beforeAll(
      TestUtils.setupTestData.catchAll(e => ZIO.logError(s"Erreur lors de la configuration: ${e.getMessage}"))
    )
      @@ afterAll(
        for {
          _ <- TestUtils.cleanupMaintenances
          _ <- TestUtils.cleanupInvoices
          _ <- TestUtils.cleanupPersons
          _ <- ZIO.logInfo("[DEBUG_LOG] Cleaned up test data before tests")
        } yield ()
      )
      @@ TestAspect.sequential).provideShared(
      MaintenanceRepositoryGel.layer,
      InvoiceRepositoryGel.layer,
      PersonRepositoryGel.layer,
      GelDriver.testLayer
    )
}
