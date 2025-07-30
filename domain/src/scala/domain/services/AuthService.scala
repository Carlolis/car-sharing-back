package domain.services

import domain.models.{Person, Token}
import zio.*

trait AuthService {
  /*   def register(user: UserCreate): Task[Person]*/
  def login(login: String): Task[Token]
  def authenticate(token: String): Task[Person]
}

object AuthService:
  def login(login: String): RIO[AuthService, Token]         =
    ZIO.serviceWithZIO[AuthService](_.login(login))
  def authenticate(token: String): RIO[AuthService, Person] =
    ZIO.serviceWithZIO[AuthService](_.authenticate(token))
