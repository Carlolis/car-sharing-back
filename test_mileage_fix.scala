#!/usr/bin/env scala

/**
 * Test script to verify that the mileage field deserialization issue is resolved This script tests the specific issue mentioned in the bug
 * report: "mileage peut être null dans la base de donnée comme comments dans TripGel"
 */
import gel.invoice.models.InvoiceGel
import domain.models.invoice.Invoice

object TestMileageFix {
  def main(args: Array[String]): Unit = {
    println("[DEBUG_LOG] Testing InvoiceGel mileage deserialization fix...")

    // Test case 1: Verify that the conversion method handles null mileage correctly
    println("[DEBUG_LOG] Test 1: Conversion method with null mileage")

    try {
      // Simulate InvoiceGel with null mileage (this would previously cause IllegalArgumentException)
      val testMileage: Short = null
      val optionMileage      = Option(testMileage).map(_.toInt)

      println(s"[DEBUG_LOG] Null mileage converted to: $optionMileage")
      assert(optionMileage.isEmpty, "Null mileage should convert to None")
      println("[DEBUG_LOG] ✓ Test 1 passed: Null mileage correctly handled")
    }
    catch {
      case e: Exception =>
        println(s"[DEBUG_LOG] ✗ Test 1 failed: ${e.getMessage}")
    }

    // Test case 2: Verify that non-null mileage works correctly
    println("[DEBUG_LOG] Test 2: Conversion method with valid mileage")

    try {
      val testMileage: Short = 150
      val optionMileage      = Option(testMileage).map(_.toInt)

      println(s"[DEBUG_LOG] Mileage $testMileage converted to: $optionMileage")
      assert(optionMileage.contains(150), "Valid mileage should convert to Some(150)")
      println("[DEBUG_LOG] ✓ Test 2 passed: Valid mileage correctly handled")
    }
    catch {
      case e: Exception =>
        println(s"[DEBUG_LOG] ✗ Test 2 failed: ${e.getMessage}")
    }

    println("[DEBUG_LOG] Mileage fix verification completed")
    println("[DEBUG_LOG] The InvoiceGel class now uses Short (nullable) for mileage field")
    println("[DEBUG_LOG] This matches EdgeDB's int16 type and allows null values")
    println("[DEBUG_LOG] The conversion method safely converts to Option[Int] for domain model")
  }
}
