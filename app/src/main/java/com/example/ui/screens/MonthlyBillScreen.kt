package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DairyAmber
import com.example.ui.theme.DairyBlue
import com.example.ui.theme.DueRed
import com.example.ui.theme.PaidGreen
import com.example.ui.util.ReminderTone
import com.example.ui.util.WhatsAppShareHelper
import com.example.ui.viewmodel.MilkViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyBillScreen(
    viewModel: MilkViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToReminders: () -> Unit
) {
    val context = LocalContext.current
    val summary by viewModel.monthlySummary.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val allEntries by viewModel.allEntries.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    val monthEntries = remember(allEntries, selectedMonth) {
        allEntries.filter { it.date.startsWith(selectedMonth) }
    }
    val monthPayments = remember(allPayments, selectedMonth) {
        allPayments.filter { it.month == selectedMonth }
    }

    var showWhatsAppPreviewDialog by remember { mutableStateOf(false) }
    var showRecordPaymentDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "मासिक बिल (Monthly Bill)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "वापस जाएं")
                    }
                },
                actions = {
                    IconButton(onClick = { showWhatsAppPreviewDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "व्हाट्सएप पर शेयर करें", tint = DairyBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                MonthSwitcherBar(
                    selectedMonth = selectedMonth,
                    onPrev = { viewModel.previousMonth() },
                    onNext = { viewModel.nextMonth() }
                )
            }

            // Automatic Bill Invoice Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = userProfile?.dairyName?.ifBlank { "दूध हिसाब" } ?: "दूध हिसाब",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DairyBlue)
                                )
                                Text(
                                    text = "बिल महीना: $selectedMonth",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Payment Status Badge
                            val isFullyPaid = summary.totalMilkAmount > 0 && summary.pendingBalance <= 0
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isFullyPaid) PaidGreen.copy(alpha = 0.15f)
                                        else if (summary.pendingBalance > 0) DueRed.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isFullyPaid) "पूर्ण भुगतान (Paid)"
                                    else if (summary.pendingBalance > 0) "बकाया (Due)"
                                    else "कोई बिल नहीं",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFullyPaid) PaidGreen
                                        else if (summary.pendingBalance > 0) DueRed
                                        else MaterialTheme.colorScheme.outline
                                    )
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        // Breakdown Items
                        BillRowItem("दूध लिए गए दिन", "${summary.daysMilkDelivered} दिन")
                        if (summary.daysSkipped > 0) {
                            BillRowItem("दूध नहीं लिया (नागा)", "${summary.daysSkipped} दिन", isRed = true)
                        }
                        BillRowItem("सुबह का कुल दूध", String.format(Locale.US, "%.1f L", summary.morningLitres))
                        BillRowItem("शाम का कुल दूध", String.format(Locale.US, "%.1f L", summary.eveningLitres))
                        if (summary.cowLitres > 0) {
                            BillRowItem("गाय का दूध", String.format(Locale.US, "%.1f L", summary.cowLitres))
                        }
                        if (summary.buffaloLitres > 0) {
                            BillRowItem("भैंस का दूध", String.format(Locale.US, "%.1f L", summary.buffaloLitres))
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        // Litres & Totals
                        BillRowItem(
                            title = "कुल दूध मात्रा (Total Milk)",
                            value = String.format(Locale.US, "%.1f L", summary.totalLitres),
                            isBold = true
                        )
                        BillRowItem(
                            title = "कुल बिल राशि (Total Bill)",
                            value = String.format(Locale.US, "₹%.2f", summary.totalMilkAmount),
                            isBold = true,
                            color = DairyBlue
                        )
                        BillRowItem(
                            title = "जमा किया गया (Paid Amount)",
                            value = String.format(Locale.US, "₹%.2f", summary.totalPaidAmount),
                            color = PaidGreen
                        )

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        // Grand Net Pending
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "शुद्ध बकाया (Net Balance Due):",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = String.format(Locale.US, "₹%.2f", summary.pendingBalance),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (summary.pendingBalance > 0) DueRed else PaidGreen
                                )
                            )
                        }

                        if (userProfile?.upiId?.isNotBlank() == true) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "UPI ID: ${userProfile?.upiId}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Primary WhatsApp & Payment Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showWhatsAppPreviewDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("send_whatsapp_bill_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366), // WhatsApp Green
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("व्हाट्सएप पर भेजें", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = { showRecordPaymentDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("record_payment_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DairyBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("भुगतान जोड़ें", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Payment Reminder Prompt Banner
            if (summary.pendingBalance > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = DairyAmber.copy(alpha = 0.12f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = DairyAmber, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "बकाया राशि का रिमाइंडर भेजें",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "₹${String.format(Locale.US, "%.2f", summary.pendingBalance)} का भुगतान स्मरण पत्र तैयार है",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Button(
                                onClick = onNavigateToReminders,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DairyAmber, contentColor = Color.White)
                            ) {
                                Text("भेजें", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Payments Received in Month Section
            if (monthPayments.isNotEmpty()) {
                item {
                    Text(
                        text = "इस माह प्राप्त भुगतान (Payments)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                items(monthPayments, key = { it.id }) { payment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "तारीख: ${payment.date} (${payment.paymentMethod})",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                if (payment.notes.isNotBlank()) {
                                    Text(payment.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(
                                text = String.format(Locale.US, "₹%.2f", payment.amount),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PaidGreen)
                            )
                        }
                    }
                }
            }

            // Daily Deliveries Details Section
            item {
                Text(
                    text = "दैनिक विवरण (${monthEntries.size} दिन)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (monthEntries.isEmpty()) {
                item {
                    Text(
                        text = "इस माह कोई प्रविष्टि नहीं है।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                items(monthEntries, key = { it.id }) { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${entry.date} - ${if (entry.shift == "MORNING") "सुबह" else "शाम"}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = if (entry.isSkipped) "नागा (दूध नहीं लिया)" else "${entry.quantity}L @ ₹${entry.rate}/L",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (entry.isSkipped) DueRed else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (entry.isSkipped) "₹0.00" else String.format(Locale.US, "₹%.2f", entry.totalAmount),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = DairyBlue)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // WhatsApp Bill Share Preview Dialog
    if (showWhatsAppPreviewDialog) {
        val initialText = remember(summary, userProfile) {
            WhatsAppShareHelper.generateBillText(summary, userProfile, isHindi = true)
        }
        var editableMessage by remember { mutableStateOf(initialText) }
        var targetPhone by remember { mutableStateOf(userProfile?.contactPhone.orEmpty()) }

        AlertDialog(
            onDismissRequest = { showWhatsAppPreviewDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF25D366))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("व्हाट्सएप पर बिल भेजें")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = targetPhone,
                        onValueChange = { targetPhone = it },
                        label = { Text("ग्राहक / डेयरी का मोबाइल नंबर") },
                        placeholder = { Text("उदा. 9876543210") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text("संदेश पूर्वावलोकन (Message Preview):", style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = editableMessage,
                        onValueChange = { editableMessage = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        maxLines = 10
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        WhatsAppShareHelper.shareToWhatsApp(
                            context = context,
                            message = editableMessage,
                            phoneNumber = targetPhone
                        )
                        showWhatsAppPreviewDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Text("व्हाट्सएप खोलें", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWhatsAppPreviewDialog = false }) {
                    Text("रद्द करें")
                }
            }
        )
    }

    // Record Payment Dialog
    if (showRecordPaymentDialog) {
        var amountStr by remember { mutableStateOf(if (summary.pendingBalance > 0) summary.pendingBalance.toString() else "") }
        var paymentDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
        var paymentMethod by remember { mutableStateOf("UPI") }
        var paymentNotes by remember { mutableStateOf("मासिक बिल भुगतान") }

        AlertDialog(
            onDismissRequest = { showRecordPaymentDialog = false },
            title = { Text("भुगतान दर्ज करें (Record Payment)") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("प्राप्त राशि (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = paymentDate,
                        onValueChange = { paymentDate = it },
                        label = { Text("भुगतान तारीख (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text("भुगतान का माध्यम:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("UPI", "CASH", "BANK").forEach { method ->
                            OutlinedButton(
                                onClick = { paymentMethod = method },
                                modifier = Modifier.weight(1f),
                                colors = if (paymentMethod == method) ButtonDefaults.outlinedButtonColors(containerColor = DairyBlue.copy(alpha = 0.15f)) else ButtonDefaults.outlinedButtonColors()
                            ) {
                                Text(
                                    when (method) {
                                        "UPI" -> "UPI"
                                        "CASH" -> "नकद"
                                        else -> "बैंक"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = if (paymentMethod == method) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = paymentNotes,
                        onValueChange = { paymentNotes = it },
                        label = { Text("विवरण (नोट)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.recordPayment(
                                date = paymentDate,
                                month = selectedMonth,
                                amount = amt,
                                method = paymentMethod,
                                notes = paymentNotes
                            )
                            showRecordPaymentDialog = false
                            Toast.makeText(context, "भुगतान सुरक्षित हो गया", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "कृपया वैध राशि दर्ज करें", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DairyBlue)
                ) {
                    Text("सुरक्षित करें")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecordPaymentDialog = false }) {
                    Text("रद्द करें")
                }
            }
        )
    }
}

@Composable
private fun BillRowItem(
    title: String,
    value: String,
    isBold: Boolean = false,
    isRed: Boolean = false,
    color: Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            else MaterialTheme.typography.bodyMedium,
            color = if (isRed) DueRed else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            else MaterialTheme.typography.bodyMedium,
            color = color ?: (if (isRed) DueRed else MaterialTheme.colorScheme.onSurface)
        )
    }
}
