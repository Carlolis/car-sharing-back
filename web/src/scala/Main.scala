import adapters.{GelDriver, SardineScalaImpl}
import api.InvoiceEndpointsLive.invoiceEndpoints
import api.TripEndpointsLive.tripEndpoints
import api.ia.IaRoutes.iaEndpoints
import api.{TripEndpointsLive, swagger}
import config.AppConfig
import domain.services.invoice.InvoiceServiceLive
import gel.ia.IAServiceGel
import gel.invoice.InvoiceRepositoryGel
import gel.person.PersonRepositoryGel
import gel.trip.TripRepositoryGel
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import webdav.invoice.InvoiceWebDavImpl
import zio.*
import zio.http.*
import zio.http.netty.NettyConfig
import zio.http.netty.NettyConfig.LeakDetectionLevel
import zio.logging.LogFormat.{label, quoted, space, timestamp}
import zio.logging.backend.SLF4J
import zio.logging.{LogColor, LogFilter, LogFormat}

import java.time.format.DateTimeFormatter
import java.util.Locale

object Main extends ZIOAppDefault:
  val logFormat: LogFormat =
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
                ) ++ ZioHttpInterpreter(options).toHttp(invoiceEndpoints)
      _      <- httpApp.serve
    yield ()).provide(
      configLayer,
      nettyConfigLayer,
      Server.customized,
      IAServiceGel.layer,
      TripRepositoryGel.layer,
      PersonRepositoryGel.layer,
      GelDriver.layer,
      AuthServiceLive.layer,
      InvoiceServiceLive.layer,
      InvoiceRepositoryGel.layer,
      InvoiceWebDavImpl.layer,
      SardineScalaImpl.layer,
      AppConfig.layer
    )
