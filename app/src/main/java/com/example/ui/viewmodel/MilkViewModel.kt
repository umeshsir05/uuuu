package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.MilkEntry
import com.example.data.model.MilkType
import com.example.data.model.MonthlyBillSummary
import com.example.data.model.Payment
import com.example.data.model.RateHistory
import com.example.data.model.UserProfile
import com.example.data.repository.MilkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MilkViewModel(
    private val repository: MilkRepository,
    val currentUserId: String
) : ViewModel() {

    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _selectedMonth = MutableStateFlow(monthFormat.format(Date()))
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _userProfile = repository.observeUserProfile(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _allEntries = repository.observeEntries(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allEntries: StateFlow<List<MilkEntry>> = _allEntries

    private val _allPayments = repository.observePayments(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allPayments: StateFlow<List<Payment>> = _allPayments

    private val _rateHistory = repository.observeRateHistory(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val rateHistory: StateFlow<List<RateHistory>> = _rateHistory

    val monthlySummary: StateFlow<MonthlyBillSummary> = combine(
        _selectedMonth,
        _allEntries,
        _allPayments
    ) { month, entries, payments ->
        calculateMonthlySummary(month, entries, payments)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthlyBillSummary())

    fun setSelectedMonth(yearMonth: String) {
        _selectedMonth.value = yearMonth
    }

    fun nextMonth() {
        changeMonth(1)
    }

    fun previousMonth() {
        changeMonth(-1)
    }

    private fun changeMonth(delta: Int) {
        try {
            val date = monthFormat.parse(_selectedMonth.value) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = date
                add(Calendar.MONTH, delta)
            }
            _selectedMonth.value = monthFormat.format(cal.time)
        } catch (_: Exception) {}
    }

    private fun calculateMonthlySummary(
        month: String,
        entries: List<MilkEntry>,
        payments: List<Payment>
    ): MonthlyBillSummary {
        val monthEntries = entries.filter { it.date.startsWith(month) }
        val monthPayments = payments.filter { it.month == month }

        var totalDays = 30
        try {
            val date = monthFormat.parse(month)
            if (date != null) {
                val cal = Calendar.getInstance().apply { time = date }
                totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            }
        } catch (_: Exception) {}

        var morningLitres = 0.0
        var eveningLitres = 0.0
        var cowLitres = 0.0
        var buffaloLitres = 0.0
        var totalAmount = 0.0
        var skippedCount = 0
        var deliveredCount = 0

        // Distinct dates with milk
        val distinctDeliveryDates = mutableSetOf<String>()
        val distinctSkippedDates = mutableSetOf<String>()

        monthEntries.forEach { entry ->
            if (entry.isSkipped) {
                distinctSkippedDates.add(entry.date)
            } else {
                distinctDeliveryDates.add(entry.date)
                totalAmount += (entry.quantity * entry.rate)
                if (entry.shift == "MORNING") {
                    morningLitres += entry.quantity
                } else {
                    eveningLitres += entry.quantity
                }
                when (entry.milkType) {
                    "COW" -> cowLitres += entry.quantity
                    "BUFFALO" -> buffaloLitres += entry.quantity
                    else -> {}
                }
            }
        }

        deliveredCount = distinctDeliveryDates.size
        skippedCount = distinctSkippedDates.size

        val totalLitres = morningLitres + eveningLitres
        val totalPaid = monthPayments.sumOf { it.amount }
        val pendingBalance = totalAmount - totalPaid

        return MonthlyBillSummary(
            yearMonth = month,
            totalDaysInMonth = totalDays,
            daysMilkDelivered = deliveredCount,
            daysSkipped = skippedCount,
            morningLitres = morningLitres,
            eveningLitres = eveningLitres,
            totalLitres = totalLitres,
            cowLitres = cowLitres,
            buffaloLitres = buffaloLitres,
            totalMilkAmount = totalAmount,
            totalPaidAmount = totalPaid,
            pendingBalance = pendingBalance
        )
    }

    fun addOrUpdateEntry(
        date: String,
        shift: String,
        milkType: String,
        quantity: Double,
        rate: Double,
        isSkipped: Boolean,
        notes: String,
        existingId: String = ""
    ) {
        viewModelScope.launch {
            val entry = MilkEntry(
                id = existingId,
                userId = currentUserId,
                date = date,
                shift = shift,
                milkType = milkType,
                quantity = quantity,
                rate = rate,
                totalAmount = if (isSkipped) 0.0 else (quantity * rate),
                isSkipped = isSkipped,
                notes = notes
            )
            repository.addOrUpdateEntry(currentUserId, entry)
        }
    }

    fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            repository.deleteEntry(currentUserId, entryId)
        }
    }

    fun recordPayment(
        date: String,
        month: String,
        amount: Double,
        method: String,
        notes: String
    ) {
        viewModelScope.launch {
            val payment = Payment(
                userId = currentUserId,
                date = date,
                month = month,
                amount = amount,
                paymentMethod = method,
                notes = notes
            )
            repository.addPayment(currentUserId, payment)
        }
    }

    fun deletePayment(paymentId: String) {
        viewModelScope.launch {
            repository.deletePayment(currentUserId, paymentId)
        }
    }

    fun updateMilkRate(
        milkType: String,
        newRate: Double,
        effectiveDate: String,
        notes: String
    ) {
        viewModelScope.launch {
            val history = RateHistory(
                userId = currentUserId,
                effectiveDate = effectiveDate,
                milkType = milkType,
                rate = newRate,
                notes = notes
            )
            repository.addRateChange(currentUserId, history)
        }
    }

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(currentUserId, profile)
        }
    }
}
