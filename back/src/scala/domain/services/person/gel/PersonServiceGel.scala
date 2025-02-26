package domain.services.person.gel

import adapters.GelDriverLive
import domain.models.*
import domain.services.person.PersonService
import domain.services.person.gel.models.PersonGel
import zio.*

import java.util.UUID

case class PersonServiceGel(edgeDb: GelDriverLive) extends PersonService {
  override def createPerson(personCreate: PersonCreate): Task[UUID] = edgeDb
    .querySingle(
      classOf[UUID],
      s"""
          |  with new_person := (insert PersonGel { name := '${personCreate.name}' }) select new_person.id;
          |"""
    ).tapBoth(error => ZIO.logError(s"Created person with id: $error"), UUID => ZIO.logInfo(s"Created person with id: $UUID"))

  override def deletePerson(id: UUID): Task[UUID] = edgeDb
    .querySingle(
      classOf[String],
      s"""
          | delete PersonGel filter .id = <uuid>'$id';
          | select '$id';
          |"""
    )
    .map(id => UUID.fromString(id)).zipLeft(ZIO.logInfo(s"Deleted person with id: $id"))

  override def getAll: Task[Set[Person]] = edgeDb
    .query(
      classOf[PersonGel],
      s"""
          | select PersonGel { id, name };
          |"""
    )
    .map(_.toSet).map(persons => persons.map(Person.fromPersonGel))

  override def getPerson(id: UUID): Task[Person] = edgeDb
    .querySingle(
      classOf[PersonGel],
      s"""
          | select PersonGel { id, name } filter .id = <uuid>'$id';
          |"""
    ).tap(person => ZIO.logInfo(s"Got person with id: $id"))
    .map(Person.fromPersonGel)

  override def getPersonByName(name: String): Task[Person] = edgeDb
    .querySingle(
      classOf[PersonGel],
      s"""
          | select PersonGel { id, name } filter .name = '$name';
          |"""
    ).tap(person => ZIO.logInfo(s"Got person with name: $name"))
    .map(Person.fromPersonGel)
}

object PersonServiceGel:
  val layer: ZLayer[GelDriverLive, Nothing, PersonService] =
    ZLayer.fromFunction(PersonServiceGel(_))
