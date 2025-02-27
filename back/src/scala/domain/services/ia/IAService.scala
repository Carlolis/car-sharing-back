package domain.services.ia

import domain.models.ia.*

import zio.*

import java.util.UUID

trait IAService {
  def createWriter(writerCreate: WriterCreate): Task[UUID]
  def deleteWriter(id: UUID): Task[UUID]
  def getAllWriters: Task[Set[Writer]]
  def getWriter(id: UUID): Task[Writer]
  def getWriterByName(name: String): Task[Writer]
  def createChatSession(writerId :UUID,name:String): Task[UUID]
  def getChatById(chatId: UUID): Task[ChatSession]
  def deleteChatById(chatId: UUID): Task[UUID]
  def getAllChats: Task[Set[ChatSession]]
}

object IAService:
  def createWriter(writerCreate: WriterCreate): RIO[IAService, UUID] =
    ZIO.serviceWithZIO[IAService](_.createWriter(writerCreate))
  def deleteWriter(id: UUID): RIO[IAService, UUID]                   =
    ZIO.serviceWithZIO[IAService](_.deleteWriter(id))
  def getAllWriters: RIO[IAService, Set[Writer]]                            =
    ZIO.serviceWithZIO[IAService](_.getAllWriters)
  def getWriter(id: UUID): RIO[IAService, Writer]                    =
    ZIO.serviceWithZIO[IAService](_.getWriter(id))
  def getWriterByName(name: String): RIO[IAService, Writer]          =
    ZIO.serviceWithZIO[IAService](_.getWriterByName(name))
  def createChatSession(writerId: UUID,name:String): RIO[IAService, UUID]        =
    ZIO.serviceWithZIO[IAService](_.createChatSession(writerId,name:String))
  def getChatById(chatId: UUID): RIO[IAService, ChatSession]         =
    ZIO.serviceWithZIO[IAService](_.getChatById(chatId))
  def deleteChatById(chatId: UUID): RIO[IAService, UUID]             =
    ZIO.serviceWithZIO[IAService](_.deleteChatById(chatId))
  def getAllChats: RIO[IAService, Set[ChatSession]]                            =
    ZIO.serviceWithZIO[IAService](_.getAllChats)
