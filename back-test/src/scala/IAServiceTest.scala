import adapters.GelDriver
import domain.models.ia.*

import domain.services.ia.IAService
import domain.services.ia.gel.IAServiceGel
import zio.ZIO
import zio.test.*

object IAServiceTest extends ZIOSpecDefault {
  val personName        = "Maé"
  val maé: WriterCreate = WriterCreate(personName)

  def spec =
    (suiteAll("IAServiceTest in Gel") {
      val personNameCharles = "charles"
      test("Create Maé writer") {

        for {

          UUID   <- IAService.createWriter(maé)
          person <- IAService.getWriter(UUID)

        } yield assertTrue(UUID != null, person.name == personName)
      }

      test("Delete Maé writer") {

        for {

          UUID     <- IAService.createWriter(maé)
          _        <- IAService.deleteWriter(UUID)
          notFound <- IAService.getWriter(UUID).either

        } yield assertTrue(notFound.isLeft)
      }
      test("Get all writers") {

        val charles: WriterCreate = WriterCreate(personNameCharles)
        for {
          _          <- IAService.createWriter(maé)
          _          <- IAService.createWriter(charles)
          allWriters <- IAService.getAllWriters

        } yield assertTrue(allWriters.nonEmpty)
      }

      test("Create a chat session") {

        val charles: WriterCreate = WriterCreate(personNameCharles)
        for {
          maéUUID <- IAService.createWriter(maé)
          chatUUID <- IAService.createChatSession(maéUUID,"chat name")
          chatSession <- IAService.getChatById(chatUUID)
          _<- ZIO.logInfo("Chat Session "+chatSession)
        } yield assertTrue(chatSession.id == chatUUID)
      }

      test("Delete a chat session") {

        val charles: WriterCreate = WriterCreate(personNameCharles)
        for {
          maéUUID <- IAService.createWriter(maé)
          chatUUID <- IAService.createChatSession(maéUUID, "chat name")
          chatSession <- IAService.deleteChatById(chatUUID)
          notFound <- IAService.getChatById(chatUUID).either
          _ <- ZIO.logInfo("Chat Session " + chatSession)
        } yield assertTrue(notFound.isLeft)
      }
    }
      @@ TestAspect
        .after {

          (for {

            allWriters <- IAService.getAllWriters
            _          <- ZIO.foreachDiscard(allWriters)(person => IAService.deleteWriter(person.id))
            allChat <- IAService.getAllChats
            _          <- ZIO.foreachDiscard(allChat)(chat => IAService.deleteChatById(chat.id))

          } yield ()).catchAll(e => ZIO.logError(e.getMessage))

        }
      @@ TestAspect.sequential).provideShared(
      IAServiceGel.layer,
      GelDriver.testLayer
    )
}
