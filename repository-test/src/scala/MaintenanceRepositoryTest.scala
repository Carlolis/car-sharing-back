import adapters.GelDriver
import domain.models.PersonCreate
import domain.models.invoice.{Invoice, InvoiceCreate, InvoiceId}
import domain.models.maintenance.{Maintenance, MaintenanceCreate, MaintenanceId, MaintenanceUpdate}
import domain.services.invoice.repository.InvoiceRepository
import domain.services.maintenance.repository.MaintenanceRepository
import domain.services.person.PersonService
import gel.invoice.InvoiceRepositoryGel
import gel.maintenance.MaintenanceRepositoryGel
import gel.person.PersonRepositoryGel
import zio.*
import zio.test.*
import zio.test.TestAspect.*

import java.time.LocalDate

object MaintenanceRepositoryTest extends ZIOSpecDefault {
  object TestData {
    val maePersonName      = "maé"
    val charlesPersonName  = "charles"
    val brigittePersonName = "brigitte"
    var kind               = "péage"

    val mae      = PersonCreate(maePersonName)
    val charles  = PersonCreate(charlesPersonName)
    val brigitte = PersonCreate(brigittePersonName)

    val allPersons         = Set(mae, charles, brigitte)
    val maintenanceCreate1 = MaintenanceCreate(
      `type` = "Vidange",
      isCompleted = false,
      dueMileage = Some(10000),
      dueDate = Some(LocalDate.now().plusMonths(1)),
      completedDate = None,
      completedMileage = None,
      description = Some("Vidange moteur scheduled"),
      invoiceId = None
    )

    val maintenanceCreate2 = MaintenanceCreate(
      `type` = "Contrôle Technique",
      isCompleted = true,
      dueMileage = None,
      dueDate = Some(LocalDate.now().minusMonths(1)),
      completedDate = Some(LocalDate.now().minusDays(5)),
      completedMileage = Some(15000),
      description = Some("Contrôle technique passed"),
      invoiceId = None
    )

    val invoiceCreate = InvoiceCreate(
      amount = BigDecimal(120.50),
      mileage = Some(15000),
      date = LocalDate.now(),
      name = "Test Invoice for Maintenance",
      driver = domain.models.invoice.DriverName(maePersonName),
      kind = "maintenance",
      fileBytes = None,
      fileName = Some("maintenance_invoice.pdf"),
      toDriver = None
    )
  }

  object TestUtils {
    def createTestInvoice: ZIO[InvoiceRepository, Throwable, InvoiceId] =
      for {
        invoiceId <- InvoiceRepository.createInvoice(TestData.invoiceCreate)
      } yield InvoiceId(invoiceId)

    def cleanupMaintenances: ZIO[MaintenanceRepository, Nothing, Unit] =
      for {
        maintenances <- MaintenanceRepository.getAllMaintenances.orDie
        _            <- ZIO.foreachDiscard(maintenances)(m => MaintenanceRepository.deleteMaintenance(m.id).orDie)
      } yield ()

    def cleanupInvoices: ZIO[InvoiceRepository, Nothing, Unit] =
      for {
        invoices <- InvoiceRepository.getAllInvoices.orDie
        _        <- ZIO.foreachDiscard(invoices)(i => InvoiceRepository.deleteInvoice(i.id).orDie)
      } yield ()

    def cleanupPersons: ZIO[PersonService, Nothing, Unit]  =
      for {
        invoices <- PersonService.getAll.orDie
        _        <- ZIO.foreachDiscard(invoices)(i => PersonService.deletePerson(i.id).orDie)
      } yield ()
    def setupTestData: ZIO[PersonService, Throwable, Unit] =
      ZIO.foreachPar(TestData.allPersons)(person => PersonService.createPerson(person)).unit
  }

  def spec: Spec[TestEnvironment & Scope, Any] =
    (suite("MaintenanceRepositoryTest")(
      test("should create maintenance without invoice") {
        for {
          maintenanceId <- MaintenanceRepository.createMaintenance(TestData.maintenanceCreate1)
          _             <- ZIO.logInfo(s"[DEBUG_LOG] Created maintenance with id: $maintenanceId")
          maintenances  <- MaintenanceRepository.getAllMaintenances
          found          = maintenances.find(_.id.toString == maintenanceId.toString)
          _             <- ZIO.logInfo(s"[DEBUG_LOG] Found maintenance: $found")
        } yield assertTrue(
          found.isDefined,
          found.get.`type` == "Vidange",
          !found.get.isCompleted,
          found.get.dueMileage.contains(10000),
          found.get.description.contains("Vidange moteur scheduled"),
          found.get.invoice.isEmpty
        )
      },
      test("should create maintenance with char ' ") {
        for {
          maintenanceId <-
            MaintenanceRepository.createMaintenance(TestData.maintenanceCreate1.copy(description = Some("Changement filtre à d'air")))
          _             <- ZIO.logInfo(s"[DEBUG_LOG] Created maintenance with id: $maintenanceId")
          maintenances  <- MaintenanceRepository.getAllMaintenances
          found          = maintenances.find(_.id.toString == maintenanceId.toString)
          _             <- ZIO.logInfo(s"[DEBUG_LOG] Found maintenance: $found")
        } yield assertTrue(
          found.isDefined,
          found.get.`type` == "Vidange",
          !found.get.isCompleted,
          found.get.dueMileage.contains(10000),
          found.get.description.contains("Changement filtre à d'air"),
          found.get.invoice.isEmpty
        )
      },
      test("should create maintenance with invoice") {
        for {
          invoiceId                   <- TestUtils.createTestInvoice
          _                           <- ZIO.logInfo(s"[DEBUG_LOG] Created test invoice with id: $invoiceId")
          maintenanceCreateWithInvoice = TestData.maintenanceCreate2.copy(invoiceId = Some(invoiceId))
          maintenanceId               <- MaintenanceRepository.createMaintenance(maintenanceCreateWithInvoice)
          _                           <- ZIO.logInfo(s"[DEBUG_LOG] Created maintenance with id: $maintenanceId")
          maintenances                <- MaintenanceRepository.getAllMaintenances
          found                        = maintenances.find(_.id.toString == maintenanceId.toString)
          _                           <- ZIO.logInfo(s"[DEBUG_LOG] Found maintenance with invoice: $found")
        } yield assertTrue(
          found.isDefined,
          found.get.`type` == "Contrôle Technique",
          found.get.isCompleted,
          found.get.completedMileage.contains(15000),
          found.get.invoice.isDefined,
          found.get.invoice.get.name == "Test Invoice for Maintenance"
        )
      },
      test("should update maintenance") {
        for {
          maintenanceId       <- MaintenanceRepository.createMaintenance(TestData.maintenanceCreate1)
          _                   <- ZIO.logInfo(s"[DEBUG_LOG] Created maintenance for update with id: $maintenanceId")
          maintenances        <- MaintenanceRepository.getAllMaintenances
          original             = maintenances.find(_.id.toString == maintenanceId.toString).get
          updatedUpdate        = MaintenanceUpdate(
                                   id = original.id,
                                   `type` = original.`type`,
                                   isCompleted = true,
                                   dueMileage = original.dueMileage,
                                   dueDate = original.dueDate,
                                   completedDate = Some(LocalDate.now()),
                                   completedMileage = Some(12000),
                                   description = Some("Vidange completed successfully"),
                                   invoiceId = original.invoice.map(_.id)
                                 )
          _                   <- MaintenanceRepository.updateMaintenance(updatedUpdate)
          _                   <- ZIO.logInfo(s"[DEBUG_LOG] Updated maintenance")
          updatedMaintenances <- MaintenanceRepository.getAllMaintenances
          found                = updatedMaintenances.find(_.id.toString == maintenanceId.toString)
          _                   <- ZIO.logInfo(s"[DEBUG_LOG] Found updated maintenance: $found")
        } yield assertTrue(
          found.isDefined,
          found.get.isCompleted,
          found.get.completedMileage.contains(12000),
          found.get.description.contains("Vidange completed successfully")
        )
      },
      test("should delete maintenance") {
        for {
          maintenanceId <- MaintenanceRepository.createMaintenance(TestData.maintenanceCreate1)
          _             <- ZIO.logInfo(s"[DEBUG_LOG] Created maintenance for deletion with id: $maintenanceId")
          _             <- MaintenanceRepository.deleteMaintenance(MaintenanceId(maintenanceId))
          _             <- ZIO.logInfo(s"[DEBUG_LOG] Deleted maintenance")
          maintenances  <- MaintenanceRepository.getAllMaintenances
          found          = maintenances.find(_.id.toString == maintenanceId.toString)
          _             <- ZIO.logInfo(s"[DEBUG_LOG] Maintenance after deletion: $found")
        } yield assertTrue(found.isEmpty)
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
