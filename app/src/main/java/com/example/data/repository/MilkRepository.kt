package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.MilkEntry
import com.example.data.model.Payment
import com.example.data.model.RateHistory
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class MilkRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    // User Profile
    fun observeUserProfile(userId: String): Flow<UserProfile?> =
        db.collection("users").document(userId)
            .snapshots()
            .map { it.toObject(UserProfile::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, "users/$userId")
                throw error
            }

    suspend fun saveUserProfile(userId: String, profile: UserProfile) {
        val docRef = db.collection("users").document(userId)
        val payload = hashMapOf<String, Any>(
            "userId" to userId,
            "dairyName" to profile.dairyName,
            "contactPhone" to profile.contactPhone,
            "defaultRate" to profile.defaultRate,
            "cowRate" to profile.cowRate,
            "buffaloRate" to profile.buffaloRate,
            "upiId" to profile.upiId,
            "language" to profile.language,
            "createdAt" to (profile.createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // Milk Entries
    fun observeEntries(userId: String): Flow<List<MilkEntry>> =
        db.collection("users").document(userId)
            .collection("entries")
            .orderBy("date", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(MilkEntry::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, "users/$userId/entries")
                throw error
            }

    suspend fun addOrUpdateEntry(userId: String, entry: MilkEntry) {
        val entriesRef = db.collection("users").document(userId).collection("entries")
        val docRef = if (entry.id.isNotBlank()) entriesRef.document(entry.id) else entriesRef.document()

        val isUpdate = entry.id.isNotBlank() && entry.createdAt != null
        val payload = hashMapOf<String, Any>(
            "userId" to userId,
            "date" to entry.date,
            "shift" to entry.shift,
            "milkType" to entry.milkType,
            "quantity" to entry.quantity,
            "rate" to entry.rate,
            "totalAmount" to (if (entry.isSkipped) 0.0 else entry.quantity * entry.rate),
            "isSkipped" to entry.isSkipped,
            "notes" to entry.notes,
            "createdAt" to (if (isUpdate) entry.createdAt!! else FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isUpdate) OperationType.UPDATE else OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteEntry(userId: String, entryId: String) {
        val docRef = db.collection("users").document(userId).collection("entries").document(entryId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // Payments
    fun observePayments(userId: String): Flow<List<Payment>> =
        db.collection("users").document(userId)
            .collection("payments")
            .orderBy("date", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Payment::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, "users/$userId/payments")
                throw error
            }

    suspend fun addPayment(userId: String, payment: Payment) {
        val paymentsRef = db.collection("users").document(userId).collection("payments")
        val docRef = if (payment.id.isNotBlank()) paymentsRef.document(payment.id) else paymentsRef.document()

        val isUpdate = payment.id.isNotBlank() && payment.createdAt != null
        val payload = hashMapOf<String, Any>(
            "userId" to userId,
            "date" to payment.date,
            "month" to payment.month,
            "amount" to payment.amount,
            "paymentMethod" to payment.paymentMethod,
            "notes" to payment.notes,
            "createdAt" to (if (isUpdate) payment.createdAt!! else FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isUpdate) OperationType.UPDATE else OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deletePayment(userId: String, paymentId: String) {
        val docRef = db.collection("users").document(userId).collection("payments").document(paymentId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // Rate History
    fun observeRateHistory(userId: String): Flow<List<RateHistory>> =
        db.collection("users").document(userId)
            .collection("rate_history")
            .orderBy("effectiveDate", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(RateHistory::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, "users/$userId/rate_history")
                throw error
            }

    suspend fun addRateChange(userId: String, rateHistory: RateHistory) {
        val docRef = db.collection("users").document(userId).collection("rate_history").document()
        val payload = hashMapOf<String, Any>(
            "userId" to userId,
            "effectiveDate" to rateHistory.effectiveDate,
            "milkType" to rateHistory.milkType,
            "rate" to rateHistory.rate,
            "notes" to rateHistory.notes,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            // Also update the active rate in user profile
            val userDoc = db.collection("users").document(userId)
            val updates = when (rateHistory.milkType) {
                "COW" -> mapOf("cowRate" to rateHistory.rate, "updatedAt" to FieldValue.serverTimestamp())
                "BUFFALO" -> mapOf("buffaloRate" to rateHistory.rate, "updatedAt" to FieldValue.serverTimestamp())
                else -> mapOf("defaultRate" to rateHistory.rate, "updatedAt" to FieldValue.serverTimestamp())
            }
            userDoc.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }
}
