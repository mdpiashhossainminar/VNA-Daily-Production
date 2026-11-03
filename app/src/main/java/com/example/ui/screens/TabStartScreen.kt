package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChecklistAnswers
import com.example.model.ShiftInfo
import com.example.model.ShiftType
import com.example.ui.theme.VnaBorder
import com.example.ui.theme.VnaGreen
import com.example.ui.theme.VnaNavyBackground
import com.example.ui.theme.VnaNavyCard
import com.example.ui.theme.VnaTextDark
import com.example.ui.theme.VnaTextPrimary
import com.example.ui.theme.VnaTextSecondary
import com.example.ui.theme.VnaYellow

@Composable
fun TabStartScreen(
    shiftInfo: ShiftInfo,
    checklist: ChecklistAnswers,
    onOperatorNameChange: (String) -> Unit,
    onDesignationChange: (String) -> Unit,
    onIdNumberChange: (String) -> Unit,
    onSupervisorChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onShiftChange: (ShiftType) -> Unit,
    onChecklistChange: ((ChecklistAnswers) -> ChecklistAnswers) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VnaNavyBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card "শিফট তথ্য"
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_shift_info"),
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
                    text = "শিফট তথ্য",
                    color = VnaYellow,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                ShiftInputField(
                    label = "Name (নাম)",
                    value = shiftInfo.operatorName,
                    onValueChange = onOperatorNameChange,
                    testTag = "input_operator_name"
                )

                ShiftInputField(
                    label = "Designation (পদবী)",
                    value = shiftInfo.designation,
                    onValueChange = onDesignationChange,
                    testTag = "input_designation"
                )

                ShiftInputField(
                    label = "ID Number (আইডি নম্বর)",
                    value = shiftInfo.idNumber,
                    onValueChange = onIdNumberChange,
                    testTag = "input_id_number"
                )

                ShiftInputField(
                    label = "Shift Supervisor (শিফট সুপারভাইজার)",
                    value = shiftInfo.shiftSupervisor,
                    onValueChange = onSupervisorChange,
                    testTag = "input_supervisor"
                )

                ShiftInputField(
                    label = "Date (তারিখ: DD.MM.YY)",
                    value = shiftInfo.dateString,
                    onValueChange = onDateChange,
                    testTag = "input_date"
                )

                Text(
                    text = "শিফট নির্বাচন",
                    color = VnaTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                // Shift buttons: A (Morning), B (Evening), C (Night), G (General)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShiftType.values().forEach { shift ->
                        val isSelected = shiftInfo.shift == shift
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) VnaYellow else VnaNavyBackground)
                                .clickable { onShiftChange(shift) }
                                .padding(horizontal = 4.dp)
                                .testTag("shift_btn_${shift.code}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = shift.displayName,
                                color = if (isSelected) VnaTextDark else VnaTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Card "কাজ শুরুর চেকলিস্ট"
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_checklist"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = VnaNavyCard),
            border = BorderStroke(1.dp, VnaBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "কাজ শুরুর চেকলিস্ট",
                    color = VnaYellow,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                ChecklistRow(
                    sl = 1,
                    text = "VNA মেশিন চালুর পূর্ববর্তী পরিদর্শন সম্পন্ন করা হয়েছে",
                    optPositive = "OK",
                    optNegative = "Not OK",
                    isPositiveSelected = checklist.chk1PreInspectOk,
                    onToggle = { onChecklistChange { it.copy(chk1PreInspectOk = it.chk1PreInspectOk.not()) } }
                )

                ChecklistRow(
                    sl = 2,
                    text = "ব্যাটারী ও চার্জিং স্ট্যাটাস চেক করা হয়েছে",
                    optPositive = "OK",
                    optNegative = "Not OK",
                    isPositiveSelected = checklist.chk2BatteryStatusOk,
                    onToggle = { onChecklistChange { it.copy(chk2BatteryStatusOk = it.chk2BatteryStatusOk.not()) } }
                )

                ChecklistRow(
                    sl = 3,
                    text = "টায়ার, ব্রেক, হর্ন ও লাইট পরীক্ষা করা হয়েছে",
                    optPositive = "OK",
                    optNegative = "Not OK",
                    isPositiveSelected = checklist.chk3TyreBrakeLightOk,
                    onToggle = { onChecklistChange { it.copy(chk3TyreBrakeLightOk = it.chk3TyreBrakeLightOk.not()) } }
                )

                ChecklistRow(
                    sl = 4,
                    text = "সেফটি অ্যালার্ম / ইমার্জেন্সি সিস্টেম পরীক্ষা করা হয়েছে",
                    optPositive = "OK",
                    optNegative = "Not OK",
                    isPositiveSelected = checklist.chk4SafetyAlarmOk,
                    onToggle = { onChecklistChange { it.copy(chk4SafetyAlarmOk = it.chk4SafetyAlarmOk.not()) } }
                )

                ChecklistRow(
                    sl = 5,
                    text = "VNA Ailes / Rack এলাকায় কোনো বাধা আছে কি না পরীক্ষা করা হয়েছে",
                    optPositive = "Clear",
                    optNegative = "Issue",
                    isPositiveSelected = checklist.chk5AisleClear,
                    onToggle = { onChecklistChange { it.copy(chk5AisleClear = it.chk5AisleClear.not()) } }
                )

                ChecklistRow(
                    sl = 6,
                    text = "লোডিংয়ের আগে Rack/ Box লোকেশন যাচাই করা হয়েছে",
                    optPositive = "Yes",
                    optNegative = "No",
                    isPositiveSelected = checklist.chk6PreLoadRackVerified,
                    onToggle = { onChecklistChange { it.copy(chk6PreLoadRackVerified = it.chk6PreLoadRackVerified.not()) } }
                )

                ChecklistRow(
                    sl = 7,
                    text = "কোনো ক্ষতি ছাড়াই Rack/Box লোডিং সম্পন্ন করা হয়েছে",
                    optPositive = "Yes",
                    optNegative = "No",
                    isPositiveSelected = checklist.chk7NoDamageLoadCompleted,
                    onToggle = { onChecklistChange { it.copy(chk7NoDamageLoadCompleted = it.chk7NoDamageLoadCompleted.not()) } }
                )

                ChecklistRow(
                    sl = 8,
                    text = "আনলোডিংয়ের আগে Rack/Box লোকেশন যাচাই করা হয়েছে",
                    optPositive = "Yes",
                    optNegative = "No",
                    isPositiveSelected = checklist.chk8PreUnloadRackVerified,
                    onToggle = { onChecklistChange { it.copy(chk8PreUnloadRackVerified = it.chk8PreUnloadRackVerified.not()) } }
                )

                ChecklistRow(
                    sl = 11,
                    text = "খালি লোকেশনের তথ্য আপডেট করা হয়েছে",
                    optPositive = "Done",
                    optNegative = "Pending",
                    isPositiveSelected = checklist.chk11EmptyLocationUpdated,
                    onToggle = { onChecklistChange { it.copy(chk11EmptyLocationUpdated = it.chk11EmptyLocationUpdated.not()) } }
                )

                // 13: accident reported: "No" is safe (positive/green), "Yes" is accident (yellow)
                ChecklistRow(
                    sl = 13,
                    text = "দুর্ঘটনা / নিকট-দুর্ঘটনা / নিরাপত্তাজনিত সমস্যা রিপোর্ট করা হয়েছে",
                    optPositive = "No",
                    optNegative = "Yes",
                    isPositiveSelected = !checklist.chk13AccidentReported,
                    onToggle = { onChecklistChange { it.copy(chk13AccidentReported = it.chk13AccidentReported.not()) } }
                )

                ChecklistRow(
                    sl = 14,
                    text = "শিফট শেষে VNA-এর অবস্থা পরীক্ষা করে সঠিকভাবে পার্ক করা হয়েছে",
                    optPositive = "Done",
                    optNegative = "Pending",
                    isPositiveSelected = checklist.chk14PostShiftParked,
                    onToggle = { onChecklistChange { it.copy(chk14PostShiftParked = it.chk14PostShiftParked.not()) } }
                )
            }
        }
    }
}

@Composable
private fun ShiftInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = VnaTextSecondary, fontSize = 13.sp) },
        singleLine = true,
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

@Composable
private fun ChecklistRow(
    sl: Int,
    text: String,
    optPositive: String,
    optNegative: String,
    isPositiveSelected: Boolean,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$sl. $text",
                color = VnaTextPrimary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Toggle pair
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(VnaNavyBackground)
                    .padding(2.dp)
            ) {
                // Positive option (Green when selected)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isPositiveSelected) VnaGreen else Color.Transparent)
                        .clickable { if (!isPositiveSelected) onToggle() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("chk_${sl}_pos"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = optPositive,
                        color = if (isPositiveSelected) VnaTextDark else VnaTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Negative option (Yellow when selected)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (!isPositiveSelected) VnaYellow else Color.Transparent)
                        .clickable { if (isPositiveSelected) onToggle() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("chk_${sl}_neg"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = optNegative,
                        color = if (!isPositiveSelected) VnaTextDark else VnaTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
