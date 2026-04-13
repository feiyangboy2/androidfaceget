package com.example.androidfaceget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

class FacePoseEstimatorTest {
    @Test
    fun classifyReturnsFrontForSmallAngles() {
        val pose = FacePoseEstimator.fromAngles(yawDegrees = 4f, pitchDegrees = -3f, rollDegrees = 2f)

        assertEquals(FaceOrientation.FRONT, pose.orientation)
        assertEquals("正脸", pose.orientation.label)
    }

    @Test
    fun classifyPrioritizesLookingDownBeforeSideFace() {
        val pose = FacePoseEstimator.fromAngles(yawDegrees = 35f, pitchDegrees = 20f, rollDegrees = 0f)

        assertEquals(FaceOrientation.LOOKING_DOWN, pose.orientation)
    }

    @Test
    fun classifyReturnsLookingUpForNegativePitch() {
        val pose = FacePoseEstimator.fromAngles(yawDegrees = 0f, pitchDegrees = -20f, rollDegrees = 0f)

        assertEquals(FaceOrientation.LOOKING_UP, pose.orientation)
    }

    @Test
    fun classifyReturnsSideFacesForYaw() {
        val left = FacePoseEstimator.fromAngles(yawDegrees = -28f, pitchDegrees = 0f, rollDegrees = 0f)
        val right = FacePoseEstimator.fromAngles(yawDegrees = 28f, pitchDegrees = 0f, rollDegrees = 0f)

        assertEquals(FaceOrientation.LEFT_SIDE, left.orientation)
        assertEquals(FaceOrientation.RIGHT_SIDE, right.orientation)
    }

    @Test
    fun frontCameraMirrorsYawBeforeClassifying() {
        val pose = FacePoseEstimator.fromMatrix(
            matrix = rotationMatrix(yawDegrees = 30f, pitchDegrees = 0f, rollDegrees = 0f),
            mirrorFrontCamera = true,
        )

        assertEquals(FaceOrientation.LEFT_SIDE, pose?.orientation)
    }

    @Test
    fun fromMatrixReturnsNullForInvalidMatrix() {
        assertNull(FacePoseEstimator.fromMatrix(floatArrayOf(1f, 0f, 0f), mirrorFrontCamera = false))
    }

    private fun rotationMatrix(yawDegrees: Float, pitchDegrees: Float, rollDegrees: Float): FloatArray {
        val yaw = Math.toRadians(yawDegrees.toDouble())
        val pitch = Math.toRadians(pitchDegrees.toDouble())
        val roll = Math.toRadians(rollDegrees.toDouble())

        val cy = cos(yaw)
        val sy = sin(yaw)
        val cp = cos(pitch)
        val sp = sin(pitch)
        val cr = cos(roll)
        val sr = sin(roll)

        val r00 = cy * cr + sy * sp * sr
        val r01 = sr * cp
        val r02 = -sy * cr + cy * sp * sr
        val r10 = -cy * sr + sy * sp * cr
        val r11 = cr * cp
        val r12 = sr * sy + cy * sp * cr
        val r20 = sy * cp
        val r21 = -sp
        val r22 = cy * cp

        return floatArrayOf(
            r00.toFloat(), r01.toFloat(), r02.toFloat(), 0f,
            r10.toFloat(), r11.toFloat(), r12.toFloat(), 0f,
            r20.toFloat(), r21.toFloat(), r22.toFloat(), 0f,
            0f, 0f, 0f, 1f,
        )
    }
}

