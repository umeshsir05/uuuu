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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DairyAmber
import com.example.ui.theme.DairyBlue
import com.example.ui.viewmodel.MilkViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateManagementScreen(
    viewModel: MilkViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val rateHistory by viewModel.rateHistory.collectAsState()

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var selectedMilkType by remember { mutableStateOf("COW") }
    var newRateStr by remember { mutableStateOf("") }
    var effectiveDate by remember { mutableStateOf(todayStr) }
    var changeNote by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "दूध के रेट बदलें (Milk Rates)",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "वर्तमान लागू दरें (Current Active Rates)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Current Rates Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RateBadgeCard(
                        title = "गाय का दूध",
                        englishTitle = "Cow Milk",
                        rate = userProfile?.cowRate ?: 60.0,
                        modifier = Modifier.weight(1f),
                        accentColor = DairyBlue
                    )
                    RateBadgeCard(
                        title = "भैंस का दूध",
                        englishTitle = "Buffalo Milk",
                        rate = userProfile?.buffaloRate ?: 75.0,
                        modifier = Modifier.weight(1f),
                        accentColor = DairyAmber
                    )
                }
            }

            // Rate Change Form Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = DairyBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "नया रेट लागू करें (Update Rate)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Text("दूध का प्रकार चुनें:", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedMilkType == "COW",
                                onClick = {
                                    selectedMilkType = "COW"
                                    newRateStr = (userProfile?.cowRate ?: 60.0).toString()
                                },
                                label = { Text("गाय (Cow)") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = selectedMilkType == "BUFFALO",
                                onClick = {
                                    selectedMilkType = "BUFFALO"
                                    newRateStr = (userProfile?.buffaloRate ?: 75.0).toString()
                                },
                                label = { Text("भैंस (Buffalo)") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = newRateStr,
                            onValueChange = { newRateStr = it },
                            label = { Text("नई दर (₹ प्रति लीटर)") },
                            placeholder = { Text("उदा. 65") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_rate_input"),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.CurrencyRupee, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        )

                        OutlinedTextField(
                            value = effectiveDate,
                            onValueChange = { effectiveDate = it },
                            label = { Text("लागू होने की तारीख (Effective Date)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = changeNote,
                            onValueChange = { changeNote = it },
                            label = { Text("रेट बदलने का कारण / टिप्पणी (वैकल्पिक)") },
                            placeholder = { Text("उदा. चारा महंगा, डेयरी संशोधन") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                val parsedRate = newRateStr.toDoubleOrNull()
                                if (parsedRate != null && parsedRate > 0) {
                                    viewModel.updateMilkRate(
                                        milkType = selectedMilkType,
                                        newRate = parsedRate,
                                        effectiveDate = effectiveDate,
                                        notes = changeNote
                                    )
                                    changeNote = ""
                                    Toast.makeText(context, "नया रेट सफलतापूर्वक सुरक्षित हो गया!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "कृपया वैध रेट दर्ज करें", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_rate_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DairyBlue, contentColor = Color.White)
                        ) {
                            Text("रेट अपडेट करें", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Rate History Section
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "रेट बदलाव का इतिहास (Rate History)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            if (rateHistory.isEmpty()) {
                item {
                    Text(
                        text = "अभी तक कोई रेट संशोधन दर्ज नहीं है।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                items(rateHistory, key = { it.id }) { item ->
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
                                    text = "${if (item.milkType == "COW") "गाय का दूध" else "भैंस का दूध"} - लागू: ${item.effectiveDate}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                if (item.notes.isNotBlank()) {
                                    Text(item.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(
                                text = "₹${item.rate} / L",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DairyBlue)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun RateBadgeCard(
    title: String,
    englishTitle: String,
    rate: Double,
    modifier: Modifier = Modifier,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = accentColor)
            )
            Text(
                text = englishTitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format(Locale.US, "₹%.2f / L", rate),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = accentColor)
            )
        }
    }
}
