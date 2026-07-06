/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.aperture.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.View
import org.lineageos.aperture.R
import org.lineageos.aperture.ext.px
import org.lineageos.aperture.models.Rotation

@Suppress("PrivateResource")
abstract class Slider @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val trackPaint = Paint().apply {
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
        setShadowLayer(1f, 0f, 0f, Color.BLACK)
    }

    private val trackBorderPaint = Paint().apply {
        style = Paint.Style.STROKE
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
        strokeWidth = 2F
    }

    private val tickPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#4DFFFFFF") // 30% transparent white
    }

    private val thumbPaint = Paint().apply {
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
        setShadowLayer(3f, 0f, 0f, Color.BLACK)
    }

    private val thumbTextPaint = Paint()

    private var gradientColors: IntArray

    private var lastHapticProgress = 0f

    var progress = 0.5f
        set(value) {
            val clamped = value.coerceIn(0f, 1f)
            field = clamped
            invalidate()

            if (visibility == View.VISIBLE) {
                val app = context.applicationContext as? org.lineageos.aperture.ApertureApplication
                val hapticEnabled = app?.preferencesRepository?.hapticFeedback?.value ?: true
                val tickStep = 0.02f
                val currentTick = (clamped / tickStep).toInt()
                val lastTick = (lastHapticProgress / tickStep).toInt()
                if (currentTick != lastTick) {
                    if (hapticEnabled) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        } else {
                            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        }
                    }
                    lastHapticProgress = clamped
                }
            }
        }
    var onProgressChangedByUser: ((value: Float) -> Unit)? = null

    var textFormatter: (value: Float) -> String = {
        "%.01f".format(it)
    }

    var screenRotation = Rotation.ROTATION_0
        set(value) {
            field = value
            invalidate()
        }

    var steps = 0

    init {
        context.obtainStyledAttributes(attrs, R.styleable.Slider, 0, 0).apply {
            try {
                if (getBoolean(R.styleable.Slider_trackColorGradient, false)) {
                    val trackColorGradientStart =
                        getColor(R.styleable.Slider_trackColorGradientStart, Color.WHITE)
                    val trackColorGradientCenter =
                        getColor(R.styleable.Slider_trackColorGradientCenter, Color.WHITE)
                    val trackColorGradientEnd =
                        getColor(R.styleable.Slider_trackColorGradientEnd, Color.WHITE)
                    gradientColors = intArrayOf(
                        trackColorGradientStart,
                        trackColorGradientCenter,
                        trackColorGradientEnd
                    )
                } else {
                    gradientColors = intArrayOf()
                    trackPaint.color = getColor(R.styleable.Slider_trackColor, Color.WHITE)
                }
                thumbPaint.color = getColor(R.styleable.Slider_thumbColor, Color.BLACK)
                trackBorderPaint.color = getColor(
                    R.styleable.Slider_trackBorderColor, Color.TRANSPARENT
                )
                thumbTextPaint.color = getColor(R.styleable.Slider_thumbTextColor, Color.WHITE)
                thumbTextPaint.textSize =
                    getDimension(R.styleable.Slider_thumbTextSize, 10.px.toFloat())
            } finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawTrack(canvas)
        drawTicks(canvas)
        drawThumb(canvas)
    }

    private fun drawTicks(canvas: Canvas) {
        val track = track()
        val isHorizontal = track.width() > track.height()
        val tickCount = 9 // 9 ticks (0%, 12.5%, 25%, ..., 100%)

        if (isHorizontal) {
            val thumbRadius = (track.height() / 2f) - 2.px.toFloat()
            val minX = track.left + thumbRadius + 2.px.toFloat()
            val maxX = track.right - thumbRadius - 2.px.toFloat()
            val rangeX = maxX - minX

            val tickWidth = 1.px.toFloat()
            val tickHeight = track.height() * 0.4f
            val startY = track.centerY() - tickHeight / 2f
            val endY = startY + tickHeight

            for (i in 0 until tickCount) {
                val fraction = i.toFloat() / (tickCount - 1)
                val x = minX + fraction * rangeX
                canvas.drawRect(x - tickWidth / 2f, startY, x + tickWidth / 2f, endY, tickPaint)
            }
        } else {
            val thumbRadius = (track.width() / 2f) - 2.px.toFloat()
            val minY = track.top + thumbRadius + 2.px.toFloat()
            val maxY = track.bottom - thumbRadius - 2.px.toFloat()
            val rangeY = maxY - minY

            val tickHeight = 1.px.toFloat()
            val tickWidth = track.width() * 0.4f
            val startX = track.centerX() - tickWidth / 2f
            val endX = track.centerX() + tickWidth / 2f

            for (i in 0 until tickCount) {
                val fraction = i.toFloat() / (tickCount - 1)
                val y = minY + fraction * rangeY
                canvas.drawRect(startX, y - tickHeight / 2f, endX, y + tickHeight / 2f, tickPaint)
            }
        }
    }

    abstract fun track(): RectF

    private fun drawTrack(canvas: Canvas) {
        val track = track()
        val trackRadius = if (track.width() > track.height()) track.height() / 2f else track.width() / 2f

        if (gradientColors.isNotEmpty()) {
            trackPaint.shader = LinearGradient(
                track().width() / 2,
                0f,
                track().width() / 2,
                track.height(),
                gradientColors,
                null,
                Shader.TileMode.CLAMP
            )
        }

        // Draw round rect
        canvas.drawRoundRect(track, trackRadius, trackRadius, trackPaint)

        // Draw border
        canvas.drawRoundRect(track, trackRadius, trackRadius, trackBorderPaint)
    }

    abstract fun thumb(): Triple<Float, Float, Float>

    private fun drawThumb(canvas: Canvas) {
        val thumb = thumb()

        // Rotate canvas
        canvas.save()
        canvas.rotate(screenRotation.compensationValue.toFloat(), thumb.first, thumb.second)

        // Draw circle
        canvas.drawCircle(thumb.first, thumb.second, thumb.third, thumbPaint)

        // Draw text
        val text = textFormatter(progress)
        val textBounds = Rect().apply {
            thumbTextPaint.getTextBounds(text, 0, text.length, this)
        }
        canvas.drawText(
            text,
            thumb.first - (textBounds.width() / 2),
            thumb.second + (textBounds.height() / 2),
            thumbTextPaint
        )

        // Restore original rotation
        canvas.restore()
    }
}
