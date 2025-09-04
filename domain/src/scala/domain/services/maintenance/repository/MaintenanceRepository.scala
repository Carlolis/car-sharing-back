package domain.services.maintenance.repository

import domain.models.maintenance.{Maintenance, MaintenanceCreate, MaintenanceId}
import domain.services.maintenance.repository.models.errors.SaveMaintenanceFailed
import zio.*

import java.util.UUID

trait MaintenanceRepository {
  def createMaintenance(maintenanceCreate: MaintenanceCreate): ZIO[Any, SaveMaintenanceFailed, UUID]
  def getAllMaintenances: Task[List[Maintenance]]
  def deleteMaintenance(id: MaintenanceId): Task[MaintenanceId]
  def updateMaintenance(maintenance: Maintenance): Task[MaintenanceId]
}

object MaintenanceRepository:
  def createMaintenance(maintenanceCreate: MaintenanceCreate): RIO[MaintenanceRepository, UUID] =
    ZIO.serviceWithZIO[MaintenanceRepository](_.createMaintenance(maintenanceCreate))
  def getAllMaintenances: RIO[MaintenanceRepository, List[Maintenance]]                  =
    ZIO.serviceWithZIO[MaintenanceRepository](_.getAllMaintenances)
  def deleteMaintenance(id: MaintenanceId): RIO[MaintenanceRepository, MaintenanceId]        =
    ZIO.serviceWithZIO[MaintenanceRepository](_.deleteMaintenance(id))
  def updateMaintenance(maintenance: Maintenance): RIO[MaintenanceRepository, MaintenanceId]        =
    ZIO.serviceWithZIO[MaintenanceRepository](_.updateMaintenance(maintenance))