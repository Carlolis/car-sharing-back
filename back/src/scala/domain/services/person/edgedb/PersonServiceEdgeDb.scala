package domain.services.person.edgedb

import adapters.EdgeDbDriverLive
import domain.models.*
import domain.services.person.PersonService
import domain.services.person.edgedb.models.PersonEdge
import zio.*

import java.util.UUID

case class PersonServiceEdgeDb(edgeDb: EdgeDbDriverLive) extends PersonService {
  override def createPerson(personCreate: PersonCreate): Task[UUID] = edgeDb
    .querySingle(
      classOf[UUID],
      s"""
          |  with new_person := (insert PersonEdge { name := '${personCreate.name}' }) select new_person.id;
          |"""
    ).tapBoth(error => ZIO.logError(s"Created person with id: $error"), UUID => ZIO.logInfo(s"Created person with id: $UUID"))

  override def deletePerson(id: UUID): Task[UUID] = edgeDb
    .querySingle(
      classOf[String],
      s"""
          | delete PersonEdge filter .id = <uuid>'$id';
          | select '$id';
          |"""
    )
    .map(id => UUID.fromString(id)).zipLeft(ZIO.logInfo(s"Deleted person with id: $id"))

  override def getAll: Task[Set[Person]] = edgeDb
    .query(
      classOf[PersonEdge],
      s"""
          | select PersonEdge { id, name };
          |"""
    )
    .map(_.toSet).map(persons => persons.map(Person.fromPersonEdge))

  override def getPerson(id: UUID): Task[Person] = edgeDb
    .querySingle(
      classOf[PersonEdge],
      s"""
          | select PersonEdge { id, name } filter .id = <uuid>'$id';
          |"""
    ).tap(person => ZIO.logInfo(s"Got person with id: $id"))
    .map(Person.fromPersonEdge)

  override def getPersonByName(name: String): Task[Person] = edgeDb
    .querySingle(
      classOf[PersonEdge],
      s"""
          | select PersonEdge { id, name } filter .name = '$name';
          |"""
    ).tap(person => ZIO.logInfo(s"Got person with name: $name"))
    .map(Person.fromPersonEdge)
}

object PersonServiceEdgeDb:
  val layer: ZLayer[EdgeDbDriverLive, Nothing, PersonService] =
    ZLayer.fromFunction(PersonServiceEdgeDb(_))
