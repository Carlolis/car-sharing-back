import adapters.EdgeDbDriver
import domain.models.ia.*

import domain.services.ia.IAService
import domain.services.ia.edgedb.IAServiceEdgeDb
import zio.ZIO
import zio.test.*

object IAServiceTest extends ZIOSpecDefault {
  val personName        = "Maé"
  val maé: WriterCreate = WriterCreate(personName)

  def spec =
    (suiteAll("IAServiceTest in EdgeDb") {

      test("Create Maé writer") {

        for {

          UUID   <- IAService.createWriter(maé)
          person <- IAService.getWriter(UUID)

        } yield assertTrue(UUID != null, person.name == personName)
      }

      test("Delete Maé person") {

        for {

          UUID     <- IAService.createWriter(maé)
          _        <- IAService.deleteWriter(UUID)
          notFound <- IAService.getWriter(UUID).either

        } yield assertTrue(notFound.isLeft)
      }
      test("Get all persons") {
        val personName            = "charles"
        val charles: WriterCreate = WriterCreate(personName)
        for {
          _          <- IAService.createWriter(maé)
          _          <- IAService.createWriter(charles)
          allWriters <- IAService.getAll

        } yield assertTrue(allWriters.nonEmpty)
      }
    }
      @@ TestAspect
        .after {

          (for {

            allWriters <- IAService.getAll
            _          <- ZIO.foreachDiscard(allWriters)(person => IAService.deleteWriter(person.id))

          } yield ()).catchAll(e => ZIO.logError(e.getMessage))

        }
      @@ TestAspect.sequential).provideShared(
      IAServiceEdgeDb.layer,
      EdgeDbDriver.testLayer
    )
}
