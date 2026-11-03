package com.example.print

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.model.ChecklistAnswers
import com.example.model.PlateScan
import com.example.model.ShiftInfo
import java.io.File
import java.io.FileOutputStream

object VnaPdfRenderer {
    // A4 dimensions in PostScript points: 595 x 842 pt
    const val PAGE_WIDTH = 595
    const val PAGE_HEIGHT = 842

    fun calculateTotalPages(unloadCount: Int, loadCount: Int): Int {
        val maxEntries = maxOf(unloadCount, loadCount)
        val boxPages = if (maxEntries <= 30) 1 else ((maxEntries - 1) / 30) + 1
        return boxPages + 1 // +1 for the final summary page
    }

    fun generatePdf(
        context: Context,
        shiftInfo: ShiftInfo,
        checklist: ChecklistAnswers,
        loadPlates: List<PlateScan>,
        unloadPlates: List<PlateScan>,
        stockCheckCount: Int,
        housekeepingCount: Int,
        outputFile: File
    ) {
        val pdfDocument = PdfDocument()

        val leftPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 7.5f
        }

        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }

        val boldPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val headerFillPaint = Paint().apply {
            color = Color.rgb(240, 240, 240)
            style = Paint.Style.FILL
        }

        val checkPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 7f
        }

        val maxEntries = maxOf(unloadPlates.size, loadPlates.size)
        val boxPages = if (maxEntries <= 30) 1 else ((maxEntries - 1) / 30) + 1

        var currentPageNumber = 1

        // ---------------- PAGE 1: Master Sheet Replica ----------------
        val pageInfo1 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
        val page1 = pdfDocument.startPage(pageInfo1)
        drawPage1(
            canvas = page1.canvas,
            shiftInfo = shiftInfo,
            chk = checklist,
            unloadList = unloadPlates.take(30),
            loadList = loadPlates.take(30),
            loadCount = loadPlates.size,
            unloadCount = unloadPlates.size,
            stockCheckCount = stockCheckCount,
            housekeepingCount = housekeepingCount,
            border = borderPaint,
            text = leftPaint,
            bold = boldPaint,
            title = titlePaint,
            fill = headerFillPaint,
            check = checkPaint
        )
        pdfDocument.finishPage(page1)

        // ---------------- Continuation Pages (Rows 31-60, 61-90, etc.) ----------------
        for (pageIdx in 2..boxPages) {
            currentPageNumber++
            val pageOffset = (pageIdx - 1) * 30
            val unloadChunk = unloadPlates.drop(pageOffset).take(30)
            val loadChunk = loadPlates.drop(pageOffset).take(30)

            val contPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
            val contPage = pdfDocument.startPage(contPageInfo)
            drawContinuationBoxPage(
                canvas = contPage.canvas,
                startSerial = pageOffset + 1,
                unloadChunk = unloadChunk,
                loadChunk = loadChunk,
                border = borderPaint,
                bold = boldPaint,
                title = titlePaint,
                fill = headerFillPaint
            )
            pdfDocument.finishPage(contPage)
        }

        // ---------------- FINAL PAGE: Summary Totals Only ----------------
        currentPageNumber++
        val summaryPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
        val summaryPage = pdfDocument.startPage(summaryPageInfo)
        drawFinalSummaryPage(
            canvas = summaryPage.canvas,
            shiftInfo = shiftInfo,
            chk = checklist,
            loadCount = loadPlates.size,
            unloadCount = unloadPlates.size,
            stockCheckCount = stockCheckCount,
            housekeepingCount = housekeepingCount,
            border = borderPaint,
            text = leftPaint,
            bold = boldPaint,
            title = titlePaint,
            fill = headerFillPaint
        )
        pdfDocument.finishPage(summaryPage)

        val fos = FileOutputStream(outputFile)
        pdfDocument.writeTo(fos)
        fos.flush()
        fos.close()
        pdfDocument.close()
    }

    private fun drawPage1(
        canvas: Canvas,
        shiftInfo: ShiftInfo,
        chk: ChecklistAnswers,
        unloadList: List<PlateScan>,
        loadList: List<PlateScan>,
        loadCount: Int,
        unloadCount: Int,
        stockCheckCount: Int,
        housekeepingCount: Int,
        border: Paint,
        text: Paint,
        bold: Paint,
        title: Paint,
        fill: Paint,
        check: Paint
    ) {
        val margin = 20f
        val colGap = 12f
        val leftColWidth = 270f
        val rightColWidth = PAGE_WIDTH - margin * 2 - leftColWidth - colGap
        val rightColX = margin + leftColWidth + colGap

        var currentY = margin

        // 1. Operator Info Block
        val infoHeight = 32f
        canvas.drawRect(margin, currentY, margin + leftColWidth, currentY + infoHeight, border)
        canvas.drawText("Operator: ${shiftInfo.operatorName.ifBlank { "—" }}", margin + 4f, currentY + 11f, bold)
        canvas.drawText("ID: ${shiftInfo.idNumber.ifBlank { "—" }}", margin + 170f, currentY + 11f, text)
        canvas.drawLine(margin, currentY + 16f, margin + leftColWidth, currentY + 16f, border)
        canvas.drawText("Designation: ${shiftInfo.designation.ifBlank { "Operator" }}", margin + 4f, currentY + 27f, text)
        canvas.drawText("Shift: ${shiftInfo.shift.code}", margin + 170f, currentY + 27f, bold)

        currentY += infoHeight + 6f

        // 2. Checklist Table
        val titleHeight = 16f
        canvas.drawRect(margin, currentY, margin + leftColWidth, currentY + titleHeight, fill)
        canvas.drawRect(margin, currentY, margin + leftColWidth, currentY + titleHeight, border)
        canvas.drawText("Daily VNA Operator Work Status / Checklist", margin + 18f, currentY + 11f, title)
        currentY += titleHeight

        val chkHeaderHeight = 15f
        val slColW = 20f
        val activityColW = 165f

        canvas.drawRect(margin, currentY, margin + leftColWidth, currentY + chkHeaderHeight, fill)
        canvas.drawRect(margin, currentY, margin + leftColWidth, currentY + chkHeaderHeight, border)
        canvas.drawLine(margin + slColW, currentY, margin + slColW, currentY + chkHeaderHeight, border)
        canvas.drawLine(margin + slColW + activityColW, currentY, margin + slColW + activityColW, currentY + chkHeaderHeight, border)

        canvas.drawText("SL", margin + 4f, currentY + 10.5f, bold)
        canvas.drawText("Activity / Check Point", margin + slColW + 6f, currentY + 10.5f, bold)
        canvas.drawText("Daily Status", margin + slColW + activityColW + 6f, currentY + 10.5f, bold)
        currentY += chkHeaderHeight

        val checklistData = listOf(
            Triple(1, "VNA মেশিন চালুর পূর্ববর্তী পরিদর্শন সম্পন্ন\nকরা হয়েছে", if (chk.chk1PreInspectOk) "☑ OK   ☐ Not OK" else "☐ OK   ☑ Not OK"),
            Triple(2, "ব্যাটারী ও চার্জিং স্ট্যাটাস চেক করা হয়েছে", if (chk.chk2BatteryStatusOk) "☑ OK   ☐ Not OK" else "☐ OK   ☑ Not OK"),
            Triple(3, "টায়ার, ব্রেক, হর্ন ও লাইট পরীক্ষা করা হয়েছে", if (chk.chk3TyreBrakeLightOk) "☑ OK   ☐ Not OK" else "☐ OK   ☑ Not OK"),
            Triple(4, "সেফটি অ্যালার্ম / ইমার্জেন্সি সিস্টেম\nপরীক্ষা করা হয়েছে", if (chk.chk4SafetyAlarmOk) "☑ OK   ☐ Not OK" else "☐ OK   ☑ Not OK"),
            Triple(5, "VNA Ailes / Rack এলাকায় কোনো বাধা\nআছে কি না পরীক্ষা করা হয়েছে", if (chk.chk5AisleClear) "☑ Clear  ☐ Issue" else "☐ Clear  ☑ Issue"),
            Triple(6, "লোডিংয়ের আগে Rack/ Box লোকেশন\nযাচাই করা হয়েছে", if (chk.chk6PreLoadRackVerified) "☑ Yes   ☐ No" else "☐ Yes   ☑ No"),
            Triple(7, "কোনো ক্ষতি ছাড়াই Rack/Box লোডিং\nসম্পন্ন করা হয়েছে", if (chk.chk7NoDamageLoadCompleted) "☑ Yes   ☐ No" else "☐ Yes   ☑ No"),
            Triple(8, "আনলোডিংয়ের আগে Rack/Box লোকেশন\nযাচাই করা হয়েছে", if (chk.chk8PreUnloadRackVerified) "☑ Yes   ☐ No" else "☐ Yes   ☑ No"),
            Triple(9, "ভুল লোকেশনে রাখা / ভুল স্থানে থাকা ইয়ার্ন\nশনাক্ত করা হয়েছে", "Qty: ${chk.chk9WrongLocationYarnQty.ifBlank { "____" }}"),
            Triple(10, "ক্ষতিগ্রস্ত প্যালেট/প্লেট শনাক্ত করা হয়েছে", "Qty: ${chk.chk10DamagedPlateQty.ifBlank { "____" }}"),
            Triple(11, "খালি লোকেশনের তথ্য আপডেট করা\nহয়েছে", if (chk.chk11EmptyLocationUpdated) "☑ Done  ☐ Pending" else "☐ Done  ☑ Pending"),
            Triple(12, "VNA-এর ডাউনটাইম, ব্রেকডাউন বা\nকারিগরি সমস্যা নথিভুক্ত করা হয়েছে", "${chk.chk12DowntimeMinutes.ifBlank { "____" }} min"),
            Triple(13, "দুর্ঘটনা / নিকট-দুর্ঘটনা / নিরাপত্তাজনিত\nসমস্যা রিপোর্ট করা হয়েছে", if (!chk.chk13AccidentReported) "☑ No    ☐ Yes" else "☐ No    ☑ Yes"),
            Triple(14, "শিফট শেষে VNA-এর অবস্থা পরীক্ষা করে\nসঠিকভাবে পার্ক করা হয়েছে", if (chk.chk14PostShiftParked) "☑ Done  ☐ Pending" else "☐ Done  ☑ Pending")
        )

        for (item in checklistData) {
            val isTwoLine = item.second.contains("\n")
            val rowH = if (isTwoLine) 24f else 17f

            canvas.drawRect(margin, currentY, margin + leftColWidth, currentY + rowH, border)
            canvas.drawLine(margin + slColW, currentY, margin + slColW, currentY + rowH, border)
            canvas.drawLine(margin + slColW + activityColW, currentY, margin + slColW + activityColW, currentY + rowH, border)

            canvas.drawText("${item.first}", margin + 6f, currentY + 12f, bold)
            if (isTwoLine) {
                val lines = item.second.split("\n")
                canvas.drawText(lines[0], margin + slColW + 4f, currentY + 9.5f, text)
                canvas.drawText(lines[1], margin + slColW + 4f, currentY + 19.5f, text)
            } else {
                canvas.drawText(item.second, margin + slColW + 4f, currentY + 11.5f, text)
            }
            canvas.drawText(item.third, margin + slColW + activityColW + 4f, currentY + (if (isTwoLine) 14f else 11.5f), check)
            currentY += rowH
        }

        currentY += 6f

        // 3. Category Target / Achieved Table
        val catHeaderH = 15f
        val catSlW = 20f
        val catNameW = 145f
        val catTargetW = 50f

        canvas.drawRect(margin, currentY, margin + leftColWidth, currentY + catHeaderH, fill)
        canvas.drawRect(margin, currentY, margin + leftColWidth, currentY + catHeaderH, border)
        canvas.drawLine(margin + catSlW, currentY, margin + catSlW, currentY + catHeaderH, border)
        canvas.drawLine(margin + catSlW + catNameW, currentY, margin + catSlW + catNameW, currentY + catHeaderH, border)
        canvas.drawLine(margin + catSlW + catNameW + catTargetW, currentY, margin + catSlW + catNameW + catTargetW, currentY + catHeaderH, border)

        canvas.drawText("SL", margin + 4f, currentY + 10.5f, bold)
        canvas.drawText("Category", margin + catSlW + 6f, currentY + 10.5f, bold)
        canvas.drawText("Target", margin + catSlW + catNameW + 8f, currentY + 10.5f, bold)
        canvas.drawText("Achieved", margin + catSlW + catNameW + catTargetW + 6f, currentY + 10.5f, bold)
        currentY += catHeaderH

        val catRows = listOf(
            Triple(1, "Plate Load", Pair(85, loadCount)),
            Triple(2, "Plate Unload", Pair(85, unloadCount)),
            Triple(3, "Stock Check Physically", Pair(10, stockCheckCount)),
            Triple(4, "Housekeeping / Segregation", Pair(10, housekeepingCount))
        )

        val catRowH = 16f
        for (cat in catRows) {
            canvas.drawRect(margin, currentY, margin + leftColWidth, currentY + catRowH, border)
            canvas.drawLine(margin + catSlW, currentY, margin + catSlW, currentY + catRowH, border)
            canvas.drawLine(margin + catSlW + catNameW, currentY, margin + catSlW + catNameW, currentY + catRowH, border)
            canvas.drawLine(margin + catSlW + catNameW + catTargetW, currentY, margin + catSlW + catNameW + catTargetW, currentY + catRowH, border)

            canvas.drawText("${cat.first}", margin + 6f, currentY + 11.5f, bold)
            canvas.drawText(cat.second, margin + catSlW + 6f, currentY + 11.5f, text)
            canvas.drawText("${cat.third.first}", margin + catSlW + catNameW + 14f, currentY + 11.5f, bold)
            val ach = cat.third.second
            canvas.drawText(if (ach > 0) "$ach" else "", margin + catSlW + catNameW + catTargetW + 16f, currentY + 11.5f, bold)

            currentY += catRowH
        }

        // Total Row
        canvas.drawRect(margin, currentY, margin + leftColWidth, currentY + catRowH, fill)
        canvas.drawRect(margin, currentY, margin + leftColWidth, currentY + catRowH, border)
        canvas.drawLine(margin + catSlW + catNameW, currentY, margin + catSlW + catNameW, currentY + catRowH, border)
        canvas.drawLine(margin + catSlW + catNameW + catTargetW, currentY, margin + catSlW + catNameW + catTargetW, currentY + catRowH, border)

        canvas.drawText("Total", margin + 60f, currentY + 11.5f, bold)
        canvas.drawText("190", margin + catSlW + catNameW + 12f, currentY + 11.5f, bold)
        val grandAch = loadCount + unloadCount + stockCheckCount + housekeepingCount
        canvas.drawText(if (grandAch > 0) "$grandAch" else "", margin + catSlW + catNameW + catTargetW + 16f, currentY + 11.5f, bold)

        currentY += catRowH + 6f

        // 4. Reason For not Achieving Target Box
        val reasonHeaderH = 15f
        val reasonBodyH = 46f
        canvas.drawRect(margin, currentY, margin + leftColWidth, currentY + reasonHeaderH, fill)
        canvas.drawRect(margin, currentY, margin + leftColWidth, currentY + reasonHeaderH + reasonBodyH, border)
        canvas.drawText("Reason For not Achieving Target (টার্গেট অর্জন না করার কারণ)", margin + 6f, currentY + 10.5f, bold)
        currentY += reasonHeaderH

        if (chk.reasonNotAchievingTarget.isNotBlank()) {
            val words = chk.reasonNotAchievingTarget.chunked(38)
            var reasonLineY = currentY + 12f
            for (line in words.take(3)) {
                canvas.drawText(line, margin + 6f, reasonLineY, text)
                reasonLineY += 12f
            }
        }
        currentY += reasonBodyH + 6f

        // 5. Shift Supervisor Block
        val supRowH = 16f
        val supLabelW = 100f
        val supTableH = supRowH * 3

        canvas.drawRect(margin, currentY, margin + leftColWidth, currentY + supTableH, border)
        canvas.drawLine(margin + supLabelW, currentY, margin + supLabelW, currentY + supTableH, border)

        canvas.drawText("Shift Supervisor Name", margin + 6f, currentY + 11.5f, bold)
        canvas.drawText(shiftInfo.shiftSupervisor.ifBlank { "—" }, margin + supLabelW + 8f, currentY + 11.5f, text)
        canvas.drawLine(margin, currentY + supRowH, margin + leftColWidth, currentY + supRowH, border)

        val row2Y = currentY + supRowH
        canvas.drawText("Date", margin + 6f, row2Y + 11.5f, bold)
        canvas.drawText(shiftInfo.dateString.ifBlank { "—" }, margin + supLabelW + 8f, row2Y + 11.5f, text)
        canvas.drawLine(margin, row2Y + supRowH, margin + leftColWidth, row2Y + supRowH, border)

        val row3Y = currentY + supRowH * 2
        canvas.drawText("Signature", margin + 6f, row3Y + 11.5f, bold)

        // ========================================================
        // RIGHT COLUMN: 4 Columns: SL#, Unload Box Number, SL#, Load Box Number
        // ========================================================
        var rightY = margin
        val titleHRight = 16f
        canvas.drawRect(rightColX, rightY, rightColX + rightColWidth, rightY + titleHRight, fill)
        canvas.drawRect(rightColX, rightY, rightColX + rightColWidth, rightY + titleHRight, border)
        canvas.drawText(
            "Plate Load / Unload / Stock Check / Housekeeping Status",
            rightColX + 8f,
            rightY + 11f,
            title
        )
        rightY += titleHRight

        val subColH = 15f
        val halfW = rightColWidth / 2f
        val slW = 24f

        canvas.drawRect(rightColX, rightY, rightColX + rightColWidth, rightY + subColH, fill)
        canvas.drawRect(rightColX, rightY, rightColX + rightColWidth, rightY + subColH, border)

        canvas.drawLine(rightColX + halfW, rightY, rightColX + halfW, rightY + subColH, border)
        canvas.drawLine(rightColX + slW, rightY, rightColX + slW, rightY + subColH, border)
        canvas.drawLine(rightColX + halfW + slW, rightY, rightColX + halfW + slW, rightY + subColH, border)

        canvas.drawText("SL #", rightColX + 4f, rightY + 10.5f, bold)
        canvas.drawText("Unload Box Number", rightColX + slW + 10f, rightY + 10.5f, bold)
        canvas.drawText("SL #", rightColX + halfW + 4f, rightY + 10.5f, bold)
        canvas.drawText("Load Box Number", rightColX + halfW + slW + 14f, rightY + 10.5f, bold)

        rightY += subColH

        val rowHScan = 24f
        for (i in 0 until 30) {
            val yTop = rightY + i * rowHScan

            canvas.drawRect(rightColX, yTop, rightColX + rightColWidth, yTop + rowHScan, border)
            canvas.drawLine(rightColX + slW, yTop, rightColX + slW, yTop + rowHScan, border)
            canvas.drawLine(rightColX + halfW, yTop, rightColX + halfW, yTop + rowHScan, border)
            canvas.drawLine(rightColX + halfW + slW, yTop, rightColX + halfW + slW, yTop + rowHScan, border)

            val serial = i + 1
            canvas.drawText("$serial", rightColX + 5f, yTop + 15.5f, bold)
            if (i < unloadList.size) {
                canvas.drawText(unloadList[i].boxNumber, rightColX + slW + 10f, yTop + 15.5f, bold)
            }

            canvas.drawText("$serial", rightColX + halfW + 4f, yTop + 15.5f, bold)
            if (i < loadList.size) {
                canvas.drawText(loadList[i].boxNumber, rightColX + halfW + slW + 10f, yTop + 15.5f, bold)
            }
        }
    }

    private fun drawContinuationBoxPage(
        canvas: Canvas,
        startSerial: Int,
        unloadChunk: List<PlateScan>,
        loadChunk: List<PlateScan>,
        border: Paint,
        bold: Paint,
        title: Paint,
        fill: Paint
    ) {
        val margin = 20f
        val tableWidth = PAGE_WIDTH - margin * 2
        var currentY = margin

        val titleH = 20f
        canvas.drawRect(margin, currentY, margin + tableWidth, currentY + titleH, fill)
        canvas.drawRect(margin, currentY, margin + tableWidth, currentY + titleH, border)
        canvas.drawText(
            "Plate Load / Unload Status (Continued: Rows $startSerial to ${startSerial + 29})",
            margin + 120f,
            currentY + 14f,
            title
        )
        currentY += titleH + 4f

        val halfW = tableWidth / 2f
        val slW = 30f
        val subColH = 16f

        canvas.drawRect(margin, currentY, margin + tableWidth, currentY + subColH, fill)
        canvas.drawRect(margin, currentY, margin + tableWidth, currentY + subColH, border)
        canvas.drawLine(margin + halfW, currentY, margin + halfW, currentY + subColH, border)
        canvas.drawLine(margin + slW, currentY, margin + slW, currentY + subColH, border)
        canvas.drawLine(margin + halfW + slW, currentY, margin + halfW + slW, currentY + subColH, border)

        canvas.drawText("SL #", margin + 6f, currentY + 11.5f, bold)
        canvas.drawText("Unload Box Number", margin + slW + 30f, currentY + 11.5f, bold)
        canvas.drawText("SL #", margin + halfW + 6f, currentY + 11.5f, bold)
        canvas.drawText("Load Box Number", margin + halfW + slW + 30f, currentY + 11.5f, bold)
        currentY += subColH

        val rowH = 24f
        for (i in 0 until 30) {
            val yTop = currentY + i * rowH
            canvas.drawRect(margin, yTop, margin + tableWidth, yTop + rowH, border)
            canvas.drawLine(margin + slW, yTop, margin + slW, yTop + rowH, border)
            canvas.drawLine(margin + halfW, yTop, margin + halfW, yTop + rowH, border)
            canvas.drawLine(margin + halfW + slW, yTop, margin + halfW + slW, yTop + rowH, border)

            val serial = startSerial + i
            canvas.drawText("$serial", margin + 6f, yTop + 15.5f, bold)
            if (i < unloadChunk.size) {
                canvas.drawText(unloadChunk[i].boxNumber, margin + slW + 20f, yTop + 15.5f, bold)
            }

            canvas.drawText("$serial", margin + halfW + 6f, yTop + 15.5f, bold)
            if (i < loadChunk.size) {
                canvas.drawText(loadChunk[i].boxNumber, margin + halfW + slW + 20f, yTop + 15.5f, bold)
            }
        }
    }

    private fun drawFinalSummaryPage(
        canvas: Canvas,
        shiftInfo: ShiftInfo,
        chk: ChecklistAnswers,
        loadCount: Int,
        unloadCount: Int,
        stockCheckCount: Int,
        housekeepingCount: Int,
        border: Paint,
        text: Paint,
        bold: Paint,
        title: Paint,
        fill: Paint
    ) {
        val margin = 28f
        val tableWidth = PAGE_WIDTH - margin * 2
        var currentY = margin + 10f

        // Title banner
        val bannerH = 28f
        canvas.drawRect(margin, currentY, margin + tableWidth, currentY + bannerH, fill)
        canvas.drawRect(margin, currentY, margin + tableWidth, currentY + bannerH, border)
        canvas.drawText("Shift Production Summary (শিফট সারসংক্ষেপ)", margin + 150f, currentY + 18f, title)
        currentY += bannerH + 12f

        // Operator meta box
        val metaH = 34f
        canvas.drawRect(margin, currentY, margin + tableWidth, currentY + metaH, border)
        canvas.drawText("Operator Name: ${shiftInfo.operatorName.ifBlank { "—" }}", margin + 8f, currentY + 12f, bold)
        canvas.drawText("Operator ID: ${shiftInfo.idNumber.ifBlank { "—" }}", margin + 260f, currentY + 12f, bold)
        canvas.drawText("Shift: ${shiftInfo.shift.displayName}", margin + 420f, currentY + 12f, bold)
        canvas.drawLine(margin, currentY + 18f, margin + tableWidth, currentY + 18f, border)
        canvas.drawText("Designation: ${shiftInfo.designation.ifBlank { "Operator" }}", margin + 8f, currentY + 30f, text)
        canvas.drawText("Date: ${shiftInfo.dateString.ifBlank { "—" }}", margin + 260f, currentY + 30f, text)
        canvas.drawText("Supervisor: ${shiftInfo.shiftSupervisor.ifBlank { "—" }}", margin + 420f, currentY + 30f, text)
        currentY += metaH + 20f

        // Target / Achieved / Difference Table (Totals Only)
        val colSlW = 32f
        val colCatW = 210f
        val colTgtW = 85f
        val colAchW = 85f
        val colDiffW = tableWidth - colSlW - colCatW - colTgtW - colAchW

        val thH = 22f
        canvas.drawRect(margin, currentY, margin + tableWidth, currentY + thH, fill)
        canvas.drawRect(margin, currentY, margin + tableWidth, currentY + thH, border)
        canvas.drawLine(margin + colSlW, currentY, margin + colSlW, currentY + thH, border)
        canvas.drawLine(margin + colSlW + colCatW, currentY, margin + colSlW + colCatW, currentY + thH, border)
        canvas.drawLine(margin + colSlW + colCatW + colTgtW, currentY, margin + colSlW + colCatW + colTgtW, currentY + thH, border)
        canvas.drawLine(margin + colSlW + colCatW + colTgtW + colAchW, currentY, margin + colSlW + colCatW + colTgtW + colAchW, currentY + thH, border)

        canvas.drawText("SL", margin + 8f, currentY + 15f, bold)
        canvas.drawText("Category", margin + colSlW + 12f, currentY + 15f, bold)
        canvas.drawText("Target", margin + colSlW + colCatW + 20f, currentY + 15f, bold)
        canvas.drawText("Achieved", margin + colSlW + colCatW + colTgtW + 16f, currentY + 15f, bold)
        canvas.drawText("Difference (+/-)", margin + colSlW + colCatW + colTgtW + colAchW + 22f, currentY + 15f, bold)
        currentY += thH

        val categories = listOf(
            Triple("Plate Load", 85, loadCount),
            Triple("Plate Unload", 85, unloadCount),
            Triple("Stock Check Physically", 10, stockCheckCount),
            Triple("Housekeeping / Segregation", 10, housekeepingCount)
        )

        val trH = 24f
        var sl = 1
        for (item in categories) {
            canvas.drawRect(margin, currentY, margin + tableWidth, currentY + trH, border)
            canvas.drawLine(margin + colSlW, currentY, margin + colSlW, currentY + trH, border)
            canvas.drawLine(margin + colSlW + colCatW, currentY, margin + colSlW + colCatW, currentY + trH, border)
            canvas.drawLine(margin + colSlW + colCatW + colTgtW, currentY, margin + colSlW + colCatW + colTgtW, currentY + trH, border)
            canvas.drawLine(margin + colSlW + colCatW + colTgtW + colAchW, currentY, margin + colSlW + colCatW + colTgtW + colAchW, currentY + trH, border)

            canvas.drawText("$sl", margin + 12f, currentY + 16f, bold)
            canvas.drawText(item.first, margin + colSlW + 12f, currentY + 16f, text)
            canvas.drawText("${item.second}", margin + colSlW + colCatW + 30f, currentY + 16f, bold)
            canvas.drawText("${item.third}", margin + colSlW + colCatW + colTgtW + 32f, currentY + 16f, bold)

            val diff = item.third - item.second
            val diffStr = if (diff > 0) "+$diff" else "$diff"
            canvas.drawText(diffStr, margin + colSlW + colCatW + colTgtW + colAchW + 40f, currentY + 16f, bold)

            currentY += trH
            sl++
        }

        // Grand Total Row
        val totalRowH = 26f
        canvas.drawRect(margin, currentY, margin + tableWidth, currentY + totalRowH, fill)
        canvas.drawRect(margin, currentY, margin + tableWidth, currentY + totalRowH, border)
        canvas.drawLine(margin + colSlW + colCatW, currentY, margin + colSlW + colCatW, currentY + totalRowH, border)
        canvas.drawLine(margin + colSlW + colCatW + colTgtW, currentY, margin + colSlW + colCatW + colTgtW, currentY + totalRowH, border)
        canvas.drawLine(margin + colSlW + colCatW + colTgtW + colAchW, currentY, margin + colSlW + colCatW + colTgtW + colAchW, currentY + totalRowH, border)

        canvas.drawText("GRAND TOTAL", margin + 80f, currentY + 17f, bold)
        canvas.drawText("190", margin + colSlW + colCatW + 28f, currentY + 17f, bold)
        val grandAchieved = loadCount + unloadCount + stockCheckCount + housekeepingCount
        canvas.drawText("$grandAchieved", margin + colSlW + colCatW + colTgtW + 30f, currentY + 17f, bold)
        val grandDiff = grandAchieved - 190
        val grandDiffStr = if (grandDiff > 0) "+$grandDiff" else "$grandDiff"
        canvas.drawText(grandDiffStr, margin + colSlW + colCatW + colTgtW + colAchW + 38f, currentY + 17f, bold)

        currentY += totalRowH + 24f

        // Reason box if any
        val rBoxH = 50f
        canvas.drawRect(margin, currentY, margin + tableWidth, currentY + 16f, fill)
        canvas.drawRect(margin, currentY, margin + tableWidth, currentY + 16f + rBoxH, border)
        canvas.drawText("Reason For not Achieving Target (টার্গেট অর্জন না করার কারণ)", margin + 8f, currentY + 12f, bold)
        currentY += 16f
        if (chk.reasonNotAchievingTarget.isNotBlank()) {
            val lines = chk.reasonNotAchievingTarget.chunked(60)
            var ly = currentY + 14f
            for (line in lines.take(3)) {
                canvas.drawText(line, margin + 8f, ly, text)
                ly += 14f
            }
        }
        currentY += rBoxH + 30f

        // Bottom Signatures
        val sigBoxW = tableWidth / 2f
        val sigBoxH = 60f
        canvas.drawRect(margin, currentY, margin + sigBoxW - 10f, currentY + sigBoxH, border)
        canvas.drawText("Operator Signature:", margin + 8f, currentY + 16f, bold)
        canvas.drawLine(margin + 10f, currentY + sigBoxH - 14f, margin + sigBoxW - 20f, currentY + sigBoxH - 14f, border)
        canvas.drawText("Date: ${shiftInfo.dateString}", margin + 10f, currentY + sigBoxH - 4f, text)

        val rightSigX = margin + sigBoxW + 10f
        canvas.drawRect(rightSigX, currentY, margin + tableWidth, currentY + sigBoxH, border)
        canvas.drawText("Shift Supervisor Signature:", rightSigX + 8f, currentY + 16f, bold)
        canvas.drawLine(rightSigX + 10f, currentY + sigBoxH - 14f, margin + tableWidth - 10f, currentY + sigBoxH - 14f, border)
        canvas.drawText("Date: ${shiftInfo.dateString}", rightSigX + 10f, currentY + sigBoxH - 4f, text)
    }
}
