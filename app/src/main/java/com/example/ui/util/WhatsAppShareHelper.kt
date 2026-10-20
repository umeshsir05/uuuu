package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.MonthlyBillSummary
import com.example.data.model.UserProfile
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale

object WhatsAppShareHelper {

    private fun getMonthNameInHindi(yearMonth: String): String {
        return try {
            val parts = yearMonth.split("-")
            val month = parts[1].toInt()
            val year = parts[0]
            val monthNamesHindi = arrayOf(
                "", "जनवरी", "फरवरी", "मार्च", "अप्रैल", "मई", "जून",
                "जुलाई", "अगस्त", "सितंबर", "अक्टूबर", "नवंबर", "दिसंबर"
            )
            "${monthNamesHindi[month]} $year"
        } catch (_: Exception) {
            yearMonth
        }
    }

    fun generateBillText(
        summary: MonthlyBillSummary,
        profile: UserProfile?,
        isHindi: Boolean = true
    ): String {
        val monthDisplay = if (isHindi) getMonthNameInHindi(summary.yearMonth) else summary.yearMonth
        val dairyName = profile?.dairyName?.ifBlank { "दूध हिसाब (Dairy Ledger)" } ?: "दूध हिसाब"
        val upiId = profile?.upiId.orEmpty()

        val sb = StringBuilder()
        sb.append("🥛 *").append(dairyName).append("*\n")
        sb.append("📅 *").append(if (isHindi) "मासिक दूध बिल" else "Monthly Milk Bill").append(":* ").append(monthDisplay).append("\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("🔹 *").append(if (isHindi) "दूध लिए गए दिन" else "Delivery Days").append(":* ").append(summary.daysMilkDelivered).append(" दिन\n")
        if (summary.daysSkipped > 0) {
            sb.append("🔹 *").append(if (isHindi) "दूध नहीं लिया (नागा)" else "Skipped Days").append(":* ").append(summary.daysSkipped).append(" दिन\n")
        }
        sb.append("🔹 *").append(if (isHindi) "सुबह का दूध" else "Morning Milk").append(":* ").append(String.format(Locale.US, "%.1f", summary.morningLitres)).append(" L\n")
        sb.append("🔹 *").append(if (isHindi) "शाम का दूध" else "Evening Milk").append(":* ").append(String.format(Locale.US, "%.1f", summary.eveningLitres)).append(" L\n")
        sb.append("🥛 *").append(if (isHindi) "कुल दूध मात्रा" else "Total Milk").append(":* *").append(String.format(Locale.US, "%.1f", summary.totalLitres)).append(" लीटर (Litres)*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("💰 *").append(if (isHindi) "कुल बिल राशि" else "Total Milk Bill").append(":* *₹").append(String.format(Locale.US, "%.2f", summary.totalMilkAmount)).append("*\n")
        if (summary.totalPaidAmount > 0) {
            sb.append("✅ *").append(if (isHindi) "जमा किया गया" else "Paid Amount").append(":* ₹").append(String.format(Locale.US, "%.2f", summary.totalPaidAmount)).append("\n")
        }
        sb.append("❗ *").append(if (isHindi) "बकाया राशि (Pending Due)" else "Net Balance Due").append(":* *₹").append(String.format(Locale.US, "%.2f", summary.pendingBalance)).append("*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")

        if (upiId.isNotBlank()) {
            sb.append("📲 *UPI द्वारा भुगतान करें:* `").append(upiId).append("`\n")
        }

        if (isHindi) {
            sb.append("🙏 _कृपया हिसाब चेक कर भुगतान करें। धन्यवाद!_")
        } else {
            sb.append("🙏 _Kindly verify and clear the dues. Thank you!_")
        }

        return sb.toString()
    }

    fun generatePaymentReminderText(
        summary: MonthlyBillSummary,
        profile: UserProfile?,
        tone: ReminderTone = ReminderTone.POLITE,
        isHindi: Boolean = true
    ): String {
        val monthDisplay = if (isHindi) getMonthNameInHindi(summary.yearMonth) else summary.yearMonth
        val dairyName = profile?.dairyName?.ifBlank { "दूध हिसाब" } ?: "दूध हिसाब"
        val upiId = profile?.upiId.orEmpty()
        val pending = String.format(Locale.US, "%.2f", summary.pendingBalance)

        val sb = StringBuilder()
        when (tone) {
            ReminderTone.POLITE -> {
                sb.append("🙏 *नमस्ते!* (Payment Reminder)\n\n")
                sb.append("यह *").append(dairyName).append("* की तरफ से माह *").append(monthDisplay).append("* के दूध बिल का विनम्र स्मरण पत्र है।\n\n")
                sb.append("🥛 कुल दूध: *").append(String.format(Locale.US, "%.1f", summary.totalLitres)).append(" L*\n")
                sb.append("💰 कुल बकाया राशि: *₹").append(pending).append("*\n\n")
                if (upiId.isNotBlank()) {
                    sb.append("📲 UPI ID: `").append(upiId).append("`\n\n")
                }
                sb.append("सुविधा अनुसार कृपया जल्द भुगतान करने की कृपा करें। धन्यवाद! 🙏")
            }
            ReminderTone.DUE_DATE -> {
                sb.append("⏰ *माह का बिल भुगतान रिमाइंडर* ⏰\n\n")
                sb.append("*").append(dairyName).append("* - माह: *").append(monthDisplay).append("*\n")
                sb.append("कुल बकाया राशि: *₹").append(pending).append("*\n")
                sb.append("महीना समाप्त होने पर कृपया अपना बकाया दूध का हिसाब चुकता करें।\n\n")
                if (upiId.isNotBlank()) {
                    sb.append("📲 Google Pay / PhonePe / Paytm UPI: `").append(upiId).append("`\n\n")
                }
                sb.append("धन्यवाद! 🙏")
            }
            ReminderTone.URGENT -> {
                sb.append("⚠️ *जरूरी सूचना - बकाया दूध बिल* ⚠️\n\n")
                sb.append("*").append(dairyName).append("* के माह *").append(monthDisplay).append("* का दूध बिल *₹").append(pending).append("* अभी तक बकाया है।\n\n")
                sb.append("कृपया आज ही भुगतान करने का कष्ट करें ताकि आगे दूध वितरण में कोई रुकावट न आए।\n")
                if (upiId.isNotBlank()) {
                    sb.append("📲 UPI ID: `").append(upiId).append("`\n\n")
                }
                sb.append("सादर धन्यवाद! 🙏")
            }
        }
        return sb.toString()
    }

    fun shareToWhatsApp(
        context: Context,
        message: String,
        phoneNumber: String = ""
    ) {
        val cleanPhone = phoneNumber.filter { it.isDigit() }
        try {
            if (cleanPhone.length >= 10) {
                // If 10 digits without country code, add 91 for India
                val phoneWithCountry = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
                val encodedText = URLEncoder.encode(message, "UTF-8")
                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phoneWithCountry&text=$encodedText")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.whatsapp")
                }
                context.startActivity(intent)
            } else {
                // General WhatsApp Share
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    setPackage("com.whatsapp")
                }
                context.startActivity(intent)
            }
        } catch (_: Exception) {
            // Fallback to general Android share chooser if WhatsApp direct fails
            try {
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                }
                context.startActivity(Intent.createChooser(fallbackIntent, "व्हाट्सएप या अन्य ऐप पर भेजें"))
            } catch (e: Exception) {
                Toast.makeText(context, "मैसेज शेयर करने में त्रुटि: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

enum class ReminderTone(val titleHindi: String, val titleEnglish: String) {
    POLITE("विनम्र निवेदन (Polite)", "Polite Reminder"),
    DUE_DATE("अंतिम तिथि (Due Date)", "Due Date Reminder"),
    URGENT("अति आवश्यक (Urgent)", "Urgent Reminder")
}
