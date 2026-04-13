package com.example.androidfaceget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult

class FaceDetectorHelper(
    private val context: Context,
    private val minDetectionConfidence: Float = DEFAULT_MIN_DETECTION_CONFIDENCE,
    private val detectorListener: DetectorListener,
) {
    private var faceDetector: FaceDetector? = null

    init {
        setupFaceDetector()
    }

    fun clearFaceDetector() {
        faceDetector?.close()
        faceDetector = null
    }

    fun setupFaceDetector() {
        clearFaceDetector()

        val baseOptions = BaseOptions.builder()
            .setDelegate(Delegate.CPU)
            .setModelAssetPath(MODEL_ASSET_NAME)
            .build()

        val options = FaceDetector.FaceDetectorOptions.builder()
            .setBaseOptions(baseOptions)
            .setMinDetectionConfidence(minDetectionConfidence)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener(this::returnLiveStreamResult)
            .setErrorListener(this::returnLiveStreamError)
            .build()

        try {
            faceDetector = FaceDetector.createFromOptions(context, options)
        } catch (error: RuntimeException) {
            Log.e(TAG, "Face detector failed to initialize.", error)
            detectorListener.onError(error.message ?: "Face detector failed to initialize")
        }
    }

    fun detectLiveStreamFrame(imageProxy: ImageProxy, mirrorFrontCamera: Boolean) {
        val frameTime = SystemClock.uptimeMillis()

        try {
            val bitmapBuffer = Bitmap.createBitmap(
                imageProxy.width,
                imageProxy.height,
                Bitmap.Config.ARGB_8888,
            )
            val buffer = imageProxy.planes[0].buffer
            buffer.rewind()
            bitmapBuffer.copyPixelsFromBuffer(buffer)

            val matrix = Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                if (mirrorFrontCamera) {
                    postScale(
                        -1f,
                        1f,
                        imageProxy.width.toFloat(),
                        imageProxy.height.toFloat(),
                    )
                }
            }

            val rotatedBitmap = Bitmap.createBitmap(
                bitmapBuffer,
                0,
                0,
                bitmapBuffer.width,
                bitmapBuffer.height,
                matrix,
                true,
            )

            detectAsync(BitmapImageBuilder(rotatedBitmap).build(), frameTime)
        } catch (error: RuntimeException) {
            Log.e(TAG, "Face detection failed.", error)
            detectorListener.onError(error.message ?: "Face detection failed")
        } finally {
            imageProxy.close()
        }
    }

    private fun detectAsync(mpImage: MPImage, frameTime: Long) {
        faceDetector?.detectAsync(mpImage, frameTime)
    }

    private fun returnLiveStreamResult(result: FaceDetectorResult, input: MPImage) {
        val inferenceTime = SystemClock.uptimeMillis() - result.timestampMs()
        detectorListener.onResults(
            ResultBundle(
                result = result,
                inferenceTimeMs = inferenceTime,
                inputImageHeight = input.height,
                inputImageWidth = input.width,
            ),
        )
    }

    private fun returnLiveStreamError(error: RuntimeException) {
        detectorListener.onError(error.message ?: "Unknown MediaPipe error")
    }

    data class ResultBundle(
        val result: FaceDetectorResult,
        val inferenceTimeMs: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int,
    )

    interface DetectorListener {
        fun onResults(resultBundle: ResultBundle)
        fun onError(error: String)
    }

    companion object {
        private const val TAG = "FaceDetectorHelper"
        private const val MODEL_ASSET_NAME = "face_detection_short_range.tflite"
        private const val DEFAULT_MIN_DETECTION_CONFIDENCE = 0.5f
    }
}
