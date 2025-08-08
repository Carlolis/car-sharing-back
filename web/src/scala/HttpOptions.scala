import sttp.tapir.server.interceptor.cors.CORSConfig.AllowedOrigin
import sttp.tapir.server.interceptor.cors.{CORSConfig, CORSInterceptor}
import sttp.tapir.server.interceptor.log.DefaultServerLog
import sttp.tapir.server.ziohttp.ZioHttpServerOptions
import sttp.tapir.ztapir.RIOMonadError
import zio.*

given RIOMonadError[Any] = new RIOMonadError[Any]

type F[A] = ZIO[Any, Throwable, A]

var serverLog: DefaultServerLog[F] =
  ZioHttpServerOptions
    .defaultServerLog
    .logWhenReceived(true).showEndpoint(s => s.show)

val options =
  ZioHttpServerOptions
    .customiseInterceptors.corsInterceptor(
      CORSInterceptor.customOrThrow(
        CORSConfig
          .default.copy(
            allowedOrigin = AllowedOrigin.All
          )
      )
    )
    .decodeFailureHandler(CustomDecodeFailureHandler.create()).serverLog(serverLog).options
