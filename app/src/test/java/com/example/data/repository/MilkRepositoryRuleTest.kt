package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.MilkEntry
import com.example.data.model.Payment
import com.example.data.model.RateHistory
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MilkRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun testAuthenticatedUserCanSaveAndRetrieveProfileAndEntries() = runBlocking {
    val uid = signInTestUser("dairy_user@example.com")
    val repository = MilkRepository(firestore)

    // Save profile
    val profile = UserProfile(
      userId = uid,
      dairyName = "Shree Krishna Dairy",
      contactPhone = "+919876543210",
      defaultRate = 60.0,
      cowRate = 60.0,
      buffaloRate = 75.0,
      upiId = "krishna@upi",
      language = "hi"
    )
    repository.saveUserProfile(uid, profile)

    val savedProfile = repository.observeUserProfile(uid).first()
    assertNotNull(savedProfile)
    assertEquals("Shree Krishna Dairy", savedProfile?.dairyName)
    assertEquals(60.0, savedProfile?.defaultRate ?: 0.0, 0.01)

    // Add Milk Entry
    val entry = MilkEntry(
      date = "2026-10-20",
      shift = "MORNING",
      milkType = "COW",
      quantity = 2.0,
      rate = 60.0,
      totalAmount = 120.0,
      isSkipped = false,
      notes = "Normal delivery"
    )
    repository.addOrUpdateEntry(uid, entry)

    val entries = repository.observeEntries(uid).first()
    assertTrue(entries.isNotEmpty())
    assertEquals(2.0, entries[0].quantity, 0.01)
    assertEquals(120.0, entries[0].totalAmount, 0.01)

    // Add Payment
    val payment = Payment(
      date = "2026-10-20",
      month = "2026-10",
      amount = 500.0,
      paymentMethod = "UPI",
      notes = "Advance"
    )
    repository.addPayment(uid, payment)

    val payments = repository.observePayments(uid).first()
    assertTrue(payments.isNotEmpty())
    assertEquals(500.0, payments[0].amount, 0.01)

    // Add Rate Change
    val rateChange = RateHistory(
      effectiveDate = "2026-11-01",
      milkType = "COW",
      rate = 65.0,
      notes = "Winter rate update"
    )
    repository.addRateChange(uid, rateChange)

    val rateHistoryList = repository.observeRateHistory(uid).first()
    assertTrue(rateHistoryList.isNotEmpty())
    assertEquals(65.0, rateHistoryList[0].rate, 0.01)
  }
}
