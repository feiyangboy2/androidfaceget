package com.example.androidfaceget

import kotlin.math.max

data class FloatBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
)

data class FrameSize(
    val width: Int,
    val height: Int,
)

object BoxMapper {
    fun mapCenterCrop(
        sourceBox: FloatBox,
        imageSize: FrameSize,
        viewSize: FrameSize,
    ): FloatBox {
        if (imageSize.width <= 0 || imageSize.height <= 0 || viewSize.width <= 0 || viewSize.height <= 0) {
            return FloatBox(0f, 0f, 0f, 0f)
        }

        val scale = max(
            viewSize.width.toFloat() / imageSize.width.toFloat(),
            viewSize.height.toFloat() / imageSize.height.toFloat(),
        )
        val scaledWidth = imageSize.width * scale
        val scaledHeight = imageSize.height * scale
        val xOffset = (viewSize.width - scaledWidth) / 2f
        val yOffset = (viewSize.height - scaledHeight) / 2f

        return FloatBox(
            left = sourceBox.left * scale + xOffset,
            top = sourceBox.top * scale + yOffset,
            right = sourceBox.right * scale + xOffset,
            bottom = sourceBox.bottom * scale + yOffset,
        )
    }
}

