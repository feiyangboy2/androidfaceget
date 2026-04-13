package com.example.androidfaceget

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2

enum class FaceOrientation(val label: String) {
    FRONT("正脸"),
    LEFT_SIDE("左侧脸"),
    RIGHT_SIDE("右侧脸"),
    LOOKING_UP("面朝上"),
    LOOKING_DOWN("面朝下"),
    UNKNOWN("未知"),
}

data class FacePose(
    val yawDegrees: Float,
    val pitchDegrees: Float,
    val rollDegrees: Float,
    val orientation: FaceOrientation,
)

object FacePoseEstimator {
    private const val SIDE_FACE_THRESHOLD_DEGREES = 25f
    private const val LOOK_UP_DOWN_THRESHOLD_DEGREES = 18f

    fun fromMatrix(matrix: FloatArray?, mirrorFrontCamera: Boolean): FacePose? {
        if (matrix == null || matrix.size < 16) return null

        val yaw = atan2(matrix[8].toDouble(), matrix[10].toDouble()).toDegrees()
        val pitch = asin((-matrix[9]).coerceIn(-1f, 1f).toDouble()).toDegrees()
        val roll = atan2(matrix[1].toDouble(), matrix[5].toDouble()).toDegrees()

        return fromAngles(
            yawDegrees = if (mirrorFrontCamera) -yaw else yaw,
            pitchDegrees = pitch,
            rollDegrees = roll,
        )
    }

    fun fromAngles(yawDegrees: Float, pitchDegrees: Float, rollDegrees: Float): FacePose {
        val orientation = when {
            pitchDegrees >= LOOK_UP_DOWN_THRESHOLD_DEGREES -> FaceOrientation.LOOKING_DOWN
            pitchDegrees <= -LOOK_UP_DOWN_THRESHOLD_DEGREES -> FaceOrientation.LOOKING_UP
            yawDegrees >= SIDE_FACE_THRESHOLD_DEGREES -> FaceOrientation.RIGHT_SIDE
            yawDegrees <= -SIDE_FACE_THRESHOLD_DEGREES -> FaceOrientation.LEFT_SIDE
            else -> FaceOrientation.FRONT
        }

        return FacePose(
            yawDegrees = yawDegrees,
            pitchDegrees = pitchDegrees,
            rollDegrees = rollDegrees,
            orientation = orientation,
        )
    }

    private fun Double.toDegrees(): Float = (this * 180.0 / PI).toFloat()
}

