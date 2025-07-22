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
    .logWhenReceived(true).logAllDecodeFailures(true).logWhenHandled(true).logLogicExceptions(true)

val options: ZioHttpServerOptions[Any] =
  ZioHttpServerOptions
    .customiseInterceptors.corsInterceptor(
      CORSInterceptor.customOrThrow(
        CORSConfig
          .default.copy(
            allowedOrigin = AllowedOrigin.All
          )
      )
    ).serverLog(serverLog).decodeFailureHandler(CustomDecodeFailureHandler.create()).options
