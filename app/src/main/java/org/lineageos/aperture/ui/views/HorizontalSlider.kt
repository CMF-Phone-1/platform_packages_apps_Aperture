/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.aperture.ui.views

import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import org.lineageos.aperture.ext.mapToRange
import org.lineageos.aperture.ext.px

class HorizontalSlider @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : Slider(context, attrs) {
    override fun track(): RectF {
        val trackHeight = height * 0.8f

        val left = height / 2f
        val right = width - left

        val top = (height - trackHeight) / 2f
        val bottom = height - top

        return RectF(left, top, right, bottom)
    }

    override fun thumb(): Triple<Float, Float, Float> {
        val track = track()
        val trackHeight = track.height()
        val thumbRadius = (trackHeight / 2f) - 2.px.toFloat()

        val minCx = track.left + thumbRadius + 2.px.toFloat()
        val maxCx = track.right - thumbRadius - 2.px.toFloat()
        val availableWidth = maxCx - minCx

        val cx = if (steps > 0) {
            val progress = Int.mapToRange(0..steps, progress).toFloat() / steps
            (availableWidth * progress) + minCx
        } else {
            (availableWidth * progress) + minCx
        }
        val cy = height / 2f

        return Triple(cx, cy, thumbRadius)
    }

    @Suppress("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)

        if (!isEnabled) {
            return false
        }

        when (event?.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_UP -> {
                val track = track()
                val trackHeight = track.height()
                val thumbRadius = (trackHeight / 2f) - 2.px.toFloat()
                val minCx = track.left + thumbRadius + 2.px.toFloat()
                val maxCx = track.right - thumbRadius - 2.px.toFloat()

                progress = (event.x - minCx) / (maxCx - minCx)
                progress = progress.coerceIn(0f, 1f)
                onProgressChangedByUser?.invoke(progress)
            }
        }

        return true
    }
}
