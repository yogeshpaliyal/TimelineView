package com.yogeshpaliyal.timelineview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.yogeshpaliyal.commons_utils.convertDpToPx
import com.yogeshpaliyal.commons_utils.convertSpToPx
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TimelineRuler : View, RulerFormatter {
    private var paddingTopBottom = convertToPx(20f)
    private var paddingStart = convertToPx(20f)
    private var timePaint: Paint? = null
    private var textPaint: Paint? = null
    private var lineGap = 0f
    private val smallLineWidth = convertToPx(15f)
    private val longLineWidth = convertToPx(30f)
    private val lineBoldStroke = convertToPx(2f)
    private val lineThinStroke = convertToPx(1f)
    private val textSize = convertSpToPx(14f)
    private var rulerFormatter: RulerFormatter = this
    var minutesInADay = TimeUnit.DAYS.toMinutes(1)


    fun setRulerFormatter(rulerFormatter: RulerFormatter) {
        this.rulerFormatter = rulerFormatter
        invalidate()
    }

    constructor(context: Context?, lineGap: Float) : super(context) {
        this.lineGap = lineGap
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    @SuppressLint("NewApi")
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    override fun onSizeChanged(viewWidth: Int, viewHeight: Int, oldw: Int, oldh: Int) {}
    private fun init() {
        timePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        timePaint!!.color = Color.BLACK
        timePaint!!.strokeWidth = lineBoldStroke
        textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint!!.color = Color.BLACK
        textPaint!!.textSize = textSize
    }

    fun setTopPadding(topPadding: Float) {
        paddingTopBottom = topPadding
        invalidate()
    }

    fun setPaddingStart(paddingStart: Float) {
        this.paddingStart = paddingStart
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val startPointX = paddingStart
        var startPoint = paddingTopBottom

        // total minute in a day 24*60*60 = 86400
        for (i in 0 until minutesInADay) {
            if (i % 10 == 0L) {
                timePaint!!.strokeWidth = lineBoldStroke
                canvas.drawLine(
                    startPointX,
                    startPoint,
                    startPointX + longLineWidth,
                    startPoint,
                    timePaint!!
                )
                canvas.drawText(
                    rulerFormatter.getValue(i.toInt()),
                    startPointX + longLineWidth + 10,
                    startPoint + textSize / 2,
                    textPaint!!
                )
            } else {
                timePaint!!.strokeWidth = lineThinStroke
                canvas.drawLine(
                    startPointX,
                    startPoint,
                    startPointX + smallLineWidth,
                    startPoint,
                    timePaint!!
                )
            }
            startPoint += lineGap
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = measuredWidth
        val desiredHeight = (minutesInADay * lineGap).toInt() + 2 * paddingTopBottom.toInt()
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        //Measure Width
        val width: Int = if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            widthSize
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            Math.min(desiredWidth, widthSize)
        } else {
            //Be whatever you want
            desiredWidth
        }

        //Measure Height
        val height: Int = if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            heightSize
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            Math.min(desiredHeight, heightSize)
        } else {
            //Be whatever you want
            desiredHeight
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height)
    }

    private fun convertToPx(dp: Float): Float {
        return convertDpToPx(context, dp).toFloat()
    }

    private fun convertSpToPx(sp: Float): Float {
        return convertSpToPx(context, sp).toFloat()
    }

    override fun getValue(minute: Int): String {
        return formatCalendar(TimeUnit.MINUTES.toMillis(minute.toLong()), "hh:mm aa")
    }

    private fun formatCalendar(millis: Long?, dateTimeFormat: String?): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis!!
        val simpleDateFormat = SimpleDateFormat(dateTimeFormat, Locale.ENGLISH)
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return simpleDateFormat.format(calendar.time)
    }

    companion object {
        private const val TAG = "TimelineRuler"
    }
}