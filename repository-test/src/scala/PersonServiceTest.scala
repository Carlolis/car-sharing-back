import adapters.GelDriver
import domain.models.PersonCreate
import domain.services.person.PersonService
import gel.person.PersonRepositoryGel
import zio.ZIO
import zio.test.*

object PersonServiceTest extends ZIOSpecDefault {
  val personName        = "Maé"
  val maé: PersonCreate = PersonCreate(personName)

  def spec =
    (suiteAll("TripServiceTest in Gel") {

      test("Create Maé person") {

        for {

          UUID   <- PersonService.createPerson(maé)
          person <- PersonService.getPerson(UUID)

        } yield assertTrue(UUID != null, person.name == personName)
      }

      test("Delete Maé person") {

        for {

          UUID     <- PersonService.createPerson(maé)
          _        <- PersonService.deletePerson(UUID)
          notFound <- PersonService.getPerson(UUID).either

        } yield assertTrue(notFound.isLeft)
      }
      test("Get all persons") {
        val personName            = "charles"
        val charles: PersonCreate = PersonCreate(personName)
        for {
          _          <- PersonService.createPerson(maé)
          _          <- PersonService.createPerson(charles)
          allPersons <- PersonService.getAll

        } yield assertTrue(allPersons.nonEmpty)
      }
    }
      @@ TestAspect
        .after {

          (for {

            allPersons <- PersonService.getAll
            _          <- ZIO.foreachDiscard(allPersons)(person => PersonService.deletePerson(person.id))

          } yield ()).catchAll(e => ZIO.logError(e.getMessage))

        }
      @@ TestAspect.sequential).provideShared(
      PersonRepositoryGel.layer,
      GelDriver.testLayer
    )
}
