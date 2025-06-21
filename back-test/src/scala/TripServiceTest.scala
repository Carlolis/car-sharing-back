import adapters.GelDriver
import domain.models.{PersonCreate, TripCreate}
import domain.services.person.PersonService
import domain.services.person.gel.PersonServiceGel
import domain.services.trip.TripService
import domain.services.trip.gel.TripServiceGel
import zio.test.*
import zio.test.Assertion.*
import zio.{ZIO, ZLayer}

import java.time.LocalDate

object TripServiceTest extends ZIOSpecDefault {
  val personName = "Maé"
  val maé        = PersonCreate(personName)
  val tripCreate =
    TripCreate(100, LocalDate.now(), "Business", Set(personName))

  def spec =
    (suiteAll("TripServiceTest in Gel") {

      test("Maé createTrip should create a trip successfully with Maé") {

        for {

          UUID       <- TripService.createTrip(tripCreate)
          tripByUser <- TripService.getAllTrips

        } yield assertTrue(UUID != null, tripByUser.trips.length == 1)
      }
      test("Charles createTrip should create a trip successfully with Charles") {
        val personName = "Charles"

        for {

          UUID       <- TripService.createTrip(tripCreate.copy(drivers = Set(personName)))
          tripByUser <- TripService.getAllTrips

        } yield assertTrue(UUID != null, tripByUser.trips.length == 1)
      }
      test("deleteTrip should delete a trip successfully with Maé") {

        for {

          UUID       <- TripService.createTrip(tripCreate)
          _          <- TripService.deleteTrip(UUID)
          tripByUser <- TripService.getAllTrips

        } yield assertTrue(UUID != null, tripByUser.trips.isEmpty)
      }
    }
      @@ TestAspect
        .after {

          (for {

            allTrips <- TripService.getAllTrips.map(_.trips)
            _        <- ZIO
                          .foreachDiscard(allTrips)(trip => TripService.deleteTrip(trip.id))

          } yield ()).catchAll(e => ZIO.logError(e.getMessage))

        }
      @@ TestAspect
        .before {
          val allPersons = Set(PersonCreate("Maé"), PersonCreate("Brigitte"), PersonCreate("Charles"))
          ZIO.foreachPar(allPersons)(person => PersonService.createPerson(person)).catchAll(e => ZIO.logError(e.getMessage))

        }
      @@ TestAspect.sequential).provideShared(
      TripServiceGel.layer,
      PersonServiceGel.layer,
      GelDriver.testLayer
    )
}
