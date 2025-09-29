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
  import common.TestData
  import common.TestUtils

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
      test("Création d'une facture - Maé devrait créer une facture avec succès avec un montant non entier") {
        for {
          invoiceUuid <- InvoiceRepository.createInvoice(TestData.sampleInvoiceCreate.copy(amount = 100.5))
          allInvoices <- InvoiceRepository.getAllInvoices
        } yield assertTrue(
          invoiceUuid != null,
          allInvoices.length == 1,
          allInvoices.head.kind == TestData.kind,
          allInvoices.head.amount == 100.5
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
          createdInvoice.get.driver == DriverName(TestData.charlesPersonName)
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
      },
      test("Création d'un remboursement - Maé devrait créer un remboursement vers Charles avec succès") {
        for {
          invoiceUuid <-
            InvoiceRepository.createInvoice(TestData
              .sampleInvoiceCreate.copy(toDriver = Some(DriverName(TestData.charlesPersonName)), kind = "remboursement"))
          allInvoices <- InvoiceRepository.getAllInvoices
        } yield assertTrue(
          invoiceUuid != null,
          allInvoices.length == 1,
          allInvoices.head.kind == "remboursement",
          allInvoices.head.toDriver.isDefined,
          allInvoices.head.toDriver.get == DriverName(TestData.charlesPersonName),
          allInvoices.head.kind == "remboursement"
        )
      }
    ) @@ TestAspect.after(
      TestUtils.cleanupData.catchAll(e => ZIO.logError(s"Erreur lors du nettoyage: ${e.getMessage}"))
    )
      @@ TestAspect
        .before(
          TestUtils.setupTestData.catchAll(e => ZIO.logError(s"Erreur lors de la configuration: ${e.getMessage}"))
        ) @@ TestAspect.sequential).provideShared(
      InvoiceRepositoryGel.layer,
      PersonRepositoryGel.layer,
      GelDriver.testLayer
    )
}
