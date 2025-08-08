import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import config.{AppConfig, AuthConfig}
import domain.models.{Person, Token}
import domain.services.AuthService
import domain.services.person.PersonService
import zio.*

import java.time.Instant

class AuthServiceLive(personService: PersonService, appConfig: AppConfig) extends AuthService:
  private val authConfig = appConfig.auth
  private val algorithm  = Algorithm.HMAC256(authConfig.secretKey)

  /*   override def register(userCreate: UserCreate): Task[Person] = {
     for {
       userMap <- users.get
       _ <- ZIO
         .fail(new Exception("Username already taken"))
         .when(userMap.contains(userCreate.username))
       newUser = Person(
         Some(userMap.size + 1L),
         userCreate.username,
         userCreate.email,
         userCreate.password
       )
       _ <- users.update(_ + (userCreate.username -> newUser))
     } yield newUser
   }*/

  override def login(login: String): Task[Token] =
    (for {
      user  <- personService.getPersonByName(login).orElseFail(new Exception("User not found"))
      token <- ZIO.attempt(createToken(user.name)).map(Token.apply)

      _ <- ZIO.logInfo("Login success !")
    } yield token).tapError(error => ZIO.logError(error.getMessage))

  override def authenticate(token: String): Task[Person] = {
    val verifier = JWT.require(algorithm).build()
    ZIO
      .attempt(verifier.verify(token)).flatMap(jwt => ZIO.attempt(jwt.getSubject)).mapError(error =>
        new Exception(error.getMessage)).flatMap(personService.getPersonByName)

  }.tapError(error => ZIO.logError(error.getMessage))

  private def createToken(username: String): String =
    JWT
      .create()
      .withSubject(username)
      .withIssuedAt(Instant.now())
      .withExpiresAt(Instant.now().plusSeconds(authConfig.tokenExpirationSeconds))
      .sign(algorithm)
object AuthServiceLive:
  val layer: ZLayer[PersonService & AppConfig, Nothing, AuthServiceLive] =
    ZLayer.fromFunction(AuthServiceLive(_, _))
