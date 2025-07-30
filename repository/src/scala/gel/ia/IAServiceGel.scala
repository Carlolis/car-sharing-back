package gel.ia

import adapters.GelDriverLive
import domain.models.ia.*
import domain.services.ia.IAService
import gel.ia.models.{ChatSessionGel, WriterGel}
import zio.*

import java.util.UUID

case class IAServiceGel(gelDriverLive: GelDriverLive) extends IAService {
  override def createWriter(writerCreate: WriterCreate): Task[UUID] = gelDriverLive
    .querySingle(
      classOf[UUID],
      s"""
         |  with new_writer := (insert WriterGel { name := '${writerCreate.name}' }) select new_writer.id;
         |"""
    ).tapBoth(error => ZIO.logError(s"Error creating writer with : $error"), UUID => ZIO.logInfo(s"Created writer with id: $UUID"))

  override def getAllWriters: Task[Set[Writer]] = gelDriverLive
    .query(
      classOf[WriterGel],
      s"""
          | select WriterGel { id, name };
          |"""
    )
    .map(_.toSet).map(writers => writers.map(WriterGel.fromWriterGel))

  override def getWriter(id: UUID): Task[Writer] = gelDriverLive
    .querySingle(
      classOf[WriterGel],
      s"""
          | select WriterGel { id, name } filter .id = <uuid>'$id';
          |"""
    ).tap(writer => ZIO.logInfo(s"Got writer with id: $id"))
    .map(WriterGel.fromWriterGel)

  override def getWriterByName(name: String): Task[Writer] = gelDriverLive
    .querySingle(
      classOf[WriterGel],
      s"""
          | select WriterGel { id, name } filter .name = '$name';
          |"""
    ).tap(writer => ZIO.logInfo(s"Got writer with name: $name"))
    .map(WriterGel.fromWriterGel)

  override def createChatSession(writerId: UUID, name: String): Task[UUID] = gelDriverLive
    .querySingle(
      classOf[UUID],
      s"WITH new_chat := ( INSERT ChatSessionGel { title := '$name' }), update_writer := ( UPDATE WriterGel FILTER .id = <uuid>'$writerId' SET { chats += new_chat }) SELECT new_chat.id;"
    ).tapBoth(
      error => ZIO.logError(s"Error creating chat session with : $error"),
      UUID => ZIO.logInfo(s"Created chat session with id: $UUID"))

  override def getChatById(chatId: UUID): Task[ChatSession] = gelDriverLive
    .querySingle(
      classOf[ChatSessionGel],
      s"""
          | select ChatSessionGel { id, title,messages : {answer, question } } filter .id = <uuid>'$chatId';
          |"""
    ).tap(chat => ZIO.logInfo(s"Got chat session with id: $chatId"))
    .map(ChatSessionGel.fromChatSessionGel)

  override def deleteChatById(chatId: UUID): Task[UUID] = gelDriverLive
    .querySingle(
      classOf[String],
      s"""
          | delete ChatSessionGel filter .id = <uuid>'$chatId';
          | select '$chatId';
          |"""
    ).map(id => UUID.fromString(id)).zipLeft(ZIO.logInfo(s"Deleted Chat with id: $chatId"))

  override def deleteWriter(id: UUID): Task[UUID] = gelDriverLive
    .querySingle(
      classOf[String],
      s"""
         | delete WriterGel filter .id = <uuid>'$id';
         | select 'done';
         |"""
    )
    .as(id).zipLeft(ZIO.logInfo(s"Deleted writer with id: $id"))

  override def getAllChats: Task[Set[ChatSession]] = gelDriverLive
    .query(
      classOf[ChatSessionGel],
      s"""
          | select ChatSessionGel { id, title, messages };
          |"""
    )
    .map(_.toSet).map(chats => chats.map(ChatSessionGel.fromChatSessionGel))

  override def addMessageToChat(chatId: UUID, message: Message): Task[UUID] = gelDriverLive
    .querySingle(
      classOf[String],
      s"""
         |update default::ChatSessionGel
         |filter .id =  <uuid>'$chatId'
         |set { messages += (insert default::MessageGel
         |{ question := '${message.question}', answer := '${message.answer}' }
         |)};
         | select '$chatId';
         | """.stripMargin
    ).as(chatId).zipLeft(ZIO.logInfo(s"Added message to chat with id: $chatId"))
}

object IAServiceGel:
  val layer: ZLayer[GelDriverLive, Nothing, IAService] =
    ZLayer.fromFunction(IAServiceGel(_))
