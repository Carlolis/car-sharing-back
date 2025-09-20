package domain.services.maintenance

import domain.models.maintenance.{Maintenance, MaintenanceCreate, MaintenanceId, MaintenanceUpdate}
import domain.services.maintenance.repository.MaintenanceRepository
import zio.*

import java.util.UUID

case class MaintenanceServiceLive(maintenanceRepository: MaintenanceRepository) extends MaintenanceService {
  override def createMaintenance(maintenanceCreate: MaintenanceCreate): Task[UUID] =
    ZIO.logInfo(s"Creating maintenance of type: ${maintenanceCreate.`type`}") *>
      maintenanceRepository
        .createMaintenance(maintenanceCreate)
        .tapBoth(
          error => ZIO.logError(s"Failed to create maintenance: $error"),
          id => ZIO.logInfo(s"Successfully created maintenance with id: $id")
        )

  override def getAllMaintenances: Task[List[Maintenance]] =
    ZIO.logInfo("Fetching all maintenances") *>
      maintenanceRepository
        .getAllMaintenances
        .tapBoth(
          error => ZIO.logError(s"Failed to fetch maintenances: $error"),
          maintenances => ZIO.logInfo(s"Successfully fetched ${maintenances.length} maintenances")
        )

  override def updateMaintenance(maintenance: MaintenanceUpdate): Task[MaintenanceId] =
    ZIO.logInfo(s"Updating maintenance with id: ${maintenance.id}") *>
      maintenanceRepository
        .updateMaintenance(maintenance)
        .tapBoth(
          error => ZIO.logError(s"Failed to update maintenance with id: ${maintenance.id}, error: $error"),
          id => ZIO.logInfo(s"Successfully updated maintenance with id: $id")
        )

  override def deleteMaintenance(id: MaintenanceId): Task[MaintenanceId] =
    ZIO.logInfo(s"Deleting maintenance with id: $id") *>
      maintenanceRepository
        .deleteMaintenance(id)
        .tapBoth(
          error => ZIO.logError(s"Failed to delete maintenance with id: $id, error: $error"),
          deletedId => ZIO.logInfo(s"Successfully deleted maintenance with id: $deletedId")
        )
}

object MaintenanceServiceLive {
  val layer: ZLayer[MaintenanceRepository, Nothing, MaintenanceService] =
    ZLayer.fromFunction(MaintenanceServiceLive(_))
}
