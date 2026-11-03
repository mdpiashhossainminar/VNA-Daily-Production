package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.data.VnaRepository
import com.example.model.ChecklistAnswers
import com.example.model.OperationMode
import com.example.model.PlateScan
import com.example.model.ShiftInfo
import com.example.model.ShiftType
import com.example.model.VnaUiState
import com.example.print.VnaPrintHelper
import com.example.util.FeedbackUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class VnaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = VnaRepository(application)

    private val _uiState = MutableStateFlow(
        VnaUiState(
            shiftInfo = repository.loadShiftInfo(),
            checklistAnswers = repository.loadChecklist(),
            currentMode = repository.loadCurrentMode(),
            loadPlates = repository.loadLoadPlates(),
            unloadPlates = repository.loadUnloadPlates(),
            stockCheckCounter = repository.loadStockCheckCounter(),
            housekeepingCounter = repository.loadHousekeepingCounter()
        )
    )
    val uiState: StateFlow<VnaUiState> = _uiState.asStateFlow()

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun openAbout(open: Boolean) {
        _uiState.update { it.copy(isAboutOpen = open) }
    }

    // ---------------- TAB 1: শিফট ও চেকলিস্ট ----------------
    fun updateOperatorName(name: String) {
        _uiState.update {
            val updated = it.shiftInfo.copy(operatorName = name)
            repository.saveShiftInfo(updated)
            it.copy(shiftInfo = updated)
        }
    }

    fun updateDesignation(desig: String) {
        _uiState.update {
            val updated = it.shiftInfo.copy(designation = desig)
            repository.saveShiftInfo(updated)
            it.copy(shiftInfo = updated)
        }
    }

    fun updateIdNumber(id: String) {
        _uiState.update {
            val updated = it.shiftInfo.copy(idNumber = id)
            repository.saveShiftInfo(updated)
            it.copy(shiftInfo = updated)
        }
    }

    fun updateSupervisor(sup: String) {
        _uiState.update {
            val updated = it.shiftInfo.copy(shiftSupervisor = sup)
            repository.saveShiftInfo(updated)
            it.copy(shiftInfo = updated)
        }
    }

    fun updateDate(date: String) {
        _uiState.update {
            val updated = it.shiftInfo.copy(dateString = date)
            repository.saveShiftInfo(updated)
            it.copy(shiftInfo = updated)
        }
    }

    fun updateShiftType(shift: ShiftType) {
        _uiState.update {
            val updated = it.shiftInfo.copy(shift = shift)
            repository.saveShiftInfo(updated)
            it.copy(shiftInfo = updated)
        }
    }

    fun updateChecklist(transform: (ChecklistAnswers) -> ChecklistAnswers) {
        _uiState.update {
            val updated = transform(it.checklistAnswers)
            repository.saveChecklist(updated)
            it.copy(checklistAnswers = updated)
        }
    }

    // ---------------- TAB 2: স্ক্যান ----------------
    fun selectMode(mode: OperationMode) {
        repository.saveCurrentMode(mode)
        _uiState.update { it.copy(currentMode = mode) }
    }

    fun setCameraScanning(active: Boolean) {
        _uiState.update { it.copy(isCameraScanning = active) }
    }

    fun updateManualPlateInput(text: String) {
        // Strip non-digit characters while typing (Number-only requirement)
        val filtered = text.filter { it.isDigit() }
        _uiState.update { it.copy(manualPlateInput = filtered) }
    }

    fun addPlateScan(boxNumber: String) {
        val cleanNumber = boxNumber.trim().filter { it.isDigit() }
        val mode = _uiState.value.currentMode

        FeedbackUtil.triggerScanFeedback(getApplication())

        when (mode) {
            OperationMode.LOAD -> {
                if (cleanNumber.isBlank()) return
                val newScan = PlateScan(boxNumber = cleanNumber, mode = OperationMode.LOAD)
                _uiState.update {
                    val updated = it.loadPlates + newScan
                    repository.saveLoadPlates(updated)
                    it.copy(loadPlates = updated, manualPlateInput = "")
                }
            }
            OperationMode.UNLOAD -> {
                if (cleanNumber.isBlank()) return
                val newScan = PlateScan(boxNumber = cleanNumber, mode = OperationMode.UNLOAD)
                _uiState.update {
                    val updated = it.unloadPlates + newScan
                    repository.saveUnloadPlates(updated)
                    it.copy(unloadPlates = updated, manualPlateInput = "")
                }
            }
            OperationMode.STOCK_CHECK -> {
                // Stock Check physically: only adds +1 to counter, does NOT store box numbers
                _uiState.update {
                    val newCount = it.stockCheckCounter + 1
                    repository.saveStockCheckCounter(newCount)
                    it.copy(stockCheckCounter = newCount, manualPlateInput = "")
                }
            }
            OperationMode.HOUSEKEEPING -> {
                // Housekeeping / Segregation: only adds +1 to counter, does NOT store box numbers
                _uiState.update {
                    val newCount = it.housekeepingCounter + 1
                    repository.saveHousekeepingCounter(newCount)
                    it.copy(housekeepingCounter = newCount, manualPlateInput = "")
                }
            }
        }
    }

    fun addMultiplePlates(boxNumbers: List<String>) {
        if (boxNumbers.isEmpty()) return
        FeedbackUtil.triggerScanFeedback(getApplication())
        val mode = _uiState.value.currentMode

        when (mode) {
            OperationMode.LOAD -> {
                val newScans = boxNumbers.map { PlateScan(boxNumber = it.trim().filter { c -> c.isDigit() }, mode = OperationMode.LOAD) }
                    .filter { it.boxNumber.isNotBlank() }
                _uiState.update {
                    val updated = it.loadPlates + newScans
                    repository.saveLoadPlates(updated)
                    it.copy(loadPlates = updated)
                }
            }
            OperationMode.UNLOAD -> {
                val newScans = boxNumbers.map { PlateScan(boxNumber = it.trim().filter { c -> c.isDigit() }, mode = OperationMode.UNLOAD) }
                    .filter { it.boxNumber.isNotBlank() }
                _uiState.update {
                    val updated = it.unloadPlates + newScans
                    repository.saveUnloadPlates(updated)
                    it.copy(unloadPlates = updated)
                }
            }
            OperationMode.STOCK_CHECK -> {
                _uiState.update {
                    val newCount = it.stockCheckCounter + boxNumbers.size
                    repository.saveStockCheckCounter(newCount)
                    it.copy(stockCheckCounter = newCount)
                }
            }
            OperationMode.HOUSEKEEPING -> {
                _uiState.update {
                    val newCount = it.housekeepingCounter + boxNumbers.size
                    repository.saveHousekeepingCounter(newCount)
                    it.copy(housekeepingCounter = newCount)
                }
            }
        }
    }

    fun decrementStockCheck() {
        _uiState.update {
            val newCount = (it.stockCheckCounter - 1).coerceAtLeast(0)
            repository.saveStockCheckCounter(newCount)
            it.copy(stockCheckCounter = newCount)
        }
    }

    fun decrementHousekeeping() {
        _uiState.update {
            val newCount = (it.housekeepingCounter - 1).coerceAtLeast(0)
            repository.saveHousekeepingCounter(newCount)
            it.copy(housekeepingCounter = newCount)
        }
    }

    fun promptDeletePlate(plate: PlateScan?) {
        _uiState.update { it.copy(plateToDelete = plate) }
    }

    fun confirmDeletePlate() {
        val plate = _uiState.value.plateToDelete ?: return
        _uiState.update { state ->
            if (plate.mode == OperationMode.LOAD) {
                val updated = state.loadPlates.filterNot { it.id == plate.id }
                repository.saveLoadPlates(updated)
                state.copy(loadPlates = updated, plateToDelete = null)
            } else {
                val updated = state.unloadPlates.filterNot { it.id == plate.id }
                repository.saveUnloadPlates(updated)
                state.copy(unloadPlates = updated, plateToDelete = null)
            }
        }
    }

    // ---------------- TAB 3: হিসাব ও প্রিন্ট ----------------
    fun promptResetShift(show: Boolean) {
        _uiState.update { it.copy(showResetConfirmation = show) }
    }

    fun confirmResetShift() {
        repository.resetShiftData(preserveOperatorDetails = true)
        _uiState.update {
            it.copy(
                loadPlates = emptyList(),
                unloadPlates = emptyList(),
                stockCheckCounter = 0,
                housekeepingCounter = 0,
                checklistAnswers = repository.loadChecklist(),
                showResetConfirmation = false
            )
        }
    }

    fun triggerPrint(context: Context) {
        val state = _uiState.value
        VnaPrintHelper.printWorksheet(
            context = context,
            shiftInfo = state.shiftInfo,
            checklist = state.checklistAnswers,
            loadPlates = state.loadPlates,
            unloadPlates = state.unloadPlates,
            stockCheckCount = state.stockCheckCounter,
            housekeepingCount = state.housekeepingCounter
        )
    }

    fun triggerSharePdf(context: Context) {
        val state = _uiState.value
        VnaPrintHelper.sharePdf(
            context = context,
            shiftInfo = state.shiftInfo,
            checklist = state.checklistAnswers,
            loadPlates = state.loadPlates,
            unloadPlates = state.unloadPlates,
            stockCheckCount = state.stockCheckCounter,
            housekeepingCount = state.housekeepingCounter
        )
    }
}
