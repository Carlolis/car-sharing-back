package domain.models.maintenance

import domain.models.invoice.InvoiceId
import zio.json.*

import java.time.LocalDate

case class MaintenanceCreate(
  `type`: String,                   // Type d'entretien (ex: "Vidange", "Contrôle des freins", "Contrôle Technique")
  isCompleted: Boolean,             // Indique si l'entretien a été effectué
  dueMileage: Option[Int],          // Kilométrage auquel l'entretien est dû (optionnel)
  dueDate: Option[LocalDate],       // Date à laquelle l'entretien est dû (optionnel)
  completedDate: Option[LocalDate], // Date de réalisation de l'entretien (optionnel)
  completedMileage: Option[Int],    // Kilométrage au moment de la réalisation (optionnel)
  description: Option[String],      // Description de l'entretien
  invoiceId: Option[InvoiceId]      // ID de la facture associée (optionnel)
)

object MaintenanceCreate {
  implicit val encoder: JsonEncoder[MaintenanceCreate] = DeriveJsonEncoder.gen[MaintenanceCreate]
  implicit val decoder: JsonDecoder[MaintenanceCreate] = DeriveJsonDecoder.gen[MaintenanceCreate]
}
