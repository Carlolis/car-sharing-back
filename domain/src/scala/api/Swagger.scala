package api

import api.TripEndpoints.tripEndPoints
import api.ia.IaEndpoints.iaEndpoints
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import zio.Task

val swagger = SwaggerInterpreter()
  .fromEndpoints[Task](tripEndPoints ++ iaEndpoints, "car-sharing", "0.1.0")
