import sttp.tapir.server.interceptor.cors.CORSConfig.AllowedOrigin
import sttp.tapir.server.interceptor.cors.{CORSConfig, CORSInterceptor}
import sttp.tapir.server.ziohttp.ZioHttpServerOptions
import sttp.tapir.ztapir.RIOMonadError

given RIOMonadError[Any]               = new RIOMonadError[Any]
val options: ZioHttpServerOptions[Any] =
  ZioHttpServerOptions
    .customiseInterceptors
    .exceptionHandler(new DefectHandler())
    .corsInterceptor(
      CORSInterceptor.customOrThrow(
        CORSConfig
          .default.copy(
            allowedOrigin = AllowedOrigin.All
          )
      )
    )
    .decodeFailureHandler(CustomDecodeFailureHandler.create())
    .options
