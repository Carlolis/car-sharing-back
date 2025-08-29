package inMemoryService

import domain.models.{Person, PersonCreate}
import domain.services.person.PersonService
import zio.*

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

class InMemoryPersonRepository extends PersonService {
  val maePersonName      = "maé"
  val charlesPersonName  = "charles"
  val brigittePersonName = "brigitte"
  var kind               = "péage"

  val mae                                        = PersonCreate(maePersonName)
  val charles                                    = PersonCreate(charlesPersonName)
  val brigitte                                   = PersonCreate(brigittePersonName)
  private val persons: mutable.Map[UUID, Person] = new ConcurrentHashMap[UUID, Person]()
    .asScala
    .addOne((UUID.randomUUID(), Person(maePersonName, UUID.randomUUID())))
    .addOne((UUID.randomUUID(), Person(charlesPersonName, UUID.randomUUID())))
    .addOne((UUID.randomUUID(), Person(brigittePersonName, UUID.randomUUID())))

  override def createPerson(personCreate: PersonCreate): Task[UUID] = {
    val id     = UUID.randomUUID()
    val person = Person(
      name = personCreate.name,
      id = id
    )

    ZIO
      .attempt {
        persons.put(id, person)
        id
      }.tapBoth(
        error => ZIO.logError(s"Failed to create person: ${error.getMessage}"),
        id => ZIO.logInfo(s"Created person with id: $id")
      )
  }

  override def deletePerson(id: UUID): Task[UUID] =
    ZIO
      .attempt {
        persons.remove(id) match {
          case Some(_) => id
          case None    => throw new RuntimeException(s"Person with id $id not found")
        }
      }.tapBoth(
        error => ZIO.logError(s"Failed to delete person with id $id: ${error.getMessage}"),
        _ => ZIO.logInfo(s"Deleted person with id: $id")
      )

  override def getAll: Task[Set[Person]] =
    ZIO
      .succeed(persons.values.toSet)
      .tap(persons => ZIO.logInfo(s"Retrieved ${persons.size} persons"))

  override def getPerson(id: UUID): Task[Person] =
    ZIO
      .attempt {
        persons.get(id) match {
          case Some(person) => person
          case None         => throw new RuntimeException(s"Person with id $id not found")
        }
      }.tapBoth(
        error => ZIO.logError(s"Failed to get person with id $id: ${error.getMessage}"),
        _ => ZIO.logInfo(s"Got person with id: $id")
      )

  override def getPersonByName(name: String): Task[Person] =
    ZIO
      .attempt {
        persons.values.find(_.name == name) match {
          case Some(person) => person
          case None         => throw new RuntimeException(s"Person with name '$name' not found")
        }
      }.tapBoth(
        error => ZIO.logError(s"Failed to get person with name '$name': ${error.getMessage}"),
        _ => ZIO.logInfo(s"Got person with name: $name")
      )
}

object InMemoryPersonRepository {
  val layer: ZLayer[Any, Nothing, InMemoryPersonRepository] =
    ZLayer.succeed(new InMemoryPersonRepository)
}
