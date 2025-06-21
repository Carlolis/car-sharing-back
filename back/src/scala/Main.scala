import adapters.GelDriver
import api.TripEndpointsLive.tripEndpoints
import api.ia.IaRoutes.iaEndpoints
import api.{TripEndpointsLive, swagger}
import domain.services.ia.gel.IAServiceGel
import domain.services.person.gel.PersonServiceGel
import domain.services.services.AuthServiceLive
import domain.services.trip.TripService
import domain.services.trip.gel.TripServiceGel
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zio.*
import zio.http.*

object Main extends ZIOAppDefault:
  override def run =
    val port = 8081
    (for

      _      <- ZIO.log(s"Swagger UI available at http://localhost:$port/docs")
      _      <- ZIO.log(s"Server starting on http://localhost:$port")
      httpApp = ZioHttpInterpreter(options).toHttp(
                  tripEndpoints
                ) ++ ZioHttpInterpreter(options).toHttp(
                  swagger
                ) ++ ZioHttpInterpreter(options).toHttp(
                  iaEndpoints
                )
      _      <- Server
                  .install(httpApp)
      _      <- ZIO.never
    yield ()).provide(
      Server.defaultWithPort(port),
      IAServiceGel.layer,
      TripServiceGel.layer,
      PersonServiceGel.layer,
      GelDriver.layer,
      AuthServiceLive.layer
    )
