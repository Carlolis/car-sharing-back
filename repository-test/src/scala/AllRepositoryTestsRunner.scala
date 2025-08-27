import adapters.GelDriver
import domain.services.ia.IAService
import domain.services.invoice.repository.InvoiceRepository
import domain.services.person.PersonService
import domain.services.trip.TripService
import gel.ia.IAServiceGel
import gel.invoice.InvoiceRepositoryGel
import gel.person.PersonRepositoryGel
import gel.trip.TripRepositoryGel
import zio.*
import zio.test.*

/**
 * Unified test runner that executes all repository tests in a single suite.
 * 
 * This class aggregates all test suites from the repository-test module:
 * - IAServiceTest: Tests AI service functionality
 * - InvoiceRepositoryTest: Tests invoice repository operations  
 * - PersonServiceTest: Tests person service operations
 * - TripServiceTest: Tests trip service operations
 * 
 * Usage: bleep test repository-test
 * This will run this unified test runner along with all individual tests.
 */
object AllRepositoryTestsRunner extends ZIOSpecDefault {
  
  def spec: Spec[TestEnvironment & Scope, Any] =
    suite("All Repository Tests - Unified Runner")(
      // Include all test suites - each provides its own layers
      IAServiceTest.spec,
      InvoiceRepositoryTest.spec,
      PersonServiceTest.spec,
      TripServiceTest.spec
    ) @@ TestAspect.sequential // Run sequentially to avoid conflicts
}