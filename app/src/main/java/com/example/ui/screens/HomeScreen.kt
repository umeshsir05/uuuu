package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.data.model.MilkEntry
import com.example.ui.theme.DairyAmber
import com.example.ui.theme.DairyBlue
import com.example.ui.theme.DueRed
import com.example.ui.theme.PaidGreen
import com.example.ui.viewmodel.MilkViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MilkViewModel,
    onNavigateToBill: () -> Unit,
    onNavigateToRates: () -> Unit
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val allEntries by viewModel.allEntries.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val summary by viewModel.monthlySummary.collectAsState()

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var showAddDialog by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<MilkEntry?>(null) }
    var entryToDelete by remember { mutableStateOf<MilkEntry?>(null) }

    // Filter entries for the selected month
    val monthEntries = remember(allEntries, selectedMonth) {
        allEntries.filter { it.date.startsWith(selectedMonth) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = userProfile?.dairyName?.ifBlank { "दूध हिसाब" } ?: "दूध हिसाब",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "दैनिक दूध का हिसाब",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    Button(
                        onClick = onNavigateToRates,
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DairyAmber,
                            contentColor = Color.White
                        )
                    ) {
                        Text("रेट बदलें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
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
            // Month Switcher Header
            item {
                Spacer(modifier = Modifier.height(4.dp))
                MonthSwitcherBar(
                    selectedMonth = selectedMonth,
                    onPrev = { viewModel.previousMonth() },
                    onNext = { viewModel.nextMonth() }
                )
            }

            // Quick Month Summary Card
            item {
                MonthQuickSummaryCard(
                    totalLitres = summary.totalLitres,
                    totalAmount = summary.totalMilkAmount,
                    pendingBalance = summary.pendingBalance,
                    onViewBill = onNavigateToBill
                )
            }

            // Quick Add Milk for Today Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DairyBlue.copy(alpha = 0.08f)
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
                                text = "आज का दूध दर्ज करें",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DairyBlue
                                )
                            )
                            Text(
                                text = "तारीख: $todayStr (सुबह / शाम)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = {
                                entryToEdit = null
                                showAddDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DairyBlue,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("add_milk_entry_fab")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("एंट्री करें")
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "दूध प्रविष्टियां (${monthEntries.size} रिकॉर्ड)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = onNavigateToBill) {
                        Text("मासिक बिल देखें →", color = DairyBlue)
                    }
                }
            }

            if (monthEntries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalDrink,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "इस माह में अभी कोई दूध प्रविष्टि नहीं है।",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    entryToEdit = null
                                    showAddDialog = true
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("पहली एंट्री जोड़ें")
                            }
                        }
                    }
                }
            } else {
                items(monthEntries, key = { it.id }) { entry ->
                    MilkEntryItemCard(
                        entry = entry,
                        onEdit = {
                            entryToEdit = entry
                            showAddDialog = true
                        },
                        onDelete = {
                            entryToDelete = entry
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Add / Edit Entry Dialog
    if (showAddDialog) {
        val defaultRate = when (entryToEdit?.milkType ?: "COW") {
            "COW" -> userProfile?.cowRate ?: 60.0
            "BUFFALO" -> userProfile?.buffaloRate ?: 75.0
            else -> userProfile?.defaultRate ?: 60.0
        }

        AddEditEntryDialog(
            initialEntry = entryToEdit,
            defaultDate = todayStr,
            defaultRate = defaultRate,
            cowRate = userProfile?.cowRate ?: 60.0,
            buffaloRate = userProfile?.buffaloRate ?: 75.0,
            onDismiss = {
                showAddDialog = false
                entryToEdit = null
            },
            onSave = { date, shift, milkType, quantity, rate, isSkipped, notes ->
                viewModel.addOrUpdateEntry(
                    date = date,
                    shift = shift,
                    milkType = milkType,
                    quantity = quantity,
                    rate = rate,
                    isSkipped = isSkipped,
                    notes = notes,
                    existingId = entryToEdit?.id.orEmpty()
                )
                showAddDialog = false
                entryToEdit = null
                Toast.makeText(context, "प्रविष्टि सुरक्षित हो गई", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Confirmation Dialog
    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("प्रविष्टि हटाएं?") },
            text = { Text("क्या आप ${entryToDelete?.date} की यह दूध प्रविष्टि हटाना चाहते हैं?") },
            confirmButton = {
                Button(
                    onClick = {
                        entryToDelete?.id?.let { viewModel.deleteEntry(it) }
                        entryToDelete = null
                        Toast.makeText(context, "प्रविष्टि हटा दी गई", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DueRed)
                ) {
                    Text("हटाएं")
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("रद्द करें")
                }
            }
        )
    }
}

@Composable
fun MonthSwitcherBar(
    selectedMonth: String,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrev) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "पिछला महीना")
            }
            Text(
                text = formatMonthHindi(selectedMonth),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = onNext) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "अगला महीना")
            }
        }
    }
}

private fun formatMonthHindi(yearMonth: String): String {
    return try {
        val parts = yearMonth.split("-")
        val month = parts[1].toInt()
        val year = parts[0]
        val names = arrayOf(
            "", "जनवरी", "फरवरी", "मार्च", "अप्रैल", "मई", "जून",
            "जुलाई", "अगस्त", "सितंबर", "अक्टूबर", "नवंबर", "दिसंबर"
        )
        "${names[month]} $year"
    } catch (_: Exception) {
        yearMonth
    }
}

@Composable
fun MonthQuickSummaryCard(
    totalLitres: Double,
    totalAmount: Double,
    pendingBalance: Double,
    onViewBill: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewBill() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = DairyBlue
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "मासिक सारांश (Monthly Summary)",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Text(
                    text = "पूरा बिल देखें →",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = DairyAmber
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "कुल दूध",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f L", totalLitres),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                Column {
                    Text(
                        text = "कुल बिल",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = String.format(Locale.US, "₹%.2f", totalAmount),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                Column {
                    Text(
                        text = "बकाया राशि",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = String.format(Locale.US, "₹%.2f", pendingBalance),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (pendingBalance > 0) Color(0xFFFFD54F) else Color(0xFFC8E6C9)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MilkEntryItemCard(
    entry: MilkEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isSkipped) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Shift Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (entry.isSkipped) DueRed.copy(alpha = 0.15f)
                            else if (entry.shift == "MORNING") DairyAmber.copy(alpha = 0.15f)
                            else DairyBlue.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (entry.isSkipped) Icons.Default.Close
                        else if (entry.shift == "MORNING") Icons.Default.WbSunny
                        else Icons.Default.NightsStay,
                        contentDescription = null,
                        tint = if (entry.isSkipped) DueRed
                        else if (entry.shift == "MORNING") DairyAmber
                        else DairyBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.date,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (entry.shift == "MORNING") "सुबह" else "शाम",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (entry.shift == "MORNING") DairyAmber else DairyBlue,
                            modifier = Modifier
                                .background(
                                    (if (entry.shift == "MORNING") DairyAmber else DairyBlue).copy(alpha = 0.1f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (entry.isSkipped) {
                        Text(
                            text = "दूध नहीं लिया (नागा)",
                            style = MaterialTheme.typography.bodySmall,
                            color = DueRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            text = "${entry.quantity} L × ₹${entry.rate}/L (${when(entry.milkType) { "COW" -> "गाय"; "BUFFALO" -> "भैंस"; else -> "सामान्य" }})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (entry.notes.isNotBlank()) {
                        Text(
                            text = entry.notes,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Amount & Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!entry.isSkipped) {
                    Text(
                        text = String.format(Locale.US, "₹%.2f", entry.totalAmount),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DairyBlue
                        )
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "संशोधित करें", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "हटाएं", tint = DueRed.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun AddEditEntryDialog(
    initialEntry: MilkEntry?,
    defaultDate: String,
    defaultRate: Double,
    cowRate: Double,
    buffaloRate: Double,
    onDismiss: () -> Unit,
    onSave: (date: String, shift: String, milkType: String, quantity: Double, rate: Double, isSkipped: Boolean, notes: String) -> Unit
) {
    var date by remember { mutableStateOf(initialEntry?.date ?: defaultDate) }
    var shift by remember { mutableStateOf(initialEntry?.shift ?: "MORNING") }
    var milkType by remember { mutableStateOf(initialEntry?.milkType ?: "COW") }
    var quantityStr by remember { mutableStateOf(initialEntry?.quantity?.toString() ?: "1.5") }
    var rateStr by remember {
        mutableStateOf(
            initialEntry?.rate?.toString() ?: (if (milkType == "COW") cowRate else buffaloRate).toString()
        )
    }
    var isSkipped by remember { mutableStateOf(initialEntry?.isSkipped ?: false) }
    var notes by remember { mutableStateOf(initialEntry?.notes ?: "") }

    val currentQty = quantityStr.toDoubleOrNull() ?: 0.0
    val currentRate = rateStr.toDoubleOrNull() ?: 0.0
    val calculatedTotal = if (isSkipped) 0.0 else currentQty * currentRate

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialEntry == null) "दूध प्रविष्टि जोड़ें" else "दूध प्रविष्टि बदलें")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Date input
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("तारीख (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Shift Selector
                Text("समय (Shift):", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { shift = "MORNING" },
                        modifier = Modifier.weight(1f),
                        colors = if (shift == "MORNING") ButtonDefaults.outlinedButtonColors(containerColor = DairyAmber.copy(alpha = 0.15f)) else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = DairyAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("सुबह (Morning)", fontWeight = if (shift == "MORNING") FontWeight.Bold else FontWeight.Normal)
                    }
                    OutlinedButton(
                        onClick = { shift = "EVENING" },
                        modifier = Modifier.weight(1f),
                        colors = if (shift == "EVENING") ButtonDefaults.outlinedButtonColors(containerColor = DairyBlue.copy(alpha = 0.15f)) else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Icon(Icons.Default.NightsStay, contentDescription = null, tint = DairyBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("शाम (Evening)", fontWeight = if (shift == "EVENING") FontWeight.Bold else FontWeight.Normal)
                    }
                }

                // Milk Type Selector
                Text("दूध का प्रकार:", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = milkType == "COW",
                        onClick = {
                            milkType = "COW"
                            rateStr = cowRate.toString()
                        },
                        label = { Text("गाय (Cow)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = milkType == "BUFFALO",
                        onClick = {
                            milkType = "BUFFALO"
                            rateStr = buffaloRate.toString()
                        },
                        label = { Text("भैंस (Buffalo)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Skipped / Holiday Checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSkipped,
                        onCheckedChange = { isSkipped = it }
                    )
                    Text("आज दूध नहीं लिया (नागा / छुट्टी)", color = if (isSkipped) DueRed else MaterialTheme.colorScheme.onSurface)
                }

                if (!isSkipped) {
                    // Quick Quantity Buttons
                    Text("मात्रा (लीटर):", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("0.5", "1.0", "1.5", "2.0", "3.0").forEach { q ->
                            OutlinedButton(
                                onClick = { quantityStr = q },
                                modifier = Modifier.weight(1f),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                                colors = if (quantityStr == q) ButtonDefaults.outlinedButtonColors(containerColor = DairyBlue.copy(alpha = 0.15f)) else ButtonDefaults.outlinedButtonColors()
                            ) {
                                Text("${q}L", fontSize = 11.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            label = { Text("मात्रा (L)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = rateStr,
                            onValueChange = { rateStr = it },
                            label = { Text("रेट (₹/L)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Total preview
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DairyBlue.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("कुल राशि:", fontWeight = FontWeight.SemiBold)
                            Text(
                                String.format(Locale.US, "₹%.2f", calculatedTotal),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DairyBlue
                                )
                            )
                        }
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("नोट्स (वैकल्पिक)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = if (isSkipped) 0.0 else (quantityStr.toDoubleOrNull() ?: 1.0)
                    val rt = rateStr.toDoubleOrNull() ?: defaultRate
                    onSave(date, shift, milkType, qty, rt, isSkipped, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DairyBlue)
            ) {
                Text("सुरक्षित करें")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("रद्द करें")
            }
        }
    )
}
