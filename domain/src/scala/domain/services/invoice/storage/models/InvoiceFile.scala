package domain.services.invoice.storage.models

case class InvoiceFile(
  name: String,
  path: String,
  size: Long,
  lastModified: Long
)
