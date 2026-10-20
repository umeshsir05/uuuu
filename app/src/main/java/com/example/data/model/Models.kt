package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dairyName: String = "मेरी डेयरी (My Dairy)",
    val contactPhone: String = "",
    val defaultRate: Double = 60.0,
    val cowRate: Double = 60.0,
    val buffaloRate: Double = 75.0,
    val upiId: String = "",
    val language: String = "hi", // "hi" for Hindi, "en" for English
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class Shift(val hindiName: String, val englishName: String) {
    MORNING("सुबह", "Morning"),
    EVENING("शाम", "Evening")
}

enum class MilkType(val hindiName: String, val englishName: String) {
    COW("गाय (Cow)", "Cow"),
    BUFFALO("भैंस (Buffalo)", "Buffalo"),
    STANDARD("मिश्रित/सामान्य (Standard)", "Standard")
}

enum class PaymentMethod(val hindiName: String, val englishName: String) {
    UPI("UPI / ऑनलाइन", "UPI / Online"),
    CASH("नकद (Cash)", "Cash"),
    BANK("बैंक ट्रांसफर", "Bank Transfer")
}

@IgnoreExtraProperties
data class MilkEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val date: String = "", // "YYYY-MM-DD"
    val shift: String = "MORNING", // "MORNING" or "EVENING"
    val milkType: String = "COW", // "COW", "BUFFALO", "STANDARD"
    val quantity: Double = 1.0, // in Litres
    val rate: Double = 60.0, // ₹ per Litre
    val totalAmount: Double = 60.0, // quantity * rate
    val isSkipped: Boolean = false, // true = no milk taken that shift
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Payment(
    @DocumentId val id: String = "",
    val userId: String = "",
    val date: String = "", // "YYYY-MM-DD"
    val month: String = "", // "YYYY-MM"
    val amount: Double = 0.0,
    val paymentMethod: String = "UPI", // "UPI", "CASH", "BANK"
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class RateHistory(
    @DocumentId val id: String = "",
    val userId: String = "",
    val effectiveDate: String = "", // "YYYY-MM-DD"
    val milkType: String = "COW",
    val rate: Double = 60.0,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class MonthlyBillSummary(
    val yearMonth: String = "", // "YYYY-MM"
    val totalDaysInMonth: Int = 30,
    val daysMilkDelivered: Int = 0,
    val daysSkipped: Int = 0,
    val morningLitres: Double = 0.0,
    val eveningLitres: Double = 0.0,
    val totalLitres: Double = 0.0,
    val cowLitres: Double = 0.0,
    val buffaloLitres: Double = 0.0,
    val totalMilkAmount: Double = 0.0,
    val totalPaidAmount: Double = 0.0,
    val pendingBalance: Double = 0.0
)
