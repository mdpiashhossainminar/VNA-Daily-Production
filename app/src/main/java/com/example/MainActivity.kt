package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.VnaViewModel
import com.example.ui.components.AboutDialog
import com.example.ui.components.VnaBottomNav
import com.example.ui.components.VnaHeader
import com.example.ui.screens.TabScanScreen
import com.example.ui.screens.TabStartScreen
import com.example.ui.screens.TabTallyPrintScreen
import com.example.ui.theme.VnaNavyBackground
import com.example.ui.theme.VnaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VnaTheme {
                VnaApp()
            }
        }
    }
}

@Composable
fun VnaApp(viewModel: VnaViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle back button for sub-states
    BackHandler(enabled = uiState.isAboutOpen || uiState.isCameraScanning || uiState.selectedTab != 0) {
        when {
            uiState.isAboutOpen -> viewModel.openAbout(false)
            uiState.isCameraScanning -> viewModel.setCameraScanning(false)
            uiState.selectedTab != 0 -> viewModel.selectTab(0)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(VnaNavyBackground),
        topBar = {
            VnaHeader(
                onAboutClick = { viewModel.openAbout(true) }
            )
        },
        bottomBar = {
            VnaBottomNav(
                selectedTab = uiState.selectedTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        },
        containerColor = VnaNavyBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(VnaNavyBackground)
        ) {
            when (uiState.selectedTab) {
                0 -> {
                    TabStartScreen(
                        shiftInfo = uiState.shiftInfo,
                        checklist = uiState.checklistAnswers,
                        onOperatorNameChange = { viewModel.updateOperatorName(it) },
                        onDesignationChange = { viewModel.updateDesignation(it) },
                        onIdNumberChange = { viewModel.updateIdNumber(it) },
                        onSupervisorChange = { viewModel.updateSupervisor(it) },
                        onDateChange = { viewModel.updateDate(it) },
                        onShiftChange = { viewModel.updateShiftType(it) },
                        onChecklistChange = { viewModel.updateChecklist(it) }
                    )
                }
                1 -> {
                    TabScanScreen(
                        currentMode = uiState.currentMode,
                        loadPlates = uiState.loadPlates,
                        unloadPlates = uiState.unloadPlates,
                        stockCheckCount = uiState.stockCheckCount,
                        housekeepingCount = uiState.housekeepingCount,
                        isCameraScanning = uiState.isCameraScanning,
                        manualInput = uiState.manualPlateInput,
                        plateToDelete = uiState.plateToDelete,
                        onModeSelect = { viewModel.selectMode(it) },
                        onSetCameraScanning = { viewModel.setCameraScanning(it) },
                        onManualInputChange = { viewModel.updateManualPlateInput(it) },
                        onAddPlateScan = { viewModel.addPlateScan(it) },
                        onAddMultiplePlates = { viewModel.addMultiplePlates(it) },
                        onDecrementStockCheck = { viewModel.decrementStockCheck() },
                        onDecrementHousekeeping = { viewModel.decrementHousekeeping() },
                        onPromptDeletePlate = { viewModel.promptDeletePlate(it) },
                        onConfirmDeletePlate = { viewModel.confirmDeletePlate() }
                    )
                }
                2 -> {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    TabTallyPrintScreen(
                        loadCount = uiState.loadCount,
                        unloadCount = uiState.unloadCount,
                        stockCheckCount = uiState.stockCheckCount,
                        housekeepingCount = uiState.housekeepingCount,
                        checklist = uiState.checklistAnswers,
                        showResetConfirmation = uiState.showResetConfirmation,
                        onChecklistChange = { viewModel.updateChecklist(it) },
                        onPromptReset = { viewModel.promptResetShift(it) },
                        onConfirmReset = { viewModel.confirmResetShift() },
                        onPrintClick = { viewModel.triggerPrint(context) },
                        onSharePdfClick = { viewModel.triggerSharePdf(context) }
                    )
                }
            }
        }
    }

    if (uiState.isAboutOpen) {
        AboutDialog(
            onDismiss = { viewModel.openAbout(false) }
        )
    }
}
