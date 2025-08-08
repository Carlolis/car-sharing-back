package api

import api.InvoiceEndpoints.invoiceEndPoints
import api.TripEndpoints.tripEndPoints
import api.ia.IaEndpoints.iaEndpoints
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import zio.Task

val swagger = SwaggerInterpreter()
  .fromEndpoints[Task](tripEndPoints ++ iaEndpoints ++ invoiceEndPoints, "car-sharing", "0.1.0")
