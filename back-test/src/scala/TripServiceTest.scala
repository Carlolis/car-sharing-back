import adapters.GelDriver
import domain.models.{PersonCreate, Trip, TripCreate}
import domain.services.person.PersonService
import domain.services.person.gel.PersonServiceGel
import domain.services.trip.TripService
import domain.services.trip.gel.TripServiceGel
import zio.test.*
import zio.test.Assertion.*
import zio.{Scope, ZIO, ZLayer}

import java.time.LocalDate

object TripServiceTest extends ZIOSpecDefault {
  val personName             = "Maé"
  val mae: PersonCreate      = PersonCreate(personName)
  val tripCreate: TripCreate =
    TripCreate(100, LocalDate.now(), "Business", Set(personName))

  def spec: Spec[TestEnvironment & Scope, Any] =
    (suiteAll("TripServiceTest in Gel") {

      test("Maé createTrip should create a trip successfully with Maé") {

        for {

          UUID  <- TripService.createTrip(tripCreate)
          trips <- TripService.getAllTrips

        } yield assertTrue(UUID != null, trips.length == 1)
      }
      test("Charles and Maé createTrip should create a trip successfully with Charles and Maé") {
        val personName = "Charles"

        for {

          UUID  <- TripService.createTrip(tripCreate.copy(drivers = tripCreate.drivers + personName))
          trips <- TripService.getAllTrips

        } yield assertTrue(UUID != null, trips.length == 1)
      }
      test("deleteTrip should delete a trip successfully with Maé") {

        for {

          UUID  <- TripService.createTrip(tripCreate)
          _     <- TripService.deleteTrip(UUID)
          trips <- TripService.getAllTrips

        } yield assertTrue(UUID != null, trips.isEmpty)
      }

      test("updateTrip should update a trip successfully with Maé") {
        val updatedTripName = "Updated Business Trip"
        val updatedDistance = 200

        for {
          uuid       <- TripService.createTrip(tripCreate)
          updatedTrip = Trip(uuid, updatedDistance, LocalDate.now(), updatedTripName, Set(personName))
          _          <- TripService.updateTrip(updatedTrip)
          trips      <- TripService.getAllTrips
        } yield assertTrue(
          trips.exists(trip => trip.id == uuid && trip.name == updatedTripName && trip.distance == updatedDistance),
          trips.length == 1)
      }

      test("updateTrip should add a driver to a trip successfully with Charles and Maé") {

        for {
          uuid       <- TripService.createTrip(tripCreate)
          updatedTrip = Trip(uuid, tripCreate.distance, tripCreate.date, tripCreate.name, tripCreate.drivers + "Charles")
          _          <- TripService.updateTrip(updatedTrip)
          trips      <- TripService.getAllTrips
        } yield assertTrue(trips.exists(trip => trip.id == uuid && trip.drivers == tripCreate.drivers + "Charles"), trips.length == 1)
      }
      test("Maé get her trip stats ") {

        for {

          UUID   <- TripService.createTrip(tripCreate)
          trips  <- TripService.getTripStatsByUser(personName)
          trips2 <- TripService.getAllTrips
          _      <- ZIO.logInfo(trips2.toString)
          _      <- ZIO.logInfo(trips.toString)
        } yield assertTrue(UUID != null, trips.totalKilometers == 100)
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
      TripServiceGel.layer,
      PersonServiceGel.layer,
      GelDriver.testLayer
    )
}
