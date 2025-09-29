package domain.services.maintenance

import domain.models.maintenance.{Maintenance, MaintenanceCreate, MaintenanceId, MaintenanceUpdate, NextMaintenance}
import zio.*

import java.util.UUID

trait MaintenanceService {
  def createMaintenance(maintenanceCreate: MaintenanceCreate): Task[UUID]
  def getAllMaintenances: Task[List[Maintenance]]
  def updateMaintenance(maintenance: MaintenanceUpdate): Task[MaintenanceId]
  def deleteMaintenance(id: MaintenanceId): Task[MaintenanceId]

  def getNextMaintenances: Task[(NextMaintenance,Option[NextMaintenance])]
}

object MaintenanceService :
  def createMaintenance(maintenanceCreate: MaintenanceCreate): ZIO[MaintenanceService, Throwable, UUID] =
    ZIO.serviceWithZIO[MaintenanceService](_.createMaintenance(maintenanceCreate))

  def getAllMaintenances: ZIO[MaintenanceService, Throwable, List[Maintenance]] =
    ZIO.serviceWithZIO[MaintenanceService](_.getAllMaintenances)

  def updateMaintenance(maintenance: MaintenanceUpdate): ZIO[MaintenanceService, Throwable, MaintenanceId] =
    ZIO.serviceWithZIO[MaintenanceService](_.updateMaintenance(maintenance))

  def deleteMaintenance(id: MaintenanceId): ZIO[MaintenanceService, Throwable, MaintenanceId] =
    ZIO.serviceWithZIO[MaintenanceService](_.deleteMaintenance(id))

  def getNextMaintenances: ZIO[MaintenanceService, Throwable, (NextMaintenance,Option[NextMaintenance])] =
    ZIO.serviceWithZIO[MaintenanceService](_.getNextMaintenances)  

