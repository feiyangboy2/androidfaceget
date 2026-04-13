package com.example.androidfaceget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult

class OverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var result: FaceDetectorResult? = null
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

    fun setResults(result: FaceDetectorResult, imageWidth: Int, imageHeight: Int) {
        this.result = result
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        invalidate()
    }

    fun clear() {
        result = null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val detections = result?.detections().orEmpty()
        if (detections.isEmpty()) return

        detections.forEachIndexed { index, detection ->
            val mapped = detection.boundingBox().toMappedRect()
            canvas.drawRect(mapped, boxPaint)

            val label = "Face ${index + 1}"
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

    private fun RectF.toMappedRect(): RectF {
        val mapped = BoxMapper.mapCenterCrop(
            sourceBox = FloatBox(left, top, right, bottom),
            imageSize = FrameSize(imageWidth, imageHeight),
            viewSize = FrameSize(width, height),
        )
        return RectF(mapped.left, mapped.top, mapped.right, mapped.bottom)
    }

    companion object {
        private const val LABEL_PADDING = 10f
        private const val LABEL_RADIUS = 8f
    }
}

