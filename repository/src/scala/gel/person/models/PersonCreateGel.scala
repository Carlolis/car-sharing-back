package gel.person.models

import com.geldata.driver.annotations.{GelDeserializer, GelType}
import domain.models.*

@GelType
case class PersonCreateGel @GelDeserializer() (var name: String)

object PersoPersonCreateGel {
  def toPersonGel(person: PersonCreate): PersonCreateGel =
    PersonCreateGel(person.name)
}

@GelType
case class PersonGel @GelDeserializer() (var name: String, var id: java.util.UUID)

object PersonGel {
  def toPersonGel(person: Person): PersonGel =
    PersonGel(person.name, person.id)

  def fromPersonGel(personGel: PersonGel): Person =
    Person(personGel.name, personGel.id)
}
