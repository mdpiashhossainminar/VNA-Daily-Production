package com.example.camera

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.VnaGreen
import com.example.ui.theme.VnaNavyCard
import com.example.ui.theme.VnaTextDark
import com.example.ui.theme.VnaTextPrimary
import com.example.ui.theme.VnaYellow
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

@Composable
fun CameraQrScanner(
    modifier: Modifier = Modifier,
    onPlateScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    var scannedOverlayText by remember { mutableStateOf<String?>(null) }

    val isScanningPaused = remember { AtomicBoolean(false) }
    val lastScannedCode = remember { AtomicReference<String?>(null) }
    val lastEmptyFrameTime = remember { AtomicLong(System.currentTimeMillis()) }
    val hadEmptyPeriod = remember { AtomicBoolean(true) }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build()
                    val scanner = BarcodeScanning.getClient(options)

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                processImageProxyWithRules(
                                    scanner = scanner,
                                    imageProxy = imageProxy,
                                    isPaused = isScanningPaused,
                                    lastCode = lastScannedCode,
                                    hadEmptyPeriod = hadEmptyPeriod,
                                    lastEmptyTime = lastEmptyFrameTime,
                                    onScanAccepted = { detectedCode ->
                                        // 1. Pause scanning
                                        isScanningPaused.set(true)
                                        hadEmptyPeriod.set(false)

                                        // 2. Notify callback on main thread
                                        mainHandler.post {
                                            scannedOverlayText = detectedCode
                                            onPlateScanned(detectedCode)
                                        }

                                        // 3. Resume automatically after ~2 seconds (2000ms)
                                        mainHandler.postDelayed({
                                            scannedOverlayText = null
                                            isScanningPaused.set(false)
                                        }, 2000L)
                                    }
                                )
                            }
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (exc: Exception) {
                        exc.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // Center targeting square with yellow border
        Box(
            modifier = Modifier
                .size(240.dp)
                .border(2.dp, if (scannedOverlayText != null) VnaGreen else VnaYellow, RoundedCornerShape(12.dp))
        )

        // 2-second Scanned Overlay Display
        AnimatedVisibility(
            visible = scannedOverlayText != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xE60E131A))
                    .border(2.dp, VnaGreen, RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "✓ স্ক্যান সম্পন্ন",
                        color = VnaGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = scannedOverlayText ?: "",
                        color = VnaTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
private fun processImageProxyWithRules(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    isPaused: AtomicBoolean,
    lastCode: AtomicReference<String?>,
    hadEmptyPeriod: AtomicBoolean,
    lastEmptyTime: AtomicLong,
    onScanAccepted: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val now = System.currentTimeMillis()
                val visibleBarcode = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }

                if (visibleBarcode == null) {
                    // No barcode in current frame
                    val prevEmptyTime = lastEmptyTime.get()
                    if (now - prevEmptyTime >= 1000L) {
                        // Has left the camera view for at least 1 second!
                        hadEmptyPeriod.set(true)
                    }
                } else {
                    lastEmptyTime.set(now)
                    val rawText = visibleBarcode.rawValue?.trim() ?: ""

                    if (rawText.isNotEmpty() && !isPaused.get()) {
                        val previous = lastCode.get()

                        // Can register if:
                        // 1. It is a different code
                        // 2. OR it is the same code BUT it had left the view for at least 1 second
                        val canRegister = (rawText != previous) || hadEmptyPeriod.get()

                        if (canRegister) {
                            lastCode.set(rawText)
                            onScanAccepted(rawText)
                        }
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

object GalleryQrDecoder {
    fun decodeImages(
        context: Context,
        uris: List<Uri>,
        onResult: (List<String>) -> Unit
    ) {
        if (uris.isEmpty()) {
            onResult(emptyList())
            return
        }

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
        val scanner = BarcodeScanning.getClient(options)

        val results = mutableListOf<String>()
        var processedCount = 0

        for (uri in uris) {
            try {
                val inputImage = InputImage.fromFilePath(context, uri)
                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        // Exactly one entry per selected image
                        val firstBarcode = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                        if (firstBarcode != null) {
                            results.add(firstBarcode.rawValue!!.trim())
                        }
                    }
                    .addOnCompleteListener {
                        processedCount++
                        if (processedCount >= uris.size) {
                            onResult(results)
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                processedCount++
                if (processedCount >= uris.size) {
                    onResult(results)
                }
            }
        }
    }
}
