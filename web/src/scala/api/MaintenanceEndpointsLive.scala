package api

import api.models.ErrorResponse
import domain.services.AuthService
import domain.services.maintenance.MaintenanceService
import sttp.model.StatusCode
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*
import zio.*

object MaintenanceEndpointsLive {
  private val createMaintenanceImpl: ZServerEndpoint[MaintenanceService & AuthService, Any] =
    MaintenanceEndpoints.createMaintenance.zServerLogic { case (token, maintenanceCreate) =>
      (for {
        _  <- AuthService.authenticate(token)
        id <- MaintenanceService.createMaintenance(maintenanceCreate)
      } yield id).mapError(error => (StatusCode.BadRequest, ErrorResponse(error.getMessage)))
    }

  private val getAllMaintenancesImpl: ZServerEndpoint[MaintenanceService & AuthService, Any] =
    MaintenanceEndpoints.getAllMaintenances.zServerLogic { token =>
      (for {
        _            <- AuthService.authenticate(token)
        maintenances <- MaintenanceService.getAllMaintenances
      } yield maintenances).mapError(error => (StatusCode.BadRequest, ErrorResponse(error.getMessage)))
    }

  private val updateMaintenanceImpl: ZServerEndpoint[MaintenanceService & AuthService, Any] =
    MaintenanceEndpoints.updateMaintenance.zServerLogic { case (token, maintenance) =>
      (for {
        _  <- AuthService.authenticate(token)
        id <- MaintenanceService.updateMaintenance(maintenance)
      } yield id).mapError(error => (StatusCode.BadRequest, ErrorResponse(error.getMessage)))
    }

  private val deleteMaintenanceImpl: ZServerEndpoint[MaintenanceService & AuthService, Any] =
    MaintenanceEndpoints.deleteMaintenance.zServerLogic { case (maintenanceId, token) =>
      (for {
        _  <- AuthService.authenticate(token)
        id <- MaintenanceService.deleteMaintenance(maintenanceId)
      } yield id).mapError(error => (StatusCode.BadRequest, ErrorResponse(error.getMessage)))
    }

  val endpoints: List[ZServerEndpoint[MaintenanceService & AuthService, Any]] = List(
    createMaintenanceImpl,
    getAllMaintenancesImpl,
    updateMaintenanceImpl,
    deleteMaintenanceImpl
  )
}
