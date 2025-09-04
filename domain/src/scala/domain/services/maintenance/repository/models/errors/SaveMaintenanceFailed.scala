package domain.services.maintenance.repository.models.errors

case class SaveMaintenanceFailed(message: String) extends Exception(message)