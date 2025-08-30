import adapters.GelDriver
import domain.models.PersonCreate
import domain.models.trip.{Trip, TripCreate}
import domain.services.person.PersonService
import domain.services.trip.TripService
import gel.person.PersonRepositoryGel
import gel.trip.TripRepositoryGel
import zio.test.*
import zio.test.Assertion.*
import zio.{Scope, ZIO, ZLayer}

import java.time.LocalDate

object TripServiceTest extends ZIOSpecDefault {
  val personName        = "maé"
  val mae: PersonCreate = PersonCreate(personName)
  var now: LocalDate    = LocalDate.now()

  val tripCreate: TripCreate =
    TripCreate(Some(100), now, now.plusDays(3), "Business", Set(personName), None)

  def spec: Spec[TestEnvironment & Scope, Any] =
    (suiteAll("TripServiceTest in Gel") {

      test("Maé createTrip should create a trip successfully with Maé") {

        for {

          UUID  <- TripService.createTrip(tripCreate)
          trips <- TripService.getAllTrips

        } yield assertTrue(trips.length == 1, trips.head.comments.isEmpty)
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
          updatedTrip = Trip(uuid, now, now.plusDays(3), updatedTripName, Set(personName), None, Some(updatedDistance))
          _          <- TripService.updateTrip(updatedTrip)
          trips      <- TripService.getAllTrips
        } yield assertTrue(
          trips.exists(trip => trip.id == uuid && trip.name == updatedTripName && trip.distance.contains(updatedDistance)),
          trips.length == 1)
      }

      test("updateTrip should add a driver to a trip successfully with Charles and Maé") {

        for {
          uuid       <- TripService.createTrip(tripCreate)
          updatedTrip =
            Trip(uuid, tripCreate.startDate, tripCreate.endDate, tripCreate.name, tripCreate.drivers + "Charles", None, tripCreate.distance)
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

      test("Maé createTrip should create a trip successfully with comment") {

        for {

          UUID  <- TripService.createTrip(tripCreate.copy(comments = Some("comments")))
          trips <- TripService.getAllTrips

        } yield assertTrue(trips.head.comments.contains("comments"))
      }

      test("createTrip with None distance should return None (not Some(0))") {
        for {
          _     <- ZIO.logInfo("[DEBUG_LOG] Creating trip with None distance")
          UUID  <- TripService.createTrip(tripCreate.copy(distance = None))
          trips <- TripService.getAllTrips
          _     <- ZIO.logInfo(s"[DEBUG_LOG] Retrieved trips: ${trips.map(t => s"id=${t.id}, distance=${t.distance}")}")
        } yield assertTrue(
          trips.nonEmpty && trips.head.distance.isEmpty // Should be None, not Some(0)
        )
      }

      test("updateTrip should update a trip successfully with Maé and a new comment") {
        val updatedTripName = "Updated Business Trip"
        val updatedDistance = 200

        for {
          uuid       <- TripService.createTrip(tripCreate)
          updatedTrip = Trip(uuid, now, now.plusDays(3), updatedTripName, Set(personName), Some("comments"), Some(updatedDistance))
          _          <- TripService.updateTrip(updatedTrip)
          trips      <- TripService.getAllTrips
        } yield assertTrue(trips.head.comments.contains("comments"))
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
          val allPersons = Set(PersonCreate("maé"), PersonCreate("brigitte"), PersonCreate("charles"))
          ZIO.foreachPar(allPersons)(person => PersonService.createPerson(person)).catchAll(e => ZIO.logError(e.getMessage))

        }
      @@ TestAspect.sequential).provideShared(
      TripRepositoryGel.layer,
      PersonRepositoryGel.layer,
      GelDriver.testLayer
    )
}
