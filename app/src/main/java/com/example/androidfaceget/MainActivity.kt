package com.example.androidfaceget

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity(), FaceDetectorHelper.DetectorListener {
    private lateinit var previewView: PreviewView
    private lateinit var overlayView: OverlayView
    private lateinit var statusText: TextView
    private lateinit var switchCameraButton: Button
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var faceDetectorHelper: FaceDetectorHelper

    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing = CameraSelector.LENS_FACING_FRONT

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                statusText.text = getString(R.string.status_permission_denied)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        overlayView = findViewById(R.id.overlayView)
        statusText = findViewById(R.id.statusText)
        switchCameraButton = findViewById(R.id.switchCameraButton)

        cameraExecutor = Executors.newSingleThreadExecutor()
        faceDetectorHelper = FaceDetectorHelper(
            context = this,
            detectorListener = this,
        )

        switchCameraButton.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            bindCameraUseCases()
        }

        if (hasCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        faceDetectorHelper.clearFaceDetector()
        cameraExecutor.shutdown()
    }

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
            },
            ContextCompat.getMainExecutor(this),
        )
    }

    private fun bindCameraUseCases() {
        val provider = cameraProvider ?: return
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        val preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }

        val imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    faceDetectorHelper.detectLiveStreamFrame(
                        imageProxy = imageProxy,
                        mirrorFrontCamera = lensFacing == CameraSelector.LENS_FACING_FRONT,
                    )
                }
            }

        try {
            provider.unbindAll()
            provider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            overlayView.clear()
            statusText.text = getString(R.string.status_waiting)
        } catch (error: RuntimeException) {
            statusText.text = error.message ?: "Camera binding failed"
        }
    }

    override fun onResults(resultBundle: FaceDetectorHelper.ResultBundle) {
        runOnUiThread {
            val faceCount = resultBundle.result.detections().size
            overlayView.setResults(
                result = resultBundle.result,
                imageWidth = resultBundle.inputImageWidth,
                imageHeight = resultBundle.inputImageHeight,
            )
            statusText.text = "Faces: $faceCount  ${resultBundle.inferenceTimeMs} ms"
        }
    }

    override fun onError(error: String) {
        runOnUiThread {
            statusText.text = error
            overlayView.clear()
        }
    }
}

