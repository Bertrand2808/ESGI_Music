package com.example.esgimusic

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


class WaveView : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?,  attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    private var waveType
            : Int? = null
    private var centerLineColor: Int = Color.BLACK
    private var centerLineWidth = 1
    private var lineColor: Int = Color.GREEN
    private var lineWidth = 10
    private var lineSpace = 30
    private var values
            : MutableList<Int>? = null
    private val fullValue = 100
    private var mScale = 0f
    private var maxValue = 100000
    private var maxLineCount = 0
    private var hasOver
            = false
    private var paintCenterLine: Paint? = null
    private var paintLine: Paint? = null
    private fun init() {

        waveType = 0
        centerLineColor = Color.BLUE
        centerLineWidth =1
        lineColor =  Color.GREEN
        lineWidth = 5
        lineSpace = 5
        paintCenterLine = Paint()
        paintCenterLine!!.strokeWidth = centerLineWidth.toFloat()
        paintCenterLine!!.color = centerLineColor
        paintLine = Paint()
        paintLine!!.strokeWidth = lineWidth.toFloat()
        paintLine!!.isAntiAlias = true
        paintLine!!.color = lineColor
        maxValue=300000
    }

    fun putValue(value: Int) {
        if (value > maxValue) {
            maxValue = value
            mScale = fullValue.toFloat() / maxValue
        }
        if (values == null) {
            values = ArrayList()
        }
        values!!.add(value)
        invalidate()
    }

    fun clearValue(){
        values?.clear()
        invalidate()
    }

    private var lastX = 0
    private var moveX = 0
    private var hasBeenEnd = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> lastX = event.rawX.toInt()
            MotionEvent.ACTION_MOVE -> {
                val x = event.rawX.toInt()

                if (!hasBeenEnd || moveX > 0 && lastX - x < 0 || moveX < 0 && lastX - x > 0) {
                    moveX += ((lastX - x) * 0.7).toInt()
                    lastX = x
                    invalidate()
                }
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        val yCenter = height / 2
        if (maxLineCount == 0) {
            maxLineCount = width / (lineSpace + lineWidth)
        }
        if (waveType == WVTYPE_CENTER_LINE) {

            paintCenterLine?.let {
                canvas.drawLine(0F, yCenter.toFloat(),
                    width.toFloat(), yCenter.toFloat(),
                    it
                )
            }
        }

        if (values != null) {
            var startIndex = 0
            var startOffset = 0
            if (!hasOver || moveX == 0) {
                if (values!!.size > maxLineCount) {
                    startIndex = values!!.size - maxLineCount
                }
            } else {
                if (values!!.size > maxLineCount) {
                    startIndex = values!!.size - maxLineCount
                }
                val moveLineSize = moveX / (lineWidth + lineSpace)
                startOffset = moveX % (lineWidth + lineSpace)
                val currentIndex = startIndex + moveLineSize
                if (currentIndex < 0) {
                    startIndex = 0
                    startOffset = 0
                    hasBeenEnd = true
                } else if (currentIndex >= values!!.size) {
                    startIndex = values!!.size - 1
                    startOffset = 0
                    hasBeenEnd = true
                } else {
                    startIndex = currentIndex
                    hasBeenEnd = false
                }
            }
            for (i in startIndex until values!!.size) {
                var startX = 0
                var endX = 0
                var startY = 0
                var endY = 0
                val lineHeight = (values!![i].toFloat()  /maxValue  * height).toInt()
                when (waveType) {
                    WVTYPE_CENTER_LINE -> {
                        startX =
                            (i - startIndex) * (lineSpace + lineWidth) + lineWidth / 2 - startOffset
                        endX = startX
                        startY = (height - lineHeight) / 2
                        endY = (height - lineHeight) / 2 + lineHeight
                    }
                    WVTYPE_SINGLE -> {
                        startX =
                            (i - startIndex) * (lineSpace + lineWidth) + lineWidth / 2 - startOffset
                        endX = startX
                        startY = height - lineHeight
                        endY = height
                    }
                }
                paintLine?.let {
                    canvas.drawLine(startX.toFloat(), startY.toFloat(), endX.toFloat(),
                        endY.toFloat(), it
                    )
                }
            }
        }
    }

    companion object {
        const val WVTYPE_CENTER_LINE = 0
        const val WVTYPE_SINGLE = 1
    }
}