package com.example.model

import java.util.UUID

enum class ShiftType(val code: String, val displayName: String) {
    A("A", "A (Morning)"),
    B("B", "B (Evening)"),
    C("C", "C (Night)"),
    G("G", "G (General)")
}

enum class OperationMode(val banglaTitle: String, val englishTitle: String, val target: Int) {
    LOAD("লোড", "Plate Load", 85),
    UNLOAD("আনলোড", "Plate Unload", 85),
    STOCK_CHECK("স্টক চেক", "Stock Check Physically", 10),
    HOUSEKEEPING("হাউসকিপিং", "Housekeeping / Segregation", 10)
}

data class ShiftInfo(
    val operatorName: String = "",
    val designation: String = "",
    val idNumber: String = "",
    val shiftSupervisor: String = "",
    val dateString: String = "",
    val shift: ShiftType = ShiftType.A
)

data class PlateScan(
    val id: String = UUID.randomUUID().toString(),
    val boxNumber: String,
    val mode: OperationMode,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChecklistAnswers(
    val chk1PreInspectOk: Boolean = true,
    val chk2BatteryStatusOk: Boolean = true,
    val chk3TyreBrakeLightOk: Boolean = true,
    val chk4SafetyAlarmOk: Boolean = true,
    val chk5AisleClear: Boolean = true,
    val chk6PreLoadRackVerified: Boolean = true,
    val chk7NoDamageLoadCompleted: Boolean = true,
    val chk8PreUnloadRackVerified: Boolean = true,
    val chk9WrongLocationYarnQty: String = "",
    val chk10DamagedPlateQty: String = "",
    val chk11EmptyLocationUpdated: Boolean = true,
    val chk12DowntimeMinutes: String = "",
    val chk13AccidentReported: Boolean = false, // false = "No" (safe), true = "Yes" (accident)
    val chk14PostShiftParked: Boolean = true,
    val reasonNotAchievingTarget: String = ""
)

data class VnaUiState(
    val shiftInfo: ShiftInfo = ShiftInfo(),
    val checklistAnswers: ChecklistAnswers = ChecklistAnswers(),
    val currentMode: OperationMode = OperationMode.LOAD,
    val loadPlates: List<PlateScan> = emptyList(),
    val unloadPlates: List<PlateScan> = emptyList(),
    val stockCheckCounter: Int = 0,
    val housekeepingCounter: Int = 0,
    val selectedTab: Int = 0, // 0 = শুরু, 1 = স্ক্যান, 2 = হিসাব ও প্রিন্ট
    val isAboutOpen: Boolean = false,
    val isCameraScanning: Boolean = false,
    val manualPlateInput: String = "",
    val plateToDelete: PlateScan? = null,
    val showResetConfirmation: Boolean = false,
    val scanOverlayMessage: String? = null
) {
    val loadCount: Int get() = loadPlates.size
    val unloadCount: Int get() = unloadPlates.size
    val stockCheckCount: Int get() = stockCheckCounter
    val housekeepingCount: Int get() = housekeepingCounter
    val totalAchieved: Int get() = loadCount + unloadCount + stockCheckCount + housekeepingCount
    val totalTarget: Int = 190
}
