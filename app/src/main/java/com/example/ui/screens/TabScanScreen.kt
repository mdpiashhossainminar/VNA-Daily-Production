package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.camera.CameraQrScanner
import com.example.camera.GalleryQrDecoder
import com.example.model.OperationMode
import com.example.model.PlateScan
import com.example.ui.theme.VnaBorder
import com.example.ui.theme.VnaNavyBackground
import com.example.ui.theme.VnaNavyCard
import com.example.ui.theme.VnaRed
import com.example.ui.theme.VnaTextDark
import com.example.ui.theme.VnaTextPrimary
import com.example.ui.theme.VnaTextSecondary
import com.example.ui.theme.VnaYellow

@Composable
fun TabScanScreen(
    currentMode: OperationMode,
    loadPlates: List<PlateScan>,
    unloadPlates: List<PlateScan>,
    stockCheckCount: Int,
    housekeepingCount: Int,
    isCameraScanning: Boolean,
    manualInput: String,
    plateToDelete: PlateScan?,
    onModeSelect: (OperationMode) -> Unit,
    onSetCameraScanning: (Boolean) -> Unit,
    onManualInputChange: (String) -> Unit,
    onAddPlateScan: (String) -> Unit,
    onAddMultiplePlates: (List<String>) -> Unit,
    onDecrementStockCheck: () -> Unit,
    onDecrementHousekeeping: () -> Unit,
    onPromptDeletePlate: (PlateScan?) -> Unit,
    onConfirmDeletePlate: () -> Unit
) {
    val context = LocalContext.current

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onSetCameraScanning(true)
        } else {
            Toast.makeText(context, "ক্যামেরা পারমিশন প্রয়োজন", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            GalleryQrDecoder.decodeImages(context, uris) { detectedPlates ->
                if (detectedPlates.isNotEmpty()) {
                    onAddMultiplePlates(detectedPlates)
                    Toast.makeText(context, "${detectedPlates.size} টি এন্ট্রি যুক্ত হয়েছে", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "ছবিতে কোনো QR কোড পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val isCounterMode = currentMode == OperationMode.STOCK_CHECK || currentMode == OperationMode.HOUSEKEEPING
    val currentBoxList = if (currentMode == OperationMode.LOAD) loadPlates else unloadPlates

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VnaNavyBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card "কী কাজ করছেন?" (2x2 grid)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_work_mode"),
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
                    text = "কী কাজ করছেন?",
                    color = VnaYellow,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                // 2x2 grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModeButton(
                        mode = OperationMode.LOAD,
                        isSelected = currentMode == OperationMode.LOAD,
                        modifier = Modifier.weight(1f),
                        onClick = { onModeSelect(OperationMode.LOAD) }
                    )
                    ModeButton(
                        mode = OperationMode.UNLOAD,
                        isSelected = currentMode == OperationMode.UNLOAD,
                        modifier = Modifier.weight(1f),
                        onClick = { onModeSelect(OperationMode.UNLOAD) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModeButton(
                        mode = OperationMode.STOCK_CHECK,
                        isSelected = currentMode == OperationMode.STOCK_CHECK,
                        modifier = Modifier.weight(1f),
                        onClick = { onModeSelect(OperationMode.STOCK_CHECK) }
                    )
                    ModeButton(
                        mode = OperationMode.HOUSEKEEPING,
                        isSelected = currentMode == OperationMode.HOUSEKEEPING,
                        modifier = Modifier.weight(1f),
                        onClick = { onModeSelect(OperationMode.HOUSEKEEPING) }
                    )
                }
            }
        }

        // Card Scanning Actions
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_scan_actions"),
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
                // Two scan buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val hasCam = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasCam) {
                                onSetCameraScanning(!isCameraScanning)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCameraScanning) VnaRed else VnaYellow,
                            contentColor = if (isCameraScanning) VnaTextPrimary else VnaTextDark
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_camera_toggle")
                    ) {
                        Text(
                            text = if (isCameraScanning) "ক্যামেরা বন্ধ" else "ক্যামেরা চালু",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            galleryPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VnaNavyBackground,
                            contentColor = VnaTextPrimary
                        ),
                        border = BorderStroke(1.dp, VnaBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_gallery_scan")
                    ) {
                        Text(
                            text = "ছবি থেকে স্ক্যান",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // If camera scanning active, show live camera viewport
                if (isCameraScanning) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        CameraQrScanner(
                            modifier = Modifier.fillMaxSize(),
                            onPlateScanned = { rawPlate ->
                                onAddPlateScan(rawPlate)
                            }
                        )
                    }
                }

                if (isCounterMode) {
                    // Counter control with +1 and -1 button
                    val currentCounter = if (currentMode == OperationMode.STOCK_CHECK) stockCheckCount else housekeepingCount
                    val onDecrement = if (currentMode == OperationMode.STOCK_CHECK) onDecrementStockCheck else onDecrementHousekeeping

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(VnaNavyBackground)
                            .border(1.dp, VnaBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${currentMode.banglaTitle} কাউন্টার",
                                color = VnaTextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "$currentCounter / 10",
                                color = VnaYellow,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Small -1 button to correct a mistake
                            Button(
                                onClick = onDecrement,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VnaNavyCard,
                                    contentColor = VnaRed
                                ),
                                border = BorderStroke(1.dp, VnaRed.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("btn_decrement_counter")
                            ) {
                                Text("-1", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }

                            // +1 button
                            Button(
                                onClick = { onAddPlateScan("1") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VnaYellow,
                                    contentColor = VnaTextDark
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("btn_increment_counter")
                            ) {
                                Text("+1 যোগ", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Manual input "প্লেট নম্বর" with "যোগ" button (Numeric Keyboard only!)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = manualInput,
                            onValueChange = onManualInputChange,
                            placeholder = { Text("প্লেট নম্বর (সংখ্যা)", color = VnaTextSecondary, fontSize = 14.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_manual_plate"),
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

                        Button(
                            onClick = {
                                if (manualInput.isNotBlank()) {
                                    onAddPlateScan(manualInput)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VnaYellow,
                                contentColor = VnaTextDark
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("btn_manual_add")
                        ) {
                            Text(
                                text = "যোগ",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Card: 4 Count tiles & List of entries for Load/Unload
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("card_counts_and_list"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = VnaNavyCard),
            border = BorderStroke(1.dp, VnaBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 4 count tiles with Achieved / Target style (e.g. 12/85)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CountTile(
                        title = "লোড",
                        achieved = loadPlates.size,
                        target = 85,
                        modifier = Modifier.weight(1f)
                    )
                    CountTile(
                        title = "আনলোড",
                        achieved = unloadPlates.size,
                        target = 85,
                        modifier = Modifier.weight(1f)
                    )
                    CountTile(
                        title = "স্টক চেক",
                        achieved = stockCheckCount,
                        target = 10,
                        modifier = Modifier.weight(1f)
                    )
                    CountTile(
                        title = "হাউসকিপিং",
                        achieved = housekeepingCount,
                        target = 10,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (isCounterMode) {
                    // For Stock Check and Housekeeping: NEVER show a number list!
                    val currentCounter = if (currentMode == OperationMode.STOCK_CHECK) stockCheckCount else housekeepingCount
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${currentMode.banglaTitle} সম্পন্ন: $currentCounter / 10",
                                color = VnaTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "প্রতি স্ক্যানে সংখ্যা +1 যুক্ত হয় (বক্স নম্বরের তালিকা সংরক্ষিত হয় না)",
                                color = VnaTextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Section title: "<mode> তালিকা (count)"
                    Text(
                        text = "${currentMode.banglaTitle} বক্স তালিকা (${currentBoxList.size})",
                        color = VnaTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (currentBoxList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "কোনো বক্স নম্বর যোগ করা হয়নি",
                                color = VnaTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            itemsIndexed(currentBoxList, key = { _, item -> item.id }) { index, plate ->
                                PlateItemRow(
                                    sl = index + 1,
                                    plate = plate,
                                    onClick = { onPromptDeletePlate(plate) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (plateToDelete != null) {
        AlertDialog(
            onDismissRequest = { onPromptDeletePlate(null) },
            title = {
                Text(
                    text = "বক্স নম্বর মুছবেন?",
                    color = VnaTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "আপনি কি '${plateToDelete.boxNumber}' মুছে ফেলতে চান?",
                    color = VnaTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDeletePlate,
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("মুছুন", color = VnaRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { onPromptDeletePlate(null) }) {
                    Text("বাতিল", color = VnaTextSecondary)
                }
            },
            containerColor = VnaNavyCard
        )
    }
}

@Composable
private fun ModeButton(
    mode: OperationMode,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) VnaYellow else VnaNavyBackground)
            .border(
                1.dp,
                if (isSelected) VnaYellow else VnaBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = mode.banglaTitle,
                color = if (isSelected) VnaTextDark else VnaTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = mode.englishTitle,
                color = if (isSelected) VnaTextDark else VnaTextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun CountTile(
    title: String,
    achieved: Int,
    target: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(VnaNavyBackground)
            .border(1.dp, VnaBorder, RoundedCornerShape(8.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = VnaTextSecondary,
                fontSize = 11.sp,
                maxLines = 1
            )
            Text(
                text = "$achieved/$target",
                color = if (achieved >= target) VnaYellow else VnaTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PlateItemRow(
    sl: Int,
    plate: PlateScan,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(VnaNavyBackground)
            .border(1.dp, VnaBorder, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$sl",
            color = VnaTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.width(36.dp)
        )

        Text(
            text = plate.boxNumber,
            color = VnaTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = VnaTextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}
