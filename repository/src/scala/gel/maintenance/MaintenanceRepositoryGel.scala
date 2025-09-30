package gel.maintenance

import adapters.GelDriverLive
import domain.models.maintenance.*
import domain.services.maintenance.repository.MaintenanceRepository
import domain.services.maintenance.repository.models.errors.SaveMaintenanceFailed
import gel.maintenance.models.MaintenanceGel
import zio.*

import java.util.UUID

case class MaintenanceRepositoryGel(gelDb: GelDriverLive) extends MaintenanceRepository {
  override def createMaintenance(
    maintenanceCreate: MaintenanceCreate
  ): ZIO[Any, SaveMaintenanceFailed, UUID] =
    gelDb
      .querySingle(
        classOf[UUID],
        s"""
           |  with new_maintenance := (insert MaintenanceGel { 
           |   type := '${maintenanceCreate.`type`}',
           |   isCompleted := ${maintenanceCreate.isCompleted},
           |   ${maintenanceCreate.dueMileage.map(mileage => s"dueMileage := $mileage,").getOrElse("")}
           |   ${maintenanceCreate
            .dueDate.map(date =>
              s"dueDate := cal::to_local_date(${date.getYear}, ${date.getMonthValue}, ${date.getDayOfMonth}),").getOrElse("")}
           |   ${maintenanceCreate
            .completedDate.map(date =>
              s"completedDate := cal::to_local_date(${date.getYear}, ${date.getMonthValue}, ${date.getDayOfMonth}),").getOrElse("")}
           |   ${maintenanceCreate.completedMileage.map(mileage => s"completedMileage := $mileage,").getOrElse("")}
           |   ${maintenanceCreate.description.map(desc => s"description := '${desc.replace("'", "\\'")}',").getOrElse("")}
           |   ${maintenanceCreate
            .invoiceId.map(invoiceId =>
              s"invoice := (select detached default::InvoiceGel filter .id = <uuid>'$invoiceId' limit 1),").getOrElse("")}
           |   }) select new_maintenance.id;
           |"""
      ).tapBoth(
        error => ZIO.logError(s"Failed to create maintenance: $error"),
        uuid => ZIO.logInfo(s"Created maintenance with id: $uuid")
      ).mapError(error => SaveMaintenanceFailed(error.getMessage))

  override def getAllMaintenances: Task[List[Maintenance]] =
    gelDb
      .query(
        classOf[MaintenanceGel],
        s"""
           | select MaintenanceGel { 
           |   id, type, isCompleted, dueMileage, dueDate, completedDate, 
           |   completedMileage, description, 
           |   invoice: { id, amount, date, name, gelPerson: { name }, kind, mileage, fileName, toDriver: { name } }
           | };
           |"""
      )
      .map(maintenances => maintenances.map(MaintenanceGel.toMaintenance))

  override def deleteMaintenance(id: MaintenanceId): Task[MaintenanceId] =
    gelDb
      .querySingle(
        classOf[String],
        s"""
           | delete MaintenanceGel filter .id = <uuid>'${id.toString}';
           | select '${id.toString}';
           |"""
      )
      .map(id => MaintenanceId(UUID.fromString(id)))
      .zipLeft(ZIO.logInfo(s"Deleted maintenance with id: $id"))

  override def updateMaintenance(maintenance: MaintenanceUpdate): Task[MaintenanceId] =
    gelDb
      .querySingle(
        classOf[UUID],
        s"""
           | with updated_maintenance := (
           |    update MaintenanceGel
           |    filter .id = <uuid>'${maintenance.id}'
           |    set {
           |        type := '${maintenance.`type`}',
           |        isCompleted := ${maintenance.isCompleted},
           |        ${maintenance.dueMileage.map(mileage => s"dueMileage := $mileage").getOrElse("dueMileage := <int16>{}")},
           |        ${maintenance
            .dueDate.map(date => s"dueDate := cal::to_local_date(${date.getYear}, ${date.getMonthValue}, ${date.getDayOfMonth})").getOrElse(
              "dueDate := <cal::local_date>{}")},
           |        ${maintenance
            .completedDate.map(date =>
              s"completedDate := cal::to_local_date(${date.getYear}, ${date.getMonthValue}, ${date.getDayOfMonth})").getOrElse(
              "completedDate := <cal::local_date>{}")},
           |        ${maintenance
            .completedMileage.map(mileage => s"completedMileage := $mileage").getOrElse("completedMileage := <int16>{}")},
           |        ${maintenance.description.map(desc => s"description := '$desc'").getOrElse("description := <str>{}")},
           |        ${maintenance
            .invoiceId.map(invoiceId =>
              s"invoice := (select detached default::InvoiceGel filter .id = <uuid>'$invoiceId' limit 1)").getOrElse(
              "invoice := <default::InvoiceGel>{}")}
           |    }
           |)
           |select updated_maintenance.id;
           |"""
      ).map(MaintenanceId(_)).tapBoth(
        error => ZIO.logError(s"Failed to update maintenance with id: ${maintenance.id}, error: $error"),
        uuid => ZIO.logInfo(s"Updated maintenance with id: $uuid")
      )

  override def getNextMaintenances: Task[Option[(NextMaintenance, Option[NextMaintenance])]] = gelDb
    .querySingle(
      classOf[MaintenanceGel],
      s"""
         | select MaintenanceGel {
         |   id, type, isCompleted, dueMileage, dueDate, completedDate,
         |   completedMileage, description,
         |   invoice: { id, amount, date, name, gelPerson: { name }, kind, mileage, fileName, toDriver: { name } }
         |   filter .isCompleted = false
         | };
         |"""
    )
    .map(maintenance => Some((MaintenanceGel.toNextMaintenance(maintenance), None))).catchAll(_ => ZIO.none)
}

object MaintenanceRepositoryGel:
  val layer: ZLayer[GelDriverLive, Nothing, MaintenanceRepository] =
    ZLayer.fromFunction(MaintenanceRepositoryGel(_))
