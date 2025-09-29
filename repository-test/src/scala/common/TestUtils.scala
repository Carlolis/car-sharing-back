package common

import domain.models.invoice.InvoiceId
import domain.services.invoice.repository.InvoiceRepository
import domain.services.maintenance.repository.MaintenanceRepository
import domain.services.person.PersonService
import domain.services.trip.TripService
import zio.*

object TestUtils {
  def setupPersons: ZIO[PersonService, Throwable, Unit] =
    ZIO.foreachPar(TestData.allPersons)(p => PersonService.createPerson(p)).unit

  def setupTestDataPersons: ZIO[PersonService, Throwable, Unit] = setupPersons
  def setupTestData: ZIO[PersonService, Throwable, Unit]        = setupPersons

  def cleanupMaintenances: ZIO[MaintenanceRepository, Nothing, Unit] =
    (for {
      maintenances <- MaintenanceRepository.getAllMaintenances
      _            <- ZIO.foreachDiscard(maintenances)(m => MaintenanceRepository.deleteMaintenance(m.id))
    } yield ()).orDie

  def cleanupInvoices: ZIO[InvoiceRepository, Nothing, Unit] =
    (for {
      invoices <- InvoiceRepository.getAllInvoices
      _        <- ZIO.foreachDiscard(invoices)(i => InvoiceRepository.deleteInvoice(i.id))
    } yield ()).orDie

  def cleanupPersons: ZIO[PersonService, Nothing, Unit] =
    (for {
      persons <- PersonService.getAll
      _       <- ZIO.foreachDiscard(persons)(p => PersonService.deletePerson(p.id))
    } yield ()).orDie

  def cleanupTrips: ZIO[TripService, Nothing, Unit] =
    (for {
      trips <- TripService.getAllTrips
      _     <- ZIO.foreachDiscard(trips)(t => TripService.deleteTrip(t.id))
    } yield ()).orDie

  def cleanupAll: ZIO[PersonService & InvoiceRepository & MaintenanceRepository & TripService, Nothing, Unit] =
    for {
      _ <- cleanupMaintenances
      _ <- cleanupInvoices
      _ <- cleanupTrips
      _ <- cleanupPersons
    } yield ()

  def cleanupData: ZIO[PersonService & InvoiceRepository, Throwable, Unit] =
    for {
      allInvoices <- InvoiceRepository.getAllInvoices
      _           <- ZIO.foreachDiscard(allInvoices)(invoice => InvoiceRepository.deleteInvoice(invoice.id))
      allPersons  <- PersonService.getAll
      _           <- ZIO.foreachDiscard(allPersons)(person => PersonService.deletePerson(person.id))
    } yield ()

  def createTestInvoice: ZIO[InvoiceRepository, Throwable, InvoiceId] =
    for {
      invoiceId <- InvoiceRepository.createInvoice(TestData.invoiceCreateForMaintenance)
    } yield InvoiceId(invoiceId)
}
