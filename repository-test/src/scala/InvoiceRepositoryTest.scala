import adapters.GelDriver
import domain.models.PersonCreate
import domain.models.invoice.{DriverName, Invoice, InvoiceCreate, Reimbursement}
import domain.services.invoice.repository.InvoiceRepository
import domain.services.person.PersonService
import gel.invoice.InvoiceRepositoryGel
import gel.person.PersonRepositoryGel
import zio.test.*
import zio.test.Assertion.*
import zio.{IO, Scope, ZIO, ZLayer}

import java.time.LocalDate
import java.util.UUID

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

    val expectedReimbursementAmount = 33
  }

  // Utilitaires de test
  object TestUtils {
    def findReimbursementByDriver(reimbursements: Set[Reimbursement], driverName: String): IO[Option[Nothing], Reimbursement] =
      ZIO.fromOption(
        reimbursements.find(r =>
          // Adaptation nécessaire selon la structure réelle de vos objets de remboursement
          r.driverName == DriverName(driverName)))

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
      test("Calcul des remboursements - Distribution équitable entre 3 conducteurs") {
        for {
          _              <- InvoiceRepository.createInvoice(TestData.sampleInvoiceCreate)
          reimbursements <- InvoiceRepository.getReimbursementProposal

          maeReimbursement      <- TestUtils.findReimbursementByDriver(reimbursements, TestData.maePersonName)
          charlesReimbursement  <- TestUtils.findReimbursementByDriver(reimbursements, TestData.charlesPersonName)
          brigitteReimbursement <- TestUtils.findReimbursementByDriver(reimbursements, TestData.brigittePersonName)

        } yield {
          val baseAssertions = assertTrue(
            reimbursements.size == 3,
            maeReimbursement.totalAmount == 0,
            charlesReimbursement.totalAmount == TestData.expectedReimbursementAmount,
            brigitteReimbursement.totalAmount == TestData.expectedReimbursementAmount
          )

          val maeDistributionAssertion = assert(
            maeReimbursement.to
          )(
            equalTo(
              Map(
                DriverName(TestData.brigittePersonName) -> 0,
                DriverName(TestData.charlesPersonName)  -> 0
              )))

          val charlesDistributionAssertion = assert(
            charlesReimbursement.to
          )(
            equalTo(
              Map(
                DriverName(TestData.brigittePersonName) -> 0,
                DriverName(TestData.maePersonName)      -> TestData.expectedReimbursementAmount
              )))

          val brigitteDistributionAssertion = assert(
            brigitteReimbursement.to
          )(
            equalTo(
              Map(
                DriverName(TestData.maePersonName)     -> TestData.expectedReimbursementAmount,
                DriverName(TestData.charlesPersonName) -> 0
              )))

          baseAssertions &&
          maeDistributionAssertion &&
          charlesDistributionAssertion &&
          brigitteDistributionAssertion
        }
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
