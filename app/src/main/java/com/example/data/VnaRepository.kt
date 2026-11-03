package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ChecklistAnswers
import com.example.model.OperationMode
import com.example.model.PlateScan
import com.example.model.ShiftInfo
import com.example.model.ShiftType
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VnaRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("vna_daily_production_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_OPERATOR_NAME = "key_operator_name"
        private const val KEY_DESIGNATION = "key_designation"
        private const val KEY_ID_NUMBER = "key_id_number"
        private const val KEY_SUPERVISOR = "key_supervisor"
        private const val KEY_DATE = "key_date"
        private const val KEY_SHIFT = "key_shift"

        private const val KEY_CHK_1 = "key_chk_1"
        private const val KEY_CHK_2 = "key_chk_2"
        private const val KEY_CHK_3 = "key_chk_3"
        private const val KEY_CHK_4 = "key_chk_4"
        private const val KEY_CHK_5 = "key_chk_5"
        private const val KEY_CHK_6 = "key_chk_6"
        private const val KEY_CHK_7 = "key_chk_7"
        private const val KEY_CHK_8 = "key_chk_8"
        private const val KEY_CHK_9 = "key_chk_9"
        private const val KEY_CHK_10 = "key_chk_10"
        private const val KEY_CHK_11 = "key_chk_11"
        private const val KEY_CHK_12 = "key_chk_12"
        private const val KEY_CHK_13 = "key_chk_13"
        private const val KEY_CHK_14 = "key_chk_14"
        private const val KEY_REASON = "key_reason"

        private const val KEY_LOAD_PLATES = "key_load_plates"
        private const val KEY_UNLOAD_PLATES = "key_unload_plates"
        private const val KEY_STOCK_CHECK_COUNTER = "key_stock_check_counter"
        private const val KEY_HOUSEKEEPING_COUNTER = "key_housekeeping_counter"
        private const val KEY_CURRENT_MODE = "key_current_mode"
    }

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        return sdf.format(Date())
    }

    fun loadShiftInfo(): ShiftInfo {
        val savedDate = prefs.getString(KEY_DATE, "") ?: ""
        val effectiveDate = if (savedDate.isBlank()) getTodayDateString() else savedDate
        val shiftCode = prefs.getString(KEY_SHIFT, "A") ?: "A"
        val shiftType = ShiftType.values().find { it.code == shiftCode } ?: ShiftType.A

        return ShiftInfo(
            operatorName = prefs.getString(KEY_OPERATOR_NAME, "") ?: "",
            designation = prefs.getString(KEY_DESIGNATION, "") ?: "",
            idNumber = prefs.getString(KEY_ID_NUMBER, "") ?: "",
            shiftSupervisor = prefs.getString(KEY_SUPERVISOR, "") ?: "",
            dateString = effectiveDate,
            shift = shiftType
        )
    }

    fun saveShiftInfo(info: ShiftInfo) {
        prefs.edit()
            .putString(KEY_OPERATOR_NAME, info.operatorName)
            .putString(KEY_DESIGNATION, info.designation)
            .putString(KEY_ID_NUMBER, info.idNumber)
            .putString(KEY_SUPERVISOR, info.shiftSupervisor)
            .putString(KEY_DATE, info.dateString)
            .putString(KEY_SHIFT, info.shift.code)
            .apply()
    }

    fun loadChecklist(): ChecklistAnswers {
        return ChecklistAnswers(
            chk1PreInspectOk = prefs.getBoolean(KEY_CHK_1, true),
            chk2BatteryStatusOk = prefs.getBoolean(KEY_CHK_2, true),
            chk3TyreBrakeLightOk = prefs.getBoolean(KEY_CHK_3, true),
            chk4SafetyAlarmOk = prefs.getBoolean(KEY_CHK_4, true),
            chk5AisleClear = prefs.getBoolean(KEY_CHK_5, true),
            chk6PreLoadRackVerified = prefs.getBoolean(KEY_CHK_6, true),
            chk7NoDamageLoadCompleted = prefs.getBoolean(KEY_CHK_7, true),
            chk8PreUnloadRackVerified = prefs.getBoolean(KEY_CHK_8, true),
            chk9WrongLocationYarnQty = prefs.getString(KEY_CHK_9, "") ?: "",
            chk10DamagedPlateQty = prefs.getString(KEY_CHK_10, "") ?: "",
            chk11EmptyLocationUpdated = prefs.getBoolean(KEY_CHK_11, true),
            chk12DowntimeMinutes = prefs.getString(KEY_CHK_12, "") ?: "",
            chk13AccidentReported = prefs.getBoolean(KEY_CHK_13, false),
            chk14PostShiftParked = prefs.getBoolean(KEY_CHK_14, true),
            reasonNotAchievingTarget = prefs.getString(KEY_REASON, "") ?: ""
        )
    }

    fun saveChecklist(chk: ChecklistAnswers) {
        prefs.edit()
            .putBoolean(KEY_CHK_1, chk.chk1PreInspectOk)
            .putBoolean(KEY_CHK_2, chk.chk2BatteryStatusOk)
            .putBoolean(KEY_CHK_3, chk.chk3TyreBrakeLightOk)
            .putBoolean(KEY_CHK_4, chk.chk4SafetyAlarmOk)
            .putBoolean(KEY_CHK_5, chk.chk5AisleClear)
            .putBoolean(KEY_CHK_6, chk.chk6PreLoadRackVerified)
            .putBoolean(KEY_CHK_7, chk.chk7NoDamageLoadCompleted)
            .putBoolean(KEY_CHK_8, chk.chk8PreUnloadRackVerified)
            .putString(KEY_CHK_9, chk.chk9WrongLocationYarnQty)
            .putString(KEY_CHK_10, chk.chk10DamagedPlateQty)
            .putBoolean(KEY_CHK_11, chk.chk11EmptyLocationUpdated)
            .putString(KEY_CHK_12, chk.chk12DowntimeMinutes)
            .putBoolean(KEY_CHK_13, chk.chk13AccidentReported)
            .putBoolean(KEY_CHK_14, chk.chk14PostShiftParked)
            .putString(KEY_REASON, chk.reasonNotAchievingTarget)
            .apply()
    }

    private fun loadPlateList(key: String, mode: OperationMode): List<PlateScan> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        val list = mutableListOf<PlateScan>()
        try {
            val jsonArray = JSONArray(raw)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optString("id", "")
                val box = obj.optString("box", "")
                val time = obj.optLong("time", System.currentTimeMillis())
                if (box.isNotBlank()) {
                    list.add(PlateScan(id = id, boxNumber = box, mode = mode, timestamp = time))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun savePlateList(key: String, plates: List<PlateScan>) {
        val jsonArray = JSONArray()
        for (scan in plates) {
            val obj = JSONObject()
            obj.put("id", scan.id)
            obj.put("box", scan.boxNumber)
            obj.put("time", scan.timestamp)
            jsonArray.put(obj)
        }
        prefs.edit().putString(key, jsonArray.toString()).apply()
    }

    fun loadLoadPlates(): List<PlateScan> = loadPlateList(KEY_LOAD_PLATES, OperationMode.LOAD)
    fun saveLoadPlates(plates: List<PlateScan>) = savePlateList(KEY_LOAD_PLATES, plates)

    fun loadUnloadPlates(): List<PlateScan> = loadPlateList(KEY_UNLOAD_PLATES, OperationMode.UNLOAD)
    fun saveUnloadPlates(plates: List<PlateScan>) = savePlateList(KEY_UNLOAD_PLATES, plates)

    fun loadStockCheckCounter(): Int = prefs.getInt(KEY_STOCK_CHECK_COUNTER, 0)
    fun saveStockCheckCounter(count: Int) = prefs.edit().putInt(KEY_STOCK_CHECK_COUNTER, count.coerceAtLeast(0)).apply()

    fun loadHousekeepingCounter(): Int = prefs.getInt(KEY_HOUSEKEEPING_COUNTER, 0)
    fun saveHousekeepingCounter(count: Int) = prefs.edit().putInt(KEY_HOUSEKEEPING_COUNTER, count.coerceAtLeast(0)).apply()

    fun loadCurrentMode(): OperationMode {
        val name = prefs.getString(KEY_CURRENT_MODE, OperationMode.LOAD.name)
        return try {
            OperationMode.valueOf(name ?: OperationMode.LOAD.name)
        } catch (_: Exception) {
            OperationMode.LOAD
        }
    }

    fun saveCurrentMode(mode: OperationMode) {
        prefs.edit().putString(KEY_CURRENT_MODE, mode.name).apply()
    }

    fun resetShiftData(preserveOperatorDetails: Boolean = true) {
        val editor = prefs.edit()
        editor.remove(KEY_LOAD_PLATES)
        editor.remove(KEY_UNLOAD_PLATES)
        editor.remove(KEY_STOCK_CHECK_COUNTER)
        editor.remove(KEY_HOUSEKEEPING_COUNTER)
        editor.remove(KEY_REASON)
        editor.remove(KEY_CHK_9)
        editor.remove(KEY_CHK_10)
        editor.remove(KEY_CHK_12)
        // Reset checklist to defaults
        editor.putBoolean(KEY_CHK_1, true)
        editor.putBoolean(KEY_CHK_2, true)
        editor.putBoolean(KEY_CHK_3, true)
        editor.putBoolean(KEY_CHK_4, true)
        editor.putBoolean(KEY_CHK_5, true)
        editor.putBoolean(KEY_CHK_6, true)
        editor.putBoolean(KEY_CHK_7, true)
        editor.putBoolean(KEY_CHK_8, true)
        editor.putBoolean(KEY_CHK_11, true)
        editor.putBoolean(KEY_CHK_13, false)
        editor.putBoolean(KEY_CHK_14, true)

        if (!preserveOperatorDetails) {
            editor.remove(KEY_OPERATOR_NAME)
            editor.remove(KEY_DESIGNATION)
            editor.remove(KEY_ID_NUMBER)
            editor.remove(KEY_SUPERVISOR)
        }
        editor.putString(KEY_DATE, getTodayDateString())
        editor.apply()
    }
}
