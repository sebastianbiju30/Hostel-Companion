package com.example.hostel_companion.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.hostel_companion.data.ml.HostelMlEngine
import com.example.hostel_companion.data.ml.MlClassificationResult
import com.example.hostel_companion.data.ml.MlMessPredictionResult
import com.example.hostel_companion.ui.theme.*

/**
 * Interactive Machine Learning Showcase & Testing Lab.
 * Allows examiners and users to test on-device NLP classification,
 * hazard urgency scoring, and mess consumption predictive models.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MlLabDialog(onDismiss: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: NLP Triage, 1: Mess Predictor, 2: Model Architecture
    var sampleText by remember {
        mutableStateOf("Water is dripping from ceiling directly over electrical socket, sparking continuously!")
    }
    var mlResult by remember {
        mutableStateOf<MlClassificationResult>(HostelMlEngine.classifyComplaint(sampleText))
    }

    // Mess prediction state
    var selectedMeal by remember { mutableStateOf("Dinner") }
    var selectedDay by remember { mutableStateOf("Sunday") }
    var menuDesc by remember { mutableStateOf("Paneer Butter Masala, Dal Makhani, Phulka, Gulab Jamun") }
    var messResult by remember {
        mutableStateOf<MlMessPredictionResult>(
            HostelMlEngine.predictMessConsumption(selectedMeal, selectedDay, menuDesc)
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            color = DarkCardSurface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(AccentBlueLight, AccentPurple))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Science,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Hostel ML Laboratory",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "On-Device Machine Learning Models",
                                style = MaterialTheme.typography.bodySmall,
                                color = AccentTeal
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Tabs
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = DarkBackground
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        val tabs = listOf("NLP Triage", "Mess Predictor", "Architecture")
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTab == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentBlueLight else Color.Transparent)
                                    .clickable { selectedTab = index }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else TextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            // TAB 1: NLP COMPLAINT TRIAGE & CLASSIFICATION
                            Text(
                                text = "Test Complaint Text (Live Inference)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )

                            OutlinedTextField(
                                value = sampleText,
                                onValueChange = {
                                    sampleText = it
                                    mlResult = HostelMlEngine.classifyComplaint(it)
                                },
                                label = { Text("Enter issue description or hazard details") },
                                minLines = 2,
                                maxLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = AccentBlueLight
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Sample preset buttons
                            Text(
                                text = "Try Sample Scenarios:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val presets = listOf(
                                    "🔥 Sparks from fan switch" to "Sparks flying from ceiling fan switch board",
                                    "💧 Washroom pipe burst" to "Toilet flush tank pipe burst and water overflowing",
                                    "📶 Wi-Fi disconnecting" to "Hostel 5G Wi-Fi dropping connection during exam",
                                    "❄️ AC warm air" to "Split AC blowing warm air and temperature display error"
                                )
                                presets.take(2).forEach { (label, text) ->
                                    AssistChip(
                                        onClick = {
                                            sampleText = text
                                            mlResult = HostelMlEngine.classifyComplaint(text)
                                        },
                                        label = { Text(label, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // INFERENCE RESULTS CARD
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkBackground),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🎯 Predicted Category",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextMuted
                                        )
                                        Surface(
                                            color = AccentBlueLight.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "${mlResult.predictedCategory} (${(mlResult.confidenceScore * 100).toInt()}%)",
                                                color = AccentBlueLight,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Urgency Level & SLA
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🚨 Urgency Severity",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextMuted
                                        )
                                        Surface(
                                            color = Color(mlResult.urgencyColorHex).copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "${mlResult.urgencyLevel} • ETA ${mlResult.estimatedResolutionHours}h",
                                                color = Color(mlResult.urgencyColorHex),
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Triage Analysis:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = mlResult.triageExplanation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )

                                    if (mlResult.detectedKeywords.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Triggered Features: ${mlResult.detectedKeywords.joinToString(", ")}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AccentTeal
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(color = DarkCardSurfaceVariant)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Probability Distribution Chart
                                    Text(
                                        text = "Softmax Probability Distribution:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    mlResult.categoryProbabilities.entries
                                        .sortedByDescending { it.value }
                                        .forEach { (cat, prob) ->
                                            Column(modifier = Modifier.padding(vertical = 3.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(text = cat, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                                    Text(
                                                        text = "${(prob * 100).toInt()}%",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = if (cat == mlResult.predictedCategory) AccentBlueLight else TextMuted
                                                    )
                                                }
                                                LinearProgressIndicator(
                                                    progress = { prob },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp)
                                                        .clip(RoundedCornerShape(3.dp)),
                                                    color = if (cat == mlResult.predictedCategory) AccentBlueLight else DarkCardSurfaceVariant,
                                                    trackColor = DarkCardSurfaceVariant.copy(alpha = 0.4f)
                                                )
                                            }
                                        }
                                }
                            }
                        }

                        1 -> {
                            // TAB 2: MESS PREDICTOR & FOOD CONSUMPTION
                            Text(
                                text = "Mess Footfall & Consumption Regression",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Predicts student turnout, plate requirements, and food waste reduction index.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )

                            // Meal selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Breakfast", "Lunch", "Snacks", "Dinner").forEach { meal ->
                                    val isSel = selectedMeal == meal
                                    OutlinedButton(
                                        onClick = {
                                            selectedMeal = meal
                                            messResult = HostelMlEngine.predictMessConsumption(meal, selectedDay, menuDesc)
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (isSel) AccentBlueLight else Color.Transparent,
                                            contentColor = if (isSel) Color.White else TextMuted
                                        ),
                                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                                    ) {
                                        Text(meal, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }

                            // Day of week selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Wednesday (Weekday)", "Sunday (Weekend)").forEach { dayOption ->
                                    val dayName = if (dayOption.contains("Wednesday")) "Wednesday" else "Sunday"
                                    val isSel = selectedDay == dayName
                                    OutlinedButton(
                                        onClick = {
                                            selectedDay = dayName
                                            messResult = HostelMlEngine.predictMessConsumption(selectedMeal, dayName, menuDesc)
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (isSel) AccentTeal else Color.Transparent,
                                            contentColor = if (isSel) Color.Black else TextMuted
                                        )
                                    ) {
                                        Text(dayOption, fontSize = 11.sp)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = menuDesc,
                                onValueChange = {
                                    menuDesc = it
                                    messResult = HostelMlEngine.predictMessConsumption(selectedMeal, selectedDay, it)
                                },
                                label = { Text("Menu Items (Dish Popularity Model)") },
                                minLines = 2,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Mess Prediction Results
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkBackground),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Expected Attendance", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                            Text(
                                                "${messResult.predictedAttendancePercent}%",
                                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                                color = StatusGreen
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Expected Residents", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                            Text(
                                                "${messResult.expectedStudentCount} / 420",
                                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                                color = TextPrimary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = DarkCardSurfaceVariant)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Dish Popularity Score:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                        Text("${(messResult.popularityScore * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = AccentAmber, fontWeight = FontWeight.Bold)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Food Waste Risk:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                        Text(
                                            messResult.foodWasteRisk,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (messResult.foodWasteRisk == "High") StatusRed else StatusGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Model Recommendations:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    messResult.recommendations.forEach { rec ->
                                        Text(
                                            text = "• $rec",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }

                        2 -> {
                            // TAB 3: MACHINE LEARNING MODEL ARCHITECTURE
                            Text(
                                text = "ML Model Architecture & Math",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )

                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkBackground),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "1. Multinomial Naive Bayes Classifier",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = AccentBlueLight
                                    )
                                    Text(
                                        text = "• Tokenization & Lemmatization with stopword filtering\n• TF-IDF feature weighting across 6 domain classes\n• Laplace Smoothing: P(w|C) = (count(w,C) + α) / (total_tokens + α|V|)\n• Softmax Probability Distribution: σ(z_i) = exp(z_i) / Σ exp(z_j)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "2. Urgency Hazard Regressor",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = AccentAmber
                                    )
                                    Text(
                                        text = "• Evaluates multi-risk hazard signals (fire, electrical sparks, flooding)\n• Predicts SLA hours and severity level (CRITICAL / HIGH / MEDIUM / LOW)\n• Automated warden dispatch queue triage",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "3. Footfall Regression Model",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = StatusGreen
                                    )
                                    Text(
                                        text = "• Multivariate regression correlating meal type, day seasonality, and menu item popularity vectors to minimize kitchen food waste.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

