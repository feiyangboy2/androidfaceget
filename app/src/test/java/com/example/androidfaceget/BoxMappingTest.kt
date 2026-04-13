package com.example.androidfaceget

import org.junit.Assert.assertEquals
import org.junit.Test

class BoxMappingTest {
    @Test
    fun mapCenterCropScalesSquareImageIntoWideView() {
        val mapped = BoxMapper.mapCenterCrop(
            sourceBox = FloatBox(25f, 25f, 75f, 75f),
            imageSize = FrameSize(100, 100),
            viewSize = FrameSize(200, 100),
        )

        assertBoxEquals(FloatBox(50f, 0f, 150f, 100f), mapped)
    }

    @Test
    fun mapCenterCropScalesWideImageIntoTallView() {
        val mapped = BoxMapper.mapCenterCrop(
            sourceBox = FloatBox(25f, 10f, 75f, 40f),
            imageSize = FrameSize(100, 50),
            viewSize = FrameSize(100, 100),
        )

        assertBoxEquals(FloatBox(0f, 20f, 100f, 80f), mapped)
    }

    @Test
    fun mapCenterCropReturnsEmptyBoxForInvalidSizes() {
        val mapped = BoxMapper.mapCenterCrop(
            sourceBox = FloatBox(25f, 10f, 75f, 40f),
            imageSize = FrameSize(0, 50),
            viewSize = FrameSize(100, 100),
        )

        assertBoxEquals(FloatBox(0f, 0f, 0f, 0f), mapped)
    }

    private fun assertBoxEquals(expected: FloatBox, actual: FloatBox) {
        assertEquals(expected.left, actual.left, DELTA)
        assertEquals(expected.top, actual.top, DELTA)
        assertEquals(expected.right, actual.right, DELTA)
        assertEquals(expected.bottom, actual.bottom, DELTA)
    }

    companion object {
        private const val DELTA = 0.001f
    }
}

