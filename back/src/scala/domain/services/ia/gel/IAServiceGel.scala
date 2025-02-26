package domain.services.ia.gel

import adapters.GelDriverLive
import domain.models.ia.*
import domain.services.ia.IAService
import domain.services.ia.gel.models.*
import zio.*

import java.util.UUID

case class IAServiceGel(edgeDb: GelDriverLive) extends IAService {
  override def createWriter(writerCreate: WriterCreate): Task[UUID] = edgeDb
    .querySingle(
      classOf[UUID],
      s"""
         |  with new_writer := (insert WriterGel { name := '${writerCreate.name}' }) select new_writer.id;
         |"""
    ).tapBoth(error => ZIO.logError(s"Error creating writer with : $error"), UUID => ZIO.logInfo(s"Created writer with id: $UUID"))

  override def deleteWriter(id: UUID): Task[UUID] = edgeDb
    .querySingle(
      classOf[String],
      s"""
          | delete WriterGel filter .id = <uuid>'$id';
          | select '$id';
          |"""
    )
    .map(id => UUID.fromString(id)).zipLeft(ZIO.logInfo(s"Deleted writer with id: $id"))

  override def getAll: Task[Set[Writer]] = edgeDb
    .query(
      classOf[WriterGel],
      s"""
          | select WriterGel { id, name };
          |"""
    )
    .map(_.toSet).map(writers => writers.map(Writer.fromWriterGel))

  override def getWriter(id: UUID): Task[Writer] = edgeDb
    .querySingle(
      classOf[WriterGel],
      s"""
          | select WriterGel { id, name } filter .id = <uuid>'$id';
          |"""
    ).tap(writer => ZIO.logInfo(s"Got writer with id: $id"))
    .map(Writer.fromWriterGel)

  override def getWriterByName(name: String): Task[Writer] = edgeDb
    .querySingle(
      classOf[WriterGel],
      s"""
          | select WriterGel { id, name } filter .name = '$name';
          |"""
    ).tap(writer => ZIO.logInfo(s"Got writer with name: $name"))
    .map(Writer.fromWriterGel)

  override def createChatSession(writerId: UUID,name:String): Task[UUID] = edgeDb
    .querySingle(
      classOf[UUID],
      s"WITH new_chat := ( INSERT ChatSessionGel { title := '$name' }), update_writer := ( UPDATE WriterGel FILTER .id = <uuid>'$writerId' SET { chats += new_chat }) SELECT new_chat.id;"
    ).tapBoth(error => ZIO.logError(s"Error creating chat session with : $error"), UUID => ZIO.logInfo(s"Created chat session with id: $UUID"))

  override def getChatById(chatId: UUID): Task[ChatSession] = edgeDb
    .querySingle(
      classOf[ChatSessionGel],
      s"""
          | select ChatSessionGel { id, title } filter .id = <uuid>'$chatId';
          |"""
    ).tap(chat => ZIO.logInfo(s"Got chat session with id: $chatId"))
    .map(ChatSession.fromChatSessionGel)
}

object IAServiceGel:
  val layer: ZLayer[GelDriverLive, Nothing, IAService] =
    ZLayer.fromFunction(IAServiceGel(_))
