package domain.services.ia.edgedb

import adapters.EdgeDbDriverLive
import domain.models.ia.*
import domain.services.ia.IAService
import domain.services.ia.edgedb.models.WriterEdge
import zio.*

import java.util.UUID

case class IAServiceEdgeDb(edgeDb: EdgeDbDriverLive) extends IAService {
  override def createWriter(writerCreate: WriterCreate): Task[UUID] = edgeDb
    .querySingle(
      classOf[UUID],
      s"""
         |  with new_writer := (insert WriterEdge { name := '${writerCreate.name}' }) select new_writer.id;
         |"""
    ).tapBoth(error => ZIO.logError(s"Error creating writer with : $error"), UUID => ZIO.logInfo(s"Created writer with id: $UUID"))

  override def deleteWriter(id: UUID): Task[UUID] = edgeDb
    .querySingle(
      classOf[String],
      s"""
          | delete WriterEdge filter .id = <uuid>'$id';
          | select '$id';
          |"""
    )
    .map(id => UUID.fromString(id)).zipLeft(ZIO.logInfo(s"Deleted writer with id: $id"))

  override def getAll: Task[Set[Writer]] = edgeDb
    .query(
      classOf[WriterEdge],
      s"""
          | select WriterEdge { id, name };
          |"""
    )
    .map(_.toSet).map(writers => writers.map(Writer.fromWriterEdge))

  override def getWriter(id: UUID): Task[Writer] = edgeDb
    .querySingle(
      classOf[WriterEdge],
      s"""
          | select WriterEdge { id, name } filter .id = <uuid>'$id';
          |"""
    ).tap(writer => ZIO.logInfo(s"Got writer with id: $id"))
    .map(Writer.fromWriterEdge)

  override def getWriterByName(name: String): Task[Writer] = edgeDb
    .querySingle(
      classOf[WriterEdge],
      s"""
          | select WriterEdge { id, name } filter .name = '$name';
          |"""
    ).tap(writer => ZIO.logInfo(s"Got writer with name: $name"))
    .map(Writer.fromWriterEdge)
}

object IAServiceEdgeDb:
  val layer: ZLayer[EdgeDbDriverLive, Nothing, IAService] =
    ZLayer.fromFunction(IAServiceEdgeDb(_))
