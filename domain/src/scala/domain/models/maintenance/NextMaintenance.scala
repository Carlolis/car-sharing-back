package domain.models.maintenance

import domain.models.invoice.Invoice
import zio.json.*

import java.time.LocalDate

case class NextMaintenance(
  `type`: String,                   // Type d'entretien (ex: "Vidange", "Contrôle des freins", "Contrôle Technique")
  dueMileage: Option[Int],          // Kilométrage auquel l'entretien est dû (optionnel)
  dueDate: Option[LocalDate],       // Date à laquelle l'entretien est dû (optionnel)
  description: Option[String],      // Description de l'entretien
)

object NextMaintenance {
  implicit val encoder: JsonEncoder[NextMaintenance] = DeriveJsonEncoder.gen[NextMaintenance]
  implicit val decoder: JsonDecoder[NextMaintenance] = DeriveJsonDecoder.gen[NextMaintenance]
}
