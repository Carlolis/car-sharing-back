package domain.services.person

import domain.models.*
import zio.*

import java.util.UUID

trait PersonService {
  def createPerson(personCreate: PersonCreate): Task[UUID]
  def deletePerson(id: UUID): Task[UUID]
  def getAll: Task[Set[Person]]
  def getPerson(id: UUID): Task[Person]
  def getPersonByName(name: String): Task[Person]
}

object PersonService:
  def createPerson(personCreate: PersonCreate): RIO[PersonService, UUID] =
    ZIO.serviceWithZIO[PersonService](_.createPerson(personCreate))
  def deletePerson(id: UUID): RIO[PersonService, UUID]                   =
    ZIO.serviceWithZIO[PersonService](_.deletePerson(id))
  def getAll: RIO[PersonService, Set[Person]]                            =
    ZIO.serviceWithZIO[PersonService](_.getAll)
  def getPerson(id: UUID): RIO[PersonService, Person]                    =
    ZIO.serviceWithZIO[PersonService](_.getPerson(id))
  def getPersonByName(name: String): RIO[PersonService, Person]          =
    ZIO.serviceWithZIO[PersonService](_.getPersonByName(name))
