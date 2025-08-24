import domain.models.invoice.{DriverName, InvoiceCreate}
import zio.test.*
import zio.test.Assertion.*
import zio.{Scope, ZIO}

import java.time.LocalDate

object SimpleGuidelineTest extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment & Scope, Any] =
    suite("Simple Guideline Test - Domain Models")(
      test("DriverName should create correctly") {
        val driverName = DriverName("John Doe")
        assertTrue(driverName.toString == "John Doe")
      },
      test("InvoiceCreate should create with valid data") {
        val invoiceCreate = InvoiceCreate(
          mileage = 100,
          date = LocalDate.of(2024, 8, 24),
          name = "Test Invoice",
          drivers = Set(DriverName("Alice"), DriverName("Bob")),
          kind = "fuel"
        )

        assertTrue(
          invoiceCreate.mileage == 100,
          invoiceCreate.name == "Test Invoice",
          invoiceCreate.drivers.size == 2,
          invoiceCreate.kind == "fuel"
        )
      },
      test("ZIO logging should work") {
        for {
          _ <- ZIO.logInfo("[DEBUG_LOG] Simple test logging works!")
        } yield assertCompletes
      }
    )
}
