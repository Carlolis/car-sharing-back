import adapters.GelDriver
import domain.services.maintenance.repository.MaintenanceRepository
import gel.invoice.InvoiceRepositoryGel
import gel.maintenance.MaintenanceRepositoryGel
import gel.person.PersonRepositoryGel
import zio.*
import zio.test.*
import zio.test.TestAspect.*

import java.time.LocalDate

object GetNextMaintenanceRepositoryTest extends ZIOSpecDefault {
  import common.{TestData, TestUtils}

  def spec: Spec[TestEnvironment & Scope, Any] =
    (suite("Get New Maintenances Repository Test")(
      test("create one none completed maintenance, should get it") {
        for {
          maintenanceId                   <- MaintenanceRepository.createMaintenance(TestData.maintenanceCreate1)
          maintenancesOption              <- MaintenanceRepository.getNextMaintenances
          _                               <- ZIO.logInfo(s"[DEBUG_LOG] Yolo: $maintenancesOption")
          (maintenanceOne, maintenanceTwo) = maintenancesOption.get
          _                               <- ZIO.logInfo(s"[DEBUG_LOG] Found next maintenances: $maintenanceOne and $maintenanceTwo")
        } yield assertTrue(
          maintenanceTwo.isEmpty,
          maintenanceOne.`type` == TestData.maintenanceCreate1.`type`,
          maintenanceOne.dueMileage == TestData.maintenanceCreate1.dueMileage,
          maintenanceOne.description == TestData.maintenanceCreate1.description
        )
      },
      test("create one completed maintenance, do not get it") {
        for {
          maintenanceId      <- MaintenanceRepository.createMaintenance(TestData.maintenanceCreate1.copy(isCompleted = true))
          maintenancesOption <- MaintenanceRepository.getNextMaintenances
        } yield assertTrue(
          maintenancesOption.isEmpty
        )
      },
        test ("create two none completed maintenances, first is with dueMileage, second is with dueDate, should get both") {
          for {
            maintenanceId                   <- MaintenanceRepository.createMaintenance(TestData.maintenanceCreate1.copy(dueDate = None))
            maintenanceId2                  <-
              MaintenanceRepository.createMaintenance(
                TestData.maintenanceCreate2.copy(isCompleted = false, dueMileage = None, dueDate = Some(LocalDate.now().plusDays(5))))
            maintenancesOption              <- MaintenanceRepository.getNextMaintenances
            (maintenanceOne, maintenanceTwo) = maintenancesOption.get
            _                               <- ZIO.logInfo(s"[DEBUG_LOG] Found next maintenances: $maintenanceOne and $maintenanceTwo")
          } yield assertTrue(
            maintenanceOne.`type` == TestData.maintenanceCreate1.`type`,
            maintenanceOne.dueMileage == TestData.maintenanceCreate1.dueMileage,
            maintenanceOne.description == TestData.maintenanceCreate1.description,
            maintenanceOne.dueDate.isEmpty,
            maintenanceTwo.isDefined,
            maintenanceTwo.get.`type` == TestData.maintenanceCreate2.`type`,
            maintenanceTwo.get.dueDate.contains(LocalDate.now().plusDays(5)),
            maintenanceTwo.get.dueMileage.isEmpty,
            maintenanceTwo.get.description == TestData.maintenanceCreate2.description
          )
        },
      
      test ("create two none completed maintenances, both is with dueDate, should get only the one with older dueDate") {
        for {
          maintenanceId                   <- MaintenanceRepository.createMaintenance(TestData.maintenanceCreate1.copy(dueMileage = None, dueDate = Some(LocalDate.now().minusDays(5))))
          maintenanceId2                  <-
            MaintenanceRepository.createMaintenance(
              TestData.maintenanceCreate2.copy(isCompleted = false, dueMileage = None, dueDate = Some(LocalDate.now().plusDays(5))))
          maintenancesOption              <- MaintenanceRepository.getNextMaintenances
          (maintenanceOne, maintenanceTwo) = maintenancesOption.get
          _                               <- ZIO.logInfo(s"[DEBUG_LOG] Found next maintenances: $maintenanceOne and $maintenanceTwo")
        } yield assertTrue(
          maintenanceOne.`type` == TestData.maintenanceCreate1.`type`,
          maintenanceOne.dueMileage.isEmpty,
          maintenanceOne.description == TestData.maintenanceCreate1.description,
          maintenanceOne.dueDate.contains(LocalDate.now().minusDays(5)),
          maintenanceTwo.isEmpty
        )
      },
    ) @@ TestAspect.beforeAll(
      TestUtils.setupTestDataPersons.catchAll(e => ZIO.logError(s"Erreur lors de la configuration: ${e.getMessage}"))
    )
      @@ after(
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
