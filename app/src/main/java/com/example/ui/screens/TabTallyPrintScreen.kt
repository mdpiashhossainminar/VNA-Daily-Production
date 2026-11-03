package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChecklistAnswers
import com.example.ui.theme.VnaBorder
import com.example.ui.theme.VnaDivider
import com.example.ui.theme.VnaNavyBackground
import com.example.ui.theme.VnaNavyCard
import com.example.ui.theme.VnaRed
import com.example.ui.theme.VnaTextDark
import com.example.ui.theme.VnaTextPrimary
import com.example.ui.theme.VnaTextSecondary
import com.example.ui.theme.VnaYellow

@Composable
fun TabTallyPrintScreen(
    loadCount: Int,
    unloadCount: Int,
    stockCheckCount: Int,
    housekeepingCount: Int,
    checklist: ChecklistAnswers,
    showResetConfirmation: Boolean,
    onChecklistChange: ((ChecklistAnswers) -> ChecklistAnswers) -> Unit,
    onPromptReset: (Boolean) -> Unit,
    onConfirmReset: () -> Unit,
    onPrintClick: () -> Unit,
    onSharePdfClick: () -> Unit
) {
    val totalAchieved = loadCount + unloadCount + stockCheckCount + housekeepingCount
    val totalTarget = 190
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VnaNavyBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card "শিফট শেষের হিসাব"
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_shift_tally"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = VnaNavyCard),
            border = BorderStroke(1.dp, VnaBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "শিফট শেষের হিসাব",
                    color = VnaYellow,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(VnaNavyBackground)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Category",
                        color = VnaTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(2f)
                    )
                    Text(
                        text = "Target",
                        color = VnaTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Achieved",
                        color = VnaTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }

                TallyRow(category = "Plate Load", target = 85, achieved = loadCount)
                TallyRow(category = "Plate Unload", target = 85, achieved = unloadCount)
                TallyRow(category = "Stock Check Physically", target = 10, achieved = stockCheckCount)
                TallyRow(category = "Housekeeping / Segregation", target = 10, achieved = housekeepingCount)

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(VnaDivider)
                )

                // Total Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(VnaNavyBackground)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total",
                        color = VnaYellow,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(2f)
                    )
                    Text(
                        text = "$totalTarget",
                        color = VnaYellow,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$totalAchieved",
                        color = VnaYellow,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Card Inputs (Checkpoints 9, 10, 12, and reason textarea)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_discrepancies_and_reason"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = VnaNavyCard),
            border = BorderStroke(1.dp, VnaBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "অতিরিক্ত তথ্য ও কারণ",
                    color = VnaYellow,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                // Checkpoint 10: ক্ষতিগ্রস্ত প্লেট (Qty)
                NumericInputField(
                    label = "ক্ষতিগ্রস্ত প্লেট (Qty) [Check Point 10]",
                    value = checklist.chk10DamagedPlateQty,
                    onValueChange = { newVal -> onChecklistChange { it.copy(chk10DamagedPlateQty = newVal) } },
                    testTag = "input_damaged_plate_qty"
                )

                // Checkpoint 9: ভুল লোকেশনে থাকা (Qty)
                NumericInputField(
                    label = "ভুল লোকেশনে থাকা (Qty) [Check Point 9]",
                    value = checklist.chk9WrongLocationYarnQty,
                    onValueChange = { newVal -> onChecklistChange { it.copy(chk9WrongLocationYarnQty = newVal) } },
                    testTag = "input_wrong_location_qty"
                )

                // Checkpoint 12: VNA ডাউনটাইম (min)
                NumericInputField(
                    label = "VNA ডাউনটাইম (min) [Check Point 12]",
                    value = checklist.chk12DowntimeMinutes,
                    onValueChange = { newVal -> onChecklistChange { it.copy(chk12DowntimeMinutes = newVal) } },
                    testTag = "input_downtime_minutes"
                )

                // Textarea: টার্গেট অর্জন না করার কারণ
                OutlinedTextField(
                    value = checklist.reasonNotAchievingTarget,
                    onValueChange = { newVal -> onChecklistChange { it.copy(reasonNotAchievingTarget = newVal) } },
                    label = { Text("টার্গেট অর্জন না করার কারণ", color = VnaTextSecondary, fontSize = 13.sp) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_target_reason"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = VnaTextPrimary,
                        unfocusedTextColor = VnaTextPrimary,
                        focusedBorderColor = VnaYellow,
                        unfocusedBorderColor = VnaBorder,
                        focusedContainerColor = VnaNavyBackground,
                        unfocusedContainerColor = VnaNavyBackground
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // Action Buttons: প্রিন্ট, পিডিএফ শেয়ার, নতুন শিট
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onPrintClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VnaYellow,
                    contentColor = VnaTextDark
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_print_worksheet")
            ) {
                Text(
                    text = "প্রিন্ট",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onSharePdfClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VnaNavyCard,
                    contentColor = VnaTextPrimary
                ),
                border = BorderStroke(1.dp, VnaBorder),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_share_pdf")
            ) {
                Text(
                    text = "পিডিএফ শেয়ার করুন",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = { onPromptReset(true) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = VnaNavyBackground,
                    contentColor = VnaRed
                ),
                border = BorderStroke(1.dp, VnaRed.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_new_sheet")
            ) {
                Text(
                    text = "নতুন শিট",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Reset Confirmation Dialog
    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { onPromptReset(false) },
            title = {
                Text(
                    text = "নতুন শিট শুরু করবেন?",
                    color = VnaTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "বর্তমান শিফটের সমস্ত স্ক্যান ও চেকলিস্ট তথ্য মুছে ফেলা হবে। আপনি কি নিশ্চিত?",
                    color = VnaTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmReset,
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text("হ্যাঁ, নতুন শিট", color = VnaRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { onPromptReset(false) }) {
                    Text("বাতিল", color = VnaTextSecondary)
                }
            },
            containerColor = VnaNavyCard
        )
    }
}

@Composable
private fun TallyRow(
    category: String,
    target: Int,
    achieved: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category,
            color = VnaTextPrimary,
            fontSize = 13.sp,
            modifier = Modifier.weight(2f)
        )
        Text(
            text = "$target",
            color = VnaTextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$achieved",
            color = if (achieved >= target) VnaYellow else VnaTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun NumericInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter { c -> c.isDigit() }) },
        label = { Text(label, color = VnaTextSecondary, fontSize = 13.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = VnaTextPrimary,
            unfocusedTextColor = VnaTextPrimary,
            focusedBorderColor = VnaYellow,
            unfocusedBorderColor = VnaBorder,
            focusedContainerColor = VnaNavyBackground,
            unfocusedContainerColor = VnaNavyBackground
        ),
        shape = RoundedCornerShape(8.dp)
    )
}
