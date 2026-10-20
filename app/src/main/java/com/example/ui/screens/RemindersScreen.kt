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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DairyAmber
import com.example.ui.theme.DairyBlue
import com.example.ui.theme.DueRed
import com.example.ui.theme.PaidGreen
import com.example.ui.util.ReminderTone
import com.example.ui.util.WhatsAppShareHelper
import com.example.ui.viewmodel.MilkViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: MilkViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val summary by viewModel.monthlySummary.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    var selectedTone by remember { mutableStateOf(ReminderTone.POLITE) }
    var phoneNumber by remember { mutableStateOf(userProfile?.contactPhone.orEmpty()) }

    val liveMessage = remember(summary, userProfile, selectedTone) {
        WhatsAppShareHelper.generatePaymentReminderText(
            summary = summary,
            profile = userProfile,
            tone = selectedTone,
            isHindi = true
        )
    }

    var editableMessage by remember(liveMessage) { mutableStateOf(liveMessage) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "पेमेंट रिमाइंडर (Payment Reminder)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "वापस जाएं")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Pending Dues Status Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (summary.pendingBalance > 0) DairyAmber.copy(alpha = 0.12f)
                    else PaidGreen.copy(alpha = 0.12f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "माह: $selectedMonth का बकाया",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(Locale.US, "₹%.2f", summary.pendingBalance),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (summary.pendingBalance > 0) DueRed else PaidGreen
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                (if (summary.pendingBalance > 0) DairyAmber else PaidGreen).copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = if (summary.pendingBalance > 0) DairyAmber else PaidGreen
                        )
                    }
                }
            }

            // Recipient Phone
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("प्राप्तकर्ता का व्हाट्सएप नंबर") },
                placeholder = { Text("उदा. 9876543210") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reminder_phone_input"),
                singleLine = true
            )

            // Tone Selector
            Text("रिमाइंडर का प्रारूप (Message Tone):", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ReminderTone.values().forEach { tone ->
                    FilterChip(
                        selected = selectedTone == tone,
                        onClick = {
                            selectedTone = tone
                            editableMessage = WhatsAppShareHelper.generatePaymentReminderText(summary, userProfile, tone, true)
                        },
                        label = { Text(tone.titleHindi, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Editable Preview
            Text("व्हाट्सएप संदेश पूर्वावलोकन:", style = MaterialTheme.typography.labelMedium)
            OutlinedTextField(
                value = editableMessage,
                onValueChange = { editableMessage = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10
            )

            // Send Button
            Button(
                onClick = {
                    WhatsAppShareHelper.shareToWhatsApp(
                        context = context,
                        message = editableMessage,
                        phoneNumber = phoneNumber
                    )
                    Toast.makeText(context, "व्हाट्सएप खोला जा रहा है...", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("send_whatsapp_reminder_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF25D366), // WhatsApp Green
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("व्हाट्सएप पर रिमाइंडर भेजें", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
