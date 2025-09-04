package domain.models.maintenance

import domain.models.invoice.Invoice
import zio.json.*

import java.time.LocalDate

case class Maintenance(
  id: MaintenanceId,
  `type`: String,                   // Type d'entretien (ex: "Vidange", "Contrôle des freins", "Contrôle Technique")
  isCompleted: Boolean,             // Indique si l'entretien a été effectué
  dueMileage: Option[Int],          // Kilométrage auquel l'entretien est dû (optionnel)
  dueDate: Option[LocalDate],       // Date à laquelle l'entretien est dû (optionnel)
  completedDate: Option[LocalDate], // Date de réalisation de l'entretien (optionnel)
  completedMileage: Option[Int],    // Kilométrage au moment de la réalisation (optionnel)
  description: Option[String],      // Description de l'entretien
  invoice: Option[Invoice]          // Facture associée (optionnel)
)

object Maintenance {
  implicit val encoder: JsonEncoder[Maintenance] = DeriveJsonEncoder.gen[Maintenance]
  implicit val decoder: JsonDecoder[Maintenance] = DeriveJsonDecoder.gen[Maintenance]
}
