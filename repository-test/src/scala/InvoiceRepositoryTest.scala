import adapters.GelDriver
import domain.models.PersonCreate
import domain.models.invoice.{DriverName, InvoiceCreate}
import domain.services.invoice.repository.InvoiceRepository
import domain.services.person.PersonService
import gel.invoice.InvoiceRepositoryGel
import gel.person.PersonRepositoryGel
import zio.test.*
import zio.{Scope, ZIO, ZLayer}

import java.time.LocalDate

object InvoiceRepositoryTest extends ZIOSpecDefault {

  // Configuration des données de test
  object TestData {
    val maePersonName      = "maé"
    val charlesPersonName  = "charles"
    val brigittePersonName = "brigitte"
    var kind               = "péage"

    val mae      = PersonCreate(maePersonName)
    val charles  = PersonCreate(charlesPersonName)
    val brigitte = PersonCreate(brigittePersonName)

    val allPersons = Set(mae, charles, brigitte)

    val sampleInvoiceCreate = InvoiceCreate(
      99,
      mileage = Some(99),
      date = LocalDate.now(),
      name = "Business",
      drivers = Set(DriverName(maePersonName)),
      kind
    )

    val sampleInvoiceCreateWithFileName = InvoiceCreate(
      150,
      mileage = Some(120),
      date = LocalDate.now(),
      name = "Business with file",
      drivers = Set(DriverName(charlesPersonName)),
      kind,
      fileName = Some("test_invoice.pdf")
    )

    val expectedReimbursementAmount = 33
  }

  // Utilitaires de test
  object TestUtils {
    def cleanupData: ZIO[PersonService & InvoiceRepository, Throwable, Unit] =
      for {
        allInvoices <- InvoiceRepository.getAllInvoices
        _           <- ZIO.foreachDiscard(allInvoices)(invoice => InvoiceRepository.deleteInvoice(invoice.id))
        allPersons  <- PersonService.getAll
        _           <- ZIO.foreachDiscard(allPersons)(person => PersonService.deletePerson(person.id))
      } yield ()

    def setupTestData: ZIO[PersonService, Throwable, Unit] =
      ZIO.foreachPar(TestData.allPersons)(person => PersonService.createPerson(person)).unit
  }

  def spec: Spec[TestEnvironment & Scope, Any] =
    (suite("InvoiceRepository - Tests Gel")(
      test("Création d'une facture - Maé devrait créer une facture avec succès") {
        for {
          invoiceUuid <- InvoiceRepository.createInvoice(TestData.sampleInvoiceCreate)
          allInvoices <- InvoiceRepository.getAllInvoices
        } yield assertTrue(
          invoiceUuid != null,
          allInvoices.length == 1,
          allInvoices.head.kind == TestData.kind
        )
      },
      test("Création d'une facture - Maé devrait créer une facture avec succès sans kilométrage ") {
        for {
          invoiceUuid <- InvoiceRepository.createInvoice(TestData.sampleInvoiceCreate.copy(mileage = None))
          allInvoices <- InvoiceRepository.getAllInvoices
        } yield assertTrue(
          invoiceUuid != null,
          allInvoices.length == 1,
          allInvoices.head.kind == TestData.kind,
          allInvoices.head.mileage.isEmpty
        )
      },
      test("Création d'une facture avec fileName - Charles devrait créer une facture avec fileName avec succès") {
        for {
          invoiceUuid   <- InvoiceRepository.createInvoice(TestData.sampleInvoiceCreateWithFileName)
          allInvoices   <- InvoiceRepository.getAllInvoices
          createdInvoice = allInvoices.find(_.name == "Business with file")
        } yield assertTrue(
          invoiceUuid != null,
          allInvoices.nonEmpty,
          createdInvoice.isDefined,
          createdInvoice.get.fileName.isDefined,
          createdInvoice.get.fileName.get == "test_invoice.pdf",
          createdInvoice.get.drivers.contains(DriverName(TestData.charlesPersonName))
        )
      },
      test("Création d'une facture sans fileName - devrait avoir fileName = None") {
        for {
          invoiceUuid   <- InvoiceRepository.createInvoice(TestData.sampleInvoiceCreate)
          allInvoices   <- InvoiceRepository.getAllInvoices
          createdInvoice = allInvoices.find(_.name == "Business")
        } yield assertTrue(
          invoiceUuid != null,
          allInvoices.nonEmpty,
          createdInvoice.isDefined,
          createdInvoice.get.fileName.isEmpty
        )
      }
    ) @@ TestAspect.after(
      TestUtils.cleanupData.catchAll(e => ZIO.logError(s"Erreur lors du nettoyage: ${e.getMessage}"))
    ) @@ TestAspect
      .before(
        TestUtils.setupTestData.catchAll(e => ZIO.logError(s"Erreur lors de la configuration: ${e.getMessage}"))
      ) @@ TestAspect.sequential).provideShared(
      InvoiceRepositoryGel.layer,
      PersonRepositoryGel.layer,
      GelDriver.testLayer
    )
}
