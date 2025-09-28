import adapters.{GelDriver, SardineScalaImpl}
import api.CarEndpointsLive.carEndpointsLive
import api.InvoiceEndpointsLive.invoiceEndpointsLive
import api.MaintenanceEndpointsLive
import api.TripEndpointsLive.tripEndpointsLive
import api.ia.IaRoutes.iaEndpoints
import api.{TripEndpointsLive, swagger}
import config.AppConfig
import domain.services.invoice.InvoiceServiceLive
import domain.services.maintenance.MaintenanceServiceLive
import gel.car.CarRepositoryGel
import gel.ia.IAServiceGel
import gel.invoice.InvoiceRepositoryGel
import gel.maintenance.MaintenanceRepositoryGel
import gel.person.PersonRepositoryGel
import gel.trip.TripRepositoryGel
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import webdav.invoice.InvoiceWebDavImpl
import zio.*
import zio.http.*
import zio.http.Server.RequestStreaming
import zio.http.netty.NettyConfig
import zio.http.netty.NettyConfig.LeakDetectionLevel
import zio.logging.LogFormat.{label, quoted, space, timestamp}
import zio.logging.backend.SLF4J
import zio.logging.{LogColor, LogFilter, LogFormat}

import java.time.format.DateTimeFormatter
import java.util.Locale

object Main extends ZIOAppDefault:
  private val logFormat: LogFormat =
    label(
      "timestamp",
      timestamp(DateTimeFormatter.ofPattern("dd/LL/uu HH:mm:ss:SS", Locale.FRANCE))
    )
      .color(LogColor.BLUE) |-|
      label("level", LogFormat.level).highlight |-|
      label("message", quoted(LogFormat.line)).highlight +
      (space + label("cause", LogFormat.cause).highlight).filter(LogFilter.causeNonEmpty) |-| label(
        "origin",
        LogFormat.text("(") + LogFormat.enclosingClass + LogFormat.text(
          ":"
        ) + LogFormat.traceLine + LogFormat.text(")")
      ).color(LogColor.GREEN)

  override val bootstrap: ZLayer[Any, Nothing, Unit] =
    Runtime.removeDefaultLoggers ++ SLF4J.slf4j(logFormat)

  override def run: ZIO[Any, Throwable, Unit] =

    val port = sys.env("PORT").toInt

    val config = Server
      .Config.default.requestStreaming(RequestStreaming.Enabled)
      .port(port)
    /*      .maxRequestSize(50 * 1024 * 1024)*/

    val configLayer = ZLayer.succeed(config)

    val nettyConfig      = NettyConfig
      .default
      .leakDetection(LeakDetectionLevel.ADVANCED)
      .maxThreads(4)
    val nettyConfigLayer = ZLayer.succeed(nettyConfig)
    (for

      _              <- ZIO.log(s"Swagger UI available at http://localhost:$port/docs")
      _              <- ZIO.log(s"Server starting on http://localhost:$port")
      tapirApp        = ZioHttpInterpreter(options).toHttp(
                          tripEndpointsLive
                        ) ++ ZioHttpInterpreter(options).toHttp(
                          swagger
                        ) ++ ZioHttpInterpreter(options).toHttp(
                          iaEndpoints
                        ) ++ ZioHttpInterpreter(options).toHttp(invoiceEndpointsLive) ++ ZioHttpInterpreter(options).toHttp(
                          MaintenanceEndpointsLive.endpoints

                        ) ++ ZioHttpInterpreter(options).toHttp(carEndpointsLive) ++ ZioHttpInterpreter(options).toHttp(
        MaintenanceEndpointsLive.endpoints
      )
      // Fallback handler for unmatched routes to log unsupported endpoints
      fallbackHandler = Handler.fromFunctionZIO[Request] { request =>
        ZIO.log(
          s"Unsupported endpoint requested: ${request.method} ${request.path} from ${request.remoteAddress.getOrElse("unknown")}").as(Response.json("""{"error":"Not found."}""").status(Status.NotFound))
                        }
      httpApp         = tapirApp ++ Routes(RoutePattern.any -> fallbackHandler)
      _              <- httpApp.serve
    yield ()).provide(
      configLayer,
      nettyConfigLayer,
      Server.customized,
      IAServiceGel.layer,
      TripRepositoryGel.layer,
      PersonRepositoryGel.layer,
      CarRepositoryGel.layer,
      GelDriver.layer,
      AuthServiceLive.layer,
      InvoiceServiceLive.layer,
      InvoiceRepositoryGel.layer,
      MaintenanceServiceLive.layer,
      MaintenanceRepositoryGel.layer,
      InvoiceWebDavImpl.layer,
      SardineScalaImpl.layer,
      AppConfig.layer
    )
