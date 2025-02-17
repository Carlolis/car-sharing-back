package domain.services.ia

import domain.models.ia.*

import zio.*

import java.util.UUID

trait IAService {
  def createWriter(writerCreate: WriterCreate): Task[UUID]
  def deleteWriter(id: UUID): Task[UUID]
  def getAll: Task[Set[Writer]]
  def getWriter(id: UUID): Task[Writer]
  def getWriterByName(name: String): Task[Writer]
}

object IAService:
  def createWriter(writerCreate: WriterCreate): RIO[IAService, UUID] =
    ZIO.serviceWithZIO[IAService](_.createWriter(writerCreate))
  def deleteWriter(id: UUID): RIO[IAService, UUID]                   =
    ZIO.serviceWithZIO[IAService](_.deleteWriter(id))
  def getAll: RIO[IAService, Set[Writer]]                            =
    ZIO.serviceWithZIO[IAService](_.getAll)
  def getWriter(id: UUID): RIO[IAService, Writer]                    =
    ZIO.serviceWithZIO[IAService](_.getWriter(id))
  def getWriterByName(name: String): RIO[IAService, Writer]          =
    ZIO.serviceWithZIO[IAService](_.getWriterByName(name))
