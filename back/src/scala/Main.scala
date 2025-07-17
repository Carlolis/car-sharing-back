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
import zio.http.netty.NettyConfig
import zio.http.netty.NettyConfig.LeakDetectionLevel

object Main extends ZIOAppDefault:
  override def run =

    val port   = sys.env("PORT").toInt
    val config = Server
      .Config.default
      .port(port)

    val configLayer = ZLayer.succeed(config)

    val nettyConfig      = NettyConfig
      .default
      .leakDetection(LeakDetectionLevel.ADVANCED)
      .maxThreads(4)
    val nettyConfigLayer = ZLayer.succeed(nettyConfig)
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
      _      <- httpApp.serve
    yield ()).provide(
      configLayer,
      nettyConfigLayer,
      Server.customized,
      IAServiceGel.layer,
      TripServiceGel.layer,
      PersonServiceGel.layer,
      GelDriver.layer,
      AuthServiceLive.layer
    )
