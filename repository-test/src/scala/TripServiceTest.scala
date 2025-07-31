import adapters.GelDriver
import domain.models.{PersonCreate, Trip, TripCreate}
import domain.services.person.PersonService
import domain.services.trip.TripService
import gel.person.PersonRepositoryGel
import gel.trip.TripRepositoryGel
import zio.test.*
import zio.test.Assertion.*
import zio.{Scope, ZIO, ZLayer}

import java.time.LocalDate

object TripServiceTest extends ZIOSpecDefault {
  val personName             = "Maé"
  val mae: PersonCreate      = PersonCreate(personName)
  var now                    = LocalDate.now()
  val tripCreate: TripCreate =
    TripCreate(100, now, now.plusDays(3), "Business", Set(personName))

  def spec: Spec[TestEnvironment & Scope, Any] =
    (suiteAll("TripServiceTest in Gel") {

      test("Maé createTrip should create a trip successfully with Maé") {

        for {

          UUID  <- TripService.createTrip(tripCreate)
          trips <- TripService.getAllTrips

        } yield assertTrue(trips.length == 1)
      }
      test("Charles and Maé createTrip should create a trip successfully with Charles and Maé") {
        val personName = "Charles"

        for {

          UUID  <- TripService.createTrip(tripCreate.copy(drivers = tripCreate.drivers + personName))
          trips <- TripService.getAllTrips

        } yield assertTrue(trips.length == 1)
      }
      test("deleteTrip should delete a trip successfully with Maé") {

        for {

          UUID  <- TripService.createTrip(tripCreate)
          _     <- TripService.deleteTrip(UUID)
          trips <- TripService.getAllTrips

        } yield assertTrue(trips.isEmpty)
      }

      test("updateTrip should update a trip successfully with Maé") {
        val updatedTripName = "Updated Business Trip"
        val updatedDistance = 200

        for {
          uuid       <- TripService.createTrip(tripCreate)
          updatedTrip = Trip(uuid, updatedDistance, now, now.plusDays(3), updatedTripName, Set(personName))
          _          <- TripService.updateTrip(updatedTrip)
          trips      <- TripService.getAllTrips
        } yield assertTrue(
          trips.exists(trip => trip.id == uuid && trip.name == updatedTripName && trip.distance == updatedDistance),
          trips.length == 1)
      }

      test("updateTrip should add a driver to a trip successfully with Charles and Maé") {

        for {
          uuid       <- TripService.createTrip(tripCreate)
          updatedTrip =
            Trip(uuid, tripCreate.distance, tripCreate.startDate, tripCreate.endDate, tripCreate.name, tripCreate.drivers + "Charles")
          _          <- TripService.updateTrip(updatedTrip)
          trips      <- TripService.getAllTrips
        } yield assertTrue(trips.exists(trip => trip.id == uuid && trip.drivers == tripCreate.drivers + "Charles"), trips.length == 1)
      }
      test("Maé get her trip stats ") {

        for {
          UUID   <- TripService.createTrip(tripCreate.copy(drivers = Set("Charles")))
          UUID   <- TripService.createTrip(tripCreate)
          trips  <- TripService.getTripStatsByUser(personName)
          trips2 <- TripService.getAllTrips
          _      <- ZIO.logInfo(trips2.toString)
          _      <- ZIO.logInfo(trips.toString)
        } yield assertTrue(trips.totalKilometers == 100)
      }

    }
      @@ TestAspect
        .after {

          (for {

            allTrips <- TripService.getAllTrips
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
      TripRepositoryGel.layer,
      PersonRepositoryGel.layer,
      GelDriver.testLayer
    )
}
