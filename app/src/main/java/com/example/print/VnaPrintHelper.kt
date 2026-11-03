package com.example.print

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.ChecklistAnswers
import com.example.model.PlateScan
import com.example.model.ShiftInfo
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object VnaPrintHelper {

    fun findActivity(context: Context): Activity? {
        var current = context
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }

    fun printWorksheet(
        context: Context,
        shiftInfo: ShiftInfo,
        checklist: ChecklistAnswers,
        loadPlates: List<PlateScan>,
        unloadPlates: List<PlateScan>,
        stockCheckCount: Int,
        housekeepingCount: Int
    ) {
        val activity = findActivity(context)
        if (activity == null) {
            Toast.makeText(context, "Activity context required for printing", Toast.LENGTH_SHORT).show()
            return
        }

        val printManager = activity.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Toast.makeText(activity, "Print service not available", Toast.LENGTH_SHORT).show()
            return
        }

        val jobName = "VNA_Daily_${shiftInfo.shift.code}_${shiftInfo.dateString.replace(".", "_")}"

        val adapter = object : PrintDocumentAdapter() {
            private var tempPdfFile: File? = null

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }

                try {
                    val pdfFile = File(context.cacheDir, "vna_print_temp.pdf")
                    VnaPdfRenderer.generatePdf(
                        context = context,
                        shiftInfo = shiftInfo,
                        checklist = checklist,
                        loadPlates = loadPlates,
                        unloadPlates = unloadPlates,
                        stockCheckCount = stockCheckCount,
                        housekeepingCount = housekeepingCount,
                        outputFile = pdfFile
                    )
                    tempPdfFile = pdfFile

                    val totalPages = VnaPdfRenderer.calculateTotalPages(unloadPlates.size, loadPlates.size)

                    val info = PrintDocumentInfo.Builder("vna_worksheet.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(totalPages)
                        .build()

                    callback?.onLayoutFinished(info, true)
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback?.onLayoutFailed(e.message)
                }
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onWriteCancelled()
                    return
                }

                try {
                    val file = tempPdfFile ?: File(context.cacheDir, "vna_print_temp.pdf")
                    val input = FileInputStream(file)
                    val output = FileOutputStream(destination?.fileDescriptor)

                    val buffer = ByteArray(16384)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } >= 0) {
                        output.write(buffer, 0, bytesRead)
                    }

                    input.close()
                    output.flush()
                    output.close()

                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback?.onWriteFailed(e.message)
                }
            }
        }

        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()

        printManager.print(jobName, adapter, attributes)
    }

    fun sharePdf(
        context: Context,
        shiftInfo: ShiftInfo,
        checklist: ChecklistAnswers,
        loadPlates: List<PlateScan>,
        unloadPlates: List<PlateScan>,
        stockCheckCount: Int,
        housekeepingCount: Int
    ) {
        try {
            val shareDir = File(context.cacheDir, "shared_pdfs").apply { mkdirs() }
            val fileName = "VNA_Daily_${shiftInfo.shift.code}_${System.currentTimeMillis()}.pdf"
            val file = File(shareDir, fileName)

            VnaPdfRenderer.generatePdf(
                context = context,
                shiftInfo = shiftInfo,
                checklist = checklist,
                loadPlates = loadPlates,
                unloadPlates = unloadPlates,
                stockCheckCount = stockCheckCount,
                housekeepingCount = housekeepingCount,
                outputFile = file
            )

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share VNA Worksheet PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error creating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
