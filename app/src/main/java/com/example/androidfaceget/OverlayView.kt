package com.example.androidfaceget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlin.math.roundToInt

class OverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var result: FaceLandmarkerResult? = null
    private var poses: List<FacePose?> = emptyList()
    private var imageWidth = 1
    private var imageHeight = 1

    private val boxPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.face_box)
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    private val labelPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.face_box)
        textSize = 36f
        isAntiAlias = true
    }

    private val labelBackgroundPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.face_label_background)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    fun setResults(
        result: FaceLandmarkerResult,
        imageWidth: Int,
        imageHeight: Int,
        poses: List<FacePose?>,
    ) {
        this.result = result
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        this.poses = poses
        invalidate()
    }

    fun clear() {
        result = null
        poses = emptyList()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val faces = result?.faceLandmarks().orEmpty()
        if (faces.isEmpty()) return

        faces.forEachIndexed { index, landmarks ->
            val mapped = landmarks.toMappedRect() ?: return@forEachIndexed
            canvas.drawRect(mapped, boxPaint)

            val pose = poses.getOrNull(index)
            val label = pose?.toOverlayLabel(index) ?: "Face ${index + 1}: ${FaceOrientation.UNKNOWN.label}"
            val labelWidth = labelPaint.measureText(label)
            val labelHeight = labelPaint.textSize
            val labelTop = (mapped.top - labelHeight - LABEL_PADDING).coerceAtLeast(LABEL_PADDING)
            canvas.drawRoundRect(
                mapped.left,
                labelTop - labelHeight,
                mapped.left + labelWidth + LABEL_PADDING * 2,
                labelTop + LABEL_PADDING,
                LABEL_RADIUS,
                LABEL_RADIUS,
                labelBackgroundPaint,
            )
            canvas.drawText(label, mapped.left + LABEL_PADDING, labelTop, labelPaint)
        }
    }

    private fun List<NormalizedLandmark>.toMappedRect(): RectF? {
        if (isEmpty()) return null

        var left = 1f
        var top = 1f
        var right = 0f
        var bottom = 0f

        forEach { landmark ->
            val x = landmark.x().coerceIn(0f, 1f)
            val y = landmark.y().coerceIn(0f, 1f)
            left = minOf(left, x)
            top = minOf(top, y)
            right = maxOf(right, x)
            bottom = maxOf(bottom, y)
        }

        val mapped = BoxMapper.mapCenterCrop(
            sourceBox = FloatBox(
                left = left * imageWidth,
                top = top * imageHeight,
                right = right * imageWidth,
                bottom = bottom * imageHeight,
            ),
            imageSize = FrameSize(imageWidth, imageHeight),
            viewSize = FrameSize(width, height),
        )
        return RectF(mapped.left, mapped.top, mapped.right, mapped.bottom)
    }

    private fun FacePose.toOverlayLabel(index: Int): String =
        "Face ${index + 1}: ${orientation.label} Y:${yawDegrees.roundToInt()} P:${pitchDegrees.roundToInt()}"

    companion object {
        private const val LABEL_PADDING = 10f
        private const val LABEL_RADIUS = 8f
    }
}
