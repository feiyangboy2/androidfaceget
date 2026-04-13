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
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult

class FaceLandmarkerHelper(
    private val context: Context,
    private val landmarkerListener: LandmarkerListener,
    private val minFaceDetectionConfidence: Float = DEFAULT_FACE_DETECTION_CONFIDENCE,
    private val minFacePresenceConfidence: Float = DEFAULT_FACE_PRESENCE_CONFIDENCE,
    private val minTrackingConfidence: Float = DEFAULT_FACE_TRACKING_CONFIDENCE,
) {
    private var faceLandmarker: FaceLandmarker? = null

    init {
        setupFaceLandmarker()
    }

    fun clearFaceLandmarker() {
        faceLandmarker?.close()
        faceLandmarker = null
    }

    fun setupFaceLandmarker() {
        clearFaceLandmarker()

        val baseOptions = BaseOptions.builder()
            .setDelegate(Delegate.CPU)
            .setModelAssetPath(MODEL_ASSET_NAME)
            .build()

        val options = FaceLandmarker.FaceLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setMinFaceDetectionConfidence(minFaceDetectionConfidence)
            .setMinFacePresenceConfidence(minFacePresenceConfidence)
            .setMinTrackingConfidence(minTrackingConfidence)
            .setNumFaces(MAX_NUM_FACES)
            .setOutputFacialTransformationMatrixes(true)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener(this::returnLiveStreamResult)
            .setErrorListener(this::returnLiveStreamError)
            .build()

        try {
            faceLandmarker = FaceLandmarker.createFromOptions(context, options)
        } catch (error: RuntimeException) {
            Log.e(TAG, "Face landmarker failed to initialize.", error)
            landmarkerListener.onError(error.message ?: "Face landmarker failed to initialize")
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
            Log.e(TAG, "Face landmarking failed.", error)
            landmarkerListener.onError(error.message ?: "Face landmarking failed")
        } finally {
            imageProxy.close()
        }
    }

    private fun detectAsync(mpImage: MPImage, frameTime: Long) {
        faceLandmarker?.detectAsync(mpImage, frameTime)
    }

    private fun returnLiveStreamResult(result: FaceLandmarkerResult, input: MPImage) {
        val inferenceTime = SystemClock.uptimeMillis() - result.timestampMs()
        landmarkerListener.onResults(
            ResultBundle(
                result = result,
                inferenceTimeMs = inferenceTime,
                inputImageHeight = input.height,
                inputImageWidth = input.width,
            ),
        )
    }

    private fun returnLiveStreamError(error: RuntimeException) {
        landmarkerListener.onError(error.message ?: "Unknown MediaPipe error")
    }

    data class ResultBundle(
        val result: FaceLandmarkerResult,
        val inferenceTimeMs: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int,
    )

    interface LandmarkerListener {
        fun onResults(resultBundle: ResultBundle)
        fun onError(error: String)
    }

    companion object {
        private const val TAG = "FaceLandmarkerHelper"
        private const val MODEL_ASSET_NAME = "face_landmarker.task"
        private const val DEFAULT_FACE_DETECTION_CONFIDENCE = 0.5f
        private const val DEFAULT_FACE_PRESENCE_CONFIDENCE = 0.5f
        private const val DEFAULT_FACE_TRACKING_CONFIDENCE = 0.5f
        private const val MAX_NUM_FACES = 3
    }
}

