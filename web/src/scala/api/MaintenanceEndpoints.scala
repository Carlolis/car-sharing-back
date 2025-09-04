package api

import api.models.ErrorResponse
import domain.models.maintenance.{Maintenance, MaintenanceCreate, MaintenanceId}
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.server.ServerEndpoint
import zio.*

import java.util.UUID

object MaintenanceEndpoints {
  val createMaintenance: Endpoint[Unit, (String, MaintenanceCreate), (StatusCode, ErrorResponse), UUID, Any] = endpoint
    .post
    .in("api" / "maintenance")
    .in(auth.bearer[String]())
    .in(jsonBody[MaintenanceCreate])
    .out(jsonBody[UUID])
    .errorOut(statusCode and jsonBody[ErrorResponse])
    .description("Create a new maintenance record")

  val getAllMaintenances: Endpoint[Unit, String, (StatusCode, ErrorResponse), List[Maintenance], Any] = endpoint
    .get
    .in("api" / "maintenance")
    .in(auth.bearer[String]())
    .out(jsonBody[List[Maintenance]])
    .errorOut(statusCode and jsonBody[ErrorResponse])
    .description("Get all maintenance records")

  val updateMaintenance: Endpoint[Unit, (String, Maintenance), (StatusCode, ErrorResponse), MaintenanceId, Any] = endpoint
    .put
    .in("api" / "maintenance")
    .in(auth.bearer[String]())
    .in(jsonBody[Maintenance])
    .out(jsonBody[MaintenanceId])
    .errorOut(statusCode and jsonBody[ErrorResponse])
    .description("Update an existing maintenance record")

  val deleteMaintenance: Endpoint[Unit, (MaintenanceId, String), (StatusCode, ErrorResponse), MaintenanceId, Any] = endpoint
    .delete
    .in("api" / "maintenance" / path[MaintenanceId]("maintenanceId"))
    .in(auth.bearer[String]())
    .out(jsonBody[MaintenanceId])
    .errorOut(statusCode and jsonBody[ErrorResponse])
    .description("Delete a maintenance record")

  val endpoints: List[AnyEndpoint] = List(
    createMaintenance,
    getAllMaintenances,
    updateMaintenance,
    deleteMaintenance
  )
}
