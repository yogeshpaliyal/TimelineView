package com.yogeshpaliyal.timelineview

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import com.yogeshpaliyal.commons_utils.convertDpToPx
import com.yogeshpaliyal.commons_utils.convertSpToPx
import com.yogeshpaliyal.commons_utils.logD
import com.yogeshpaliyal.timelineview.interfaces.TimelineAvailability
import com.yogeshpaliyal.timelineview.interfaces.TimelineBooked
import java.util.*
import java.util.concurrent.TimeUnit

class TimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.timelineViewStyle
) : FrameLayout(context, attrs, defStyleAttr) {
    var drawableAva: Drawable? = null
    var drawableBooking: Drawable? = null
    private var isAutoScrolled = false
    private var mStartClickTime: Long? = null
    private var mListener: TimelineListener? = null
    private val minutesInADay = TimeUnit.DAYS.toMinutes(1)
    private var isEditingEnable = false
    private val startPadding = convertToPx(30f).toFloat()

    @TYPE
    private var type = TYPE.BOOK_SLOT
    private var isPublishEnable = false
    private var startIndex = 50
    private val descPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mThumbPaint1 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mThumbPaint2 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mThumbTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var selectedDate = Calendar.getInstance()
    private var todaysDate = Calendar.getInstance()
    private val lineGap = convertToPx(6f).toFloat()
    private val thumbFreeArea = convertToPx(5f).toFloat()
    private val paddingTopBottom = convertToPx(20f).toFloat()
    private var minimumSteps = 5
    private var maxSteps = 0f
    private val radius = convertToPx(15f).toFloat()

    private val textSize = convertSpToPx(14f).toFloat()
    private val thumbHeight = convertToPx(30f).toFloat()
    private val thumbWidth = convertToPx(100f).toFloat()
    private val thumbRightMargin = thumbWidth + convertToPx(20f)
    private val arrThumb = ArrayList<Thumb>()
    private var arrGlobalData: List<TimelineAvailability> = ArrayList()
    private var arrAvailabilitySlots: MutableList<TimelineAvailability> = ArrayList()
    private var arrBookedSlots: MutableList<TimelineBooked> = ArrayList()
    private var arrBookedSlotsRaw: List<TimelineBooked> = ArrayList() // non filtered list
    private var selection: SelectedArea? = null
    private var currentThumbIndex = 0
    private var currentThumb: Thumb? = null
    private var isOutsideButtons = false
    private var selectedDrawable: Drawable? = null


    fun setArrAvailabilitySlots(arrAvailabilitySlots: List<TimelineAvailability>) {
        arrGlobalData = arrAvailabilitySlots
        filterTodays()
        invalidate()
    }

    fun setBookingsSlots(arrBookedSlots: List<TimelineBooked>) {
        arrBookedSlotsRaw = arrBookedSlots
        filterTodays()
        invalidate()
    }

    fun setMinimumSteps(minimumSteps: Int) {
        if (minimumSteps > 0) {
            this.minimumSteps = minimumSteps
        }
        selection = SelectedArea(selectedDrawable, startIndex, startIndex + this.minimumSteps)
    }

    fun setMaxStepsSteps(maxSteps: Float) {
        if (maxSteps > 0) {
            this.maxSteps = maxSteps
        }
    }

    fun getSelectedDate(): Calendar {
        return selectedDate
    }

    val selectedDateOnly: Calendar
        get() {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDate.timeInMillis
            calendar[Calendar.HOUR_OF_DAY] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
            return calendar
        }

    fun setSelectedDate(selectedDate: Calendar) {
        isAutoScrolled = false
        this.selectedDate = selectedDate

        mListener?.onDateChange()
        filterTodays()
        invalidate()
    }

    fun getSelectedStartTime() = selection?.startStep ?: 0
    fun getSelectedEndTime() = selection?.endStep ?: 0

    override fun onSizeChanged(viewWidth: Int, viewHeight: Int, oldw: Int, oldh: Int) {}
    fun setStartPosition(coordinate: Int) {
        val startIndex = pixelToStep(coordinate.toFloat())
        selection = SelectedArea(selectedDrawable, startIndex, startIndex + minimumSteps)
    }

    fun isEditingEnable(): Boolean {
        return isEditingEnable
    }

    fun setEditingEnable(editingEnable: Boolean) {
        isEditingEnable = editingEnable

        mListener?.onStartChange(getSelectedStartTime())
        mListener?.onEndChange(getSelectedEndTime())

        invalidate()
    }

    fun setType(@TYPE type: Int) {
        this.type = type
        invalidate()
    }

    @TYPE
    fun getType(): Int {
        return type
    }

    private var descPaintSize = convertSpToPx(14f).toFloat()
    private var thumbPaintSize = convertSpToPx(13f).toFloat()
    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        descPaint.color = Color.BLACK
        mThumbTextPaint.color = Color.WHITE
        textPaint.color = Color.BLACK
        val theme = context.theme
        val res = resources
        val a = theme.obtainStyledAttributes(attrs, R.styleable.TimelineView, defStyleAttr, 0)
        descPaintSize = a.getDimension(
            R.styleable.TimelineView_timelineDescTextSize,
            res.getDimensionPixelSize(R.dimen.default_desc_text_size).toFloat()
        )
        thumbPaintSize = a.getDimension(
            R.styleable.TimelineView_timelineThumbTextSize,
            res.getDimensionPixelSize(R.dimen.default_thumb_text_size).toFloat()
        )
        mThumbPaint1.color = a.getColor(R.styleable.TimelineView_timelineThumb1Bg, Color.BLACK)
        mThumbPaint2.color =
            a.getColor(R.styleable.TimelineView_timelineThumb2Bg, Color.BLACK)
        type = a.getInt(R.styleable.TimelineView_timelineType, TYPE.SET_AVAILABILITY)
        selectedDrawable = a.getDrawable(R.styleable.TimelineView_timelineBgSelection)
        if (selectedDrawable == null) {
            selectedDrawable =
                ResourcesCompat.getDrawable(res, R.drawable.bg_default_selected, theme)
        }
        drawableAva = a.getDrawable(R.styleable.TimelineView_timelineBgAvailability)
        if (drawableAva == null) {
            drawableAva =
                ResourcesCompat.getDrawable(res, R.drawable.bg_default_availability, theme)
        }
        drawableBooking = a.getDrawable(R.styleable.TimelineView_timelineBgBooking)
        if (drawableBooking == null) {
            drawableBooking = ResourcesCompat.getDrawable(res, R.drawable.bg_default_booking, theme)
        }
        a.recycle()
        descPaint.textSize = descPaintSize
        mThumbTextPaint.textSize = thumbPaintSize
        textPaint.textSize = textSize
        initSelectionArea()
        initThumb()
        setWillNotDraw(false)
    }

    private fun initThumb() {
        val thumb1 = Thumb()
        arrThumb.add(thumb1)
        val thumb2 = Thumb()
        arrThumb.add(thumb2)
    }

    fun getStartIndex(): Float {
        return startIndex.toFloat()
    }

    fun setStartIndex(startIndex: Int) {
        this.startIndex = startIndex
    }

    private fun initSelectionArea() {
        selection = SelectedArea(selectedDrawable, startIndex, startIndex + minimumSteps)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        val timelineRuler = TimelineRuler(context, lineGap)
        timelineRuler.setTopPadding(paddingTopBottom)
        timelineRuler.setPaddingStart(startPadding)
        addView(timelineRuler)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawAvailabilitySlots(canvas)
        if (isEditingEnable) {
            if (selectedDate.timeInMillis >= todaysDate.timeInMillis) {
                drawSelected(canvas)
                drawThumb(canvas)
            }
        }
        checkRegion()
    }

    private fun isMoreStepsThanMin(
        currentThumbIndex: Int,
        position: Float,
        minimumSteps: Float
    ): Boolean {
        var distanceBetweenThumb = 0f
        if (currentThumbIndex == 0) {
            distanceBetweenThumb = stepScaleToPixel(selection?.endStep?.toFloat()) - position
        } else if (currentThumbIndex == 1) {
            distanceBetweenThumb = position - stepScaleToPixel(selection?.startStep?.toFloat())
        }
        distanceBetweenThumb += paddingTopBottom
        return pixelToStep(distanceBetweenThumb) >= minimumSteps
    }

    private fun isMoreStepsThanMax(
        currentThumbIndex: Int,
        position: Float,
        maxSteps: Float
    ): Boolean {
        var distanceBetweenThumb = 0f
        if (maxSteps <= 0) {
            return false
        }
        if (currentThumbIndex == 0) {
            distanceBetweenThumb = stepScaleToPixel(selection?.endStep?.toFloat()) - position
        } else if (currentThumbIndex == 1) {
            distanceBetweenThumb = position - stepScaleToPixel(selection?.startStep?.toFloat())
        }
        distanceBetweenThumb += paddingTopBottom
        return pixelToStep(distanceBetweenThumb) >= maxSteps
    }

    private fun stepFromCalender(calendar: Calendar): Int {
        return calendar[Calendar.MINUTE] + calendar[Calendar.HOUR_OF_DAY] * 60
    }

    private fun isInSelectedData(
        selectedDate: Calendar,
        startCal: Calendar,
        endCal: Calendar
    ): Boolean {
        return selectedDate[Calendar.YEAR] == startCal[Calendar.YEAR] && selectedDate[Calendar.MONTH] == startCal[Calendar.MONTH] && selectedDate[Calendar.DAY_OF_MONTH] == startCal[Calendar.DAY_OF_MONTH] ||
                selectedDate[Calendar.YEAR] == endCal[Calendar.YEAR] && selectedDate[Calendar.MONTH] == endCal[Calendar.MONTH] && selectedDate[Calendar.DAY_OF_MONTH] == endCal[Calendar.DAY_OF_MONTH]
    }

    private fun filterTodays() {
        arrAvailabilitySlots = ArrayList()
        arrBookedSlots = ArrayList()
        Log.d("TimelineView", "filterTodays: global slots $arrGlobalData")
        val todayDate = selectedDateOnly
        for (arrAvailabilitySlot in arrGlobalData) {
            if (isInSelectedData(
                    todayDate,
                    arrAvailabilitySlot.getTAStartTime(),
                    arrAvailabilitySlot.getTAEndTime()
                )
            ) {
                arrAvailabilitySlots.add(arrAvailabilitySlot)
            }
        }
        for (timelineBooked in arrBookedSlotsRaw) {
            if (isInSelectedData(
                    todayDate,
                    timelineBooked.getTBStartTime(),
                    timelineBooked.getTBEndTime()
                )
            ) {
                arrBookedSlots.add(timelineBooked)
            }
        }
        Log.d("TimelineView", "filterTodays: availability slots $arrAvailabilitySlots")
        Log.d("TimelineView", "filterTodays: booked slots $arrBookedSlots")
    }

    fun getAvailabilitySelected(): TimelineAvailability? {
        return if (isPublishEnable) {
            for (arrAvailabilitySlot in arrAvailabilitySlots) {
                val startCalender = arrAvailabilitySlot.getTAStartTime()
                val endCalender = arrAvailabilitySlot.getTAEndTime()
                var stepStart: Float
                var stepEnd: Float
                if (startCalender[Calendar.DAY_OF_MONTH] < selectedDate[Calendar.DAY_OF_MONTH]) {
                    stepStart = 0f
                    stepEnd = stepFromCalender(arrAvailabilitySlot.getTAEndTime()).toFloat()
                } else if (endCalender[Calendar.DAY_OF_MONTH] > selectedDate[Calendar.DAY_OF_MONTH]) {
                    stepStart = stepFromCalender(arrAvailabilitySlot.getTAStartTime()).toFloat()
                    stepEnd = minutesInADay.toFloat()
                } else {
                    stepStart = stepFromCalender(arrAvailabilitySlot.getTAStartTime()).toFloat()
                    stepEnd = stepFromCalender(arrAvailabilitySlot.getTAEndTime()).toFloat()
                }
                if (stepStart <= selection?.startStep ?: 0 && stepEnd >= selection?.endStep ?: 0) {
                    return arrAvailabilitySlot
                }
            }
            null
        } else {
            null
        }
    }

    private fun drawAvailabilitySlots(canvas: Canvas) {
        var minStep = -1f
        for (arrAvailabilitySlot in arrAvailabilitySlots) {
            val startCalender = arrAvailabilitySlot.getTAStartTime()
            val endCalender = arrAvailabilitySlot.getTAEndTime()
            var stepStart: Float
            var stepEnd: Float
            if (startCalender[Calendar.DAY_OF_MONTH] < selectedDate[Calendar.DAY_OF_MONTH]) {
                stepStart = 0f
                stepEnd = stepFromCalender(arrAvailabilitySlot.getTAEndTime()).toFloat()
            } else if (endCalender[Calendar.DAY_OF_MONTH] > selectedDate[Calendar.DAY_OF_MONTH]) {
                stepStart = stepFromCalender(arrAvailabilitySlot.getTAStartTime()).toFloat()
                stepEnd = minutesInADay.toFloat()
            } else {
                stepStart = stepFromCalender(arrAvailabilitySlot.getTAStartTime()).toFloat()
                stepEnd = stepFromCalender(arrAvailabilitySlot.getTAEndTime()).toFloat()
            }
            if (minStep == -1f) {
                minStep = stepStart
            }
            if (minStep > stepStart) {
                minStep = stepStart
            }


            // float stepStart = stepFromCalender(arrAvailabilitySlot.getStartTime());
            // float stepEnd = stepFromCalender(arrAvailabilitySlot.getEndTime());
            val y1 = stepScaleToPixel(stepStart)
            val y2 = stepScaleToPixel(stepEnd)
            drawableAva?.setBounds(0, y1.toInt(), measuredWidth, y2.toInt())
            drawableAva?.draw(canvas)
            if (!isEditingEnable && arrAvailabilitySlot.getTAStartTime().timeInMillis >= todaysDate.timeInMillis) {
                val halfThumbWidth = thumbWidth / 2
                val halfThumbHeight = thumbHeight / 2
                val left = measuredWidth - thumbRightMargin
                val top = (y2 - y1) / 2 - halfThumbHeight + y1
                if (type == TYPE.SET_AVAILABILITY) {


                    // TODO Cancel button setup
                    /*Thumb thumb = new Thumb(null);
                    thumb.setPosition(top);
                    arrAvailabilitySlot.setCancelButton(thumb);*/
                    canvas.drawRoundRect(
                        RectF(left, top, left + thumbWidth, top + thumbHeight),
                        radius,
                        radius,
                        mThumbPaint2
                    )
                    val text = "Cancel"
                    val rect = Rect()
                    mThumbTextPaint.getTextBounds(text, 0, text.length, rect)
                    val halfTextWidth = rect.width() / 2
                    val textx = halfThumbWidth - halfTextWidth + left
                    val yPos =
                        (thumbHeight / 2 - (textPaint.descent() + textPaint.ascent()) / 2).toInt()
                    canvas.drawText(text, textx, yPos + top, mThumbTextPaint)
                }
                val desc1 = getTimeFromStep(startCalender, endCalender)
                val bounds = Rect()
                descPaint.getTextBounds(desc1, 0, desc1.length, bounds)
                val height = bounds.height()
                val desc2 = " USD/1 Min"
                // LogHelper.logD("TestingNull", "Price => " + arrAvailabilitySlot.getPrice());
                val bounds1 = Rect()
                descPaint.getTextBounds(desc2, 0, desc2.length, bounds1)
                val height1 = bounds1.height()
                canvas.drawText(
                    desc1,
                    halfThumbWidth - bounds.width() / 2 + left,
                    top - height - height1 - 10,
                    descPaint
                )
                canvas.drawText(
                    desc2,
                    halfThumbWidth - bounds1.width() / 2 + left,
                    top - height1,
                    descPaint
                )
            }
        }
        for (arrBookedSlot in arrBookedSlots) {
            val by1 = stepScaleToPixel(stepFromCalender(arrBookedSlot.getTBStartTime()).toFloat())
            val by2 = stepScaleToPixel(stepFromCalender(arrBookedSlot.getTBEndTime()).toFloat())
            drawableBooking?.setBounds(
                startPadding.toInt(),
                by1.toInt(),
                measuredWidth,
                by2.toInt()
            )
            drawableBooking?.draw(canvas)
        }
        if (!isAutoScrolled) {
            if (selectedDate[Calendar.YEAR] == todaysDate[Calendar.YEAR] && selectedDate[Calendar.MONTH] == todaysDate[Calendar.MONTH] && selectedDate[Calendar.DAY_OF_MONTH] == todaysDate[Calendar.DAY_OF_MONTH]) {
                // today only
                val s = getStepAfterCurrentTime()
                if (s != -1) {
                    TAG.logD("scroll to after time")
                    mListener?.parentScrollTo(s)
                } else {
                    val todaysMinutes =
                        todaysDate[Calendar.HOUR_OF_DAY] * 60 + todaysDate[Calendar.MINUTE]
                    mListener?.parentScrollTo(stepScaleToPixel(todaysMinutes.toFloat()).toInt())
                    TAG.logD("scroll to current time")
                }
            } else {
                if (minStep > 5) {
                    mListener?.parentScrollTo(stepScaleToPixel(minStep - 5).toInt())
                } else {
                    mListener?.parentScrollTo(0)
                }
            }
            isAutoScrolled = true
        }
    }

    /// in between booking slot
    private fun getStepAfterCurrentTime(): Int {
        var step = -1
        for (arrAvailabilitySlot in arrAvailabilitySlots) {
            if (arrAvailabilitySlot.getTAStartTime().timeInMillis < todaysDate.timeInMillis
                && arrAvailabilitySlot.getTAEndTime().timeInMillis > todaysDate.timeInMillis
            ) {
                /// in between booking slot
                break
            } else if (arrAvailabilitySlot.getTAStartTime().timeInMillis > todaysDate.timeInMillis) {
                TAG.logD("Start time more than current time")
                val todaysMinutes =
                    arrAvailabilitySlot.getTAStartTime()[Calendar.HOUR_OF_DAY] * 60 + arrAvailabilitySlot.getTAStartTime()[Calendar.MINUTE]
                step = stepScaleToPixel(todaysMinutes.toFloat()).toInt()
                break
            }
        }
        return step
    }


    private fun getTimeFromStep(): String {
        val endStep = selection?.endStep ?: 0
        val startStep = selection?.startStep ?: 0

        val minutes = (endStep.minus(startStep)).toFloat()
        return "$minutes Minutes"
    }


    private fun getTimeFromStep(calendar1: Calendar, calendar2: Calendar): String {
        val diff = calendar2.timeInMillis - calendar1.timeInMillis
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = (minutes / 60).toInt()
        return if (hours <= 0) {
            "$minutes Minutes"
        } else {
            val min = minutes - hours * 60
            val hrs = "$hours Hours"
            if (min > 0) {
                hrs + ", " + (minutes - hours * 60) + " Minutes"
            } else hrs
        }
    }

    private fun drawSelected(canvas: Canvas) {
        val y1 = stepScaleToPixel(selection?.startStep?.toFloat())
        val y2 = stepScaleToPixel(selection?.endStep?.toFloat())
        Log.d("TimelineView", "drawSelected: y1=>$y1; y2 =>$y2")
        val right = measuredWidth - thumbRightMargin + thumbWidth / 8
        selection?.drawable?.setBounds(startPadding.toInt(), y1.toInt(), right.toInt(), y2.toInt())
        selection?.drawable?.draw(canvas)
        val text = getTimeFromStep()
        val rect = Rect()
        textPaint.getTextBounds(text, 0, text.length, rect)
        val textx = thumbWidth / 2 - rect.width() / 2
        val yPos = ((y2 - y1) / 2 - (textPaint.descent() + textPaint.ascent()) / 2)
        canvas.drawText(text, textx + right, yPos + y1, textPaint)
    }

    private fun convertToPx(dp: Float): Int {
        return convertDpToPx(context, dp)
    }

    private fun convertSpToPx(sp: Float): Int {
        return convertSpToPx(context, sp)
    }

    private fun drawThumb(canvas: Canvas) {
        for (i in arrThumb.indices) {
            // if (arrThumb.get(i).getDrawable() != null) {
            val dx = measuredWidth - thumbRightMargin
            var dy = 0f
            var text = ""
            if (i == 0) {
                dy = stepScaleToPixel(selection?.startStep?.toFloat()) - thumbHeight / 2
                canvas.drawRoundRect(
                    RectF(dx, dy, dx + thumbWidth, dy + thumbHeight),
                    radius,
                    radius,
                    mThumbPaint1
                )
                text = "Start"
            } else {
                dy = stepScaleToPixel(selection?.endStep?.toFloat()) - thumbHeight / 2
                canvas.drawRoundRect(
                    RectF(dx, dy, dx + thumbWidth, dy + thumbHeight),
                    radius,
                    radius,
                    mThumbPaint2
                )
                text = "Stop"
            }
            val rect = Rect()
            mThumbTextPaint.getTextBounds(text, 0, text.length, rect)
            val textx = thumbWidth / 2 - rect.width() / 2
            val yPos =
                (thumbHeight / 2 - (mThumbTextPaint.descent() + mThumbTextPaint.ascent()) / 2).toInt()
            canvas.drawText(text, textx + dx, yPos + dy, mThumbTextPaint)
            // }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        var coordinate = event.y
        val action = event.action
        val coordinateX = event.x


        // Find thumb closest to event coordinate on screen touch
        if (action == MotionEvent.ACTION_DOWN) {
            mStartClickTime = Calendar.getInstance().timeInMillis
            isOutsideButtons = false
            if (coordinateX < measuredWidth - thumbRightMargin || coordinateX > measuredWidth - thumbRightMargin + thumbWidth) {
                isOutsideButtons = true
                return true
            }
            if (isEditingEnable && selectedDate.timeInMillis >= todaysDate.timeInMillis) {
                currentThumbIndex = getClosestThumbIndex(coordinate)
                if (currentThumbIndex == -1) {
                    return false
                }
                currentThumb = getThumbAt(currentThumbIndex)
                val state =
                    intArrayOf(android.R.attr.state_window_focused, android.R.attr.state_pressed)
                currentThumb?.drawable?.state = state
                parent.requestDisallowInterceptTouchEvent(true)
            } else {
                // TODO Cancel button click
                /* for (TimelineAvailability availabilitySlot : arrAvailabilitySlots) {
                    if (availabilitySlot.getCancelButton() != null) {
                        if (coordinate > availabilitySlot.getCancelButton().getPosition()
                                && coordinate < availabilitySlot.getCancelButton().getPosition() + thumbHeight) {
                            mListener.onCancelClicked(availabilitySlot);
                            break;
                        }
                    }
                }*/
            }
        }
        if (action == MotionEvent.ACTION_MOVE) {
            mStartClickTime = null
        }
        if (action == MotionEvent.ACTION_UP) {

            val state = intArrayOf()
            currentThumb?.drawable?.state = state



            mStartClickTime?.let { mStartClickTime ->
                val clickDuration = Calendar.getInstance().timeInMillis - mStartClickTime
                if (clickDuration >= MAX_CLICK_DURATION) {
                    /**
                     * TimelineView long clicked, Move selection at user long clicked
                     */
                    val step = pixelToStep(asStep(coordinate))
                    selection?.startStep = step
                    selection?.endStep = step + minimumSteps
                }
            }

        }
        if (isEditingEnable && selectedDate.timeInMillis >= todaysDate.timeInMillis && !isOutsideButtons) {
            coordinate = asStep(coordinate)
            val step = pixelToStep(coordinate).toFloat()
            if (step >= 0 && step < minutesInADay) {
                val maxSteps = 0
                val minimumSteps = 1
                if (isMoreStepsThanMax(currentThumbIndex, coordinate, maxSteps.toFloat())) {
                    TAG.logD("More than max")
                    if (currentThumbIndex == 0) {
                        selection?.startStep = selection?.endStep?.minus(maxSteps) ?: 0
                        // LogHelper.logD(TAG, "Start step " + (selection.getEndStep() - maxSteps));
                    } else {
                        selection?.endStep = selection?.startStep?.plus(maxSteps) ?: 0
                        // LogHelper.logD(TAG, "End step " + (selection.getStartStep() + maxSteps));
                    }
                } else if (isMoreStepsThanMin(
                        currentThumbIndex,
                        coordinate,
                        minimumSteps.toFloat()
                    )
                ) {
                    "More than min".logD(TAG)
                    if (currentThumbIndex == 0) {
                        selection?.startStep = pixelToStep(coordinate)
                    } else {
                        selection?.endStep = pixelToStep(coordinate)
                    }
                } else {
                    TAG.logD("Else case")
                    if (currentThumbIndex == 0) {
                        selection?.startStep = selection?.endStep?.minus(minimumSteps) ?: 0
                        // LogHelper.logD(TAG, "Start step " + (selection.getEndStep() - minimumSteps));
                    } else {
                        selection?.endStep = selection?.startStep?.plus(minimumSteps) ?: 0
                        //LogHelper.logD(TAG, "End step " + (selection.getStartStep() + minimumSteps));
                    }
                }

                if (currentThumbIndex == 0) {
                    // start
                    // mListener.onStartChange(DateTimeHelper.formatCalendar(selection.getStartStep()));
                } else {
                    // end
                    //mListener.onEndChange(DateTimeHelper.convertDateStrIntoCalendar(arrTimes.get((int) selection.getEndStep()), TIME_FORMAT));
                }

            }
        }
        invalidate()
        return true
    }

    private fun checkRegion() {
        var isLocalPublishEnable = false
        if (type == TYPE.SET_AVAILABILITY) {
            isLocalPublishEnable = true
        }
        if (!isEditingEnable) {
            isLocalPublishEnable = false
        } else {
            val selectedStartStep = selection?.startStep?.toFloat() ?: 0f
            val selectedEndStep = selection?.endStep?.toFloat() ?: 0f
            for (availabilitySlot in arrAvailabilitySlots) {
                val startCalender = availabilitySlot.getTAStartTime()
                val endCalender = availabilitySlot.getTAEndTime()
                var startStep = 0f
                var endStep = 0f
                if (startCalender[Calendar.DAY_OF_MONTH] < selectedDate[Calendar.DAY_OF_MONTH]) {
                    startStep = 0f
                    endStep = stepFromCalender(availabilitySlot.getTAEndTime()).toFloat()
                } else if (endCalender[Calendar.DAY_OF_MONTH] > selectedDate[Calendar.DAY_OF_MONTH]) {
                    startStep = stepFromCalender(availabilitySlot.getTAStartTime()).toFloat()
                    endStep = minutesInADay.toFloat()
                } else {
                    startStep = stepFromCalender(availabilitySlot.getTAStartTime()).toFloat()
                    endStep = stepFromCalender(availabilitySlot.getTAEndTime()).toFloat()
                }
                if (type == TYPE.SET_AVAILABILITY) {
                    if (selectedStartStep <= startStep && selectedEndStep <= startStep) {
                        /// both points above the availability
                        if (!isLocalPublishEnable) {
                            isLocalPublishEnable = true
                        }
                    } else if (selectedStartStep >= endStep && selectedEndStep >= endStep) {
                        /// both points below the availability
                        if (!isLocalPublishEnable) {
                            isLocalPublishEnable = true
                        }
                    } else {
                        if (isLocalPublishEnable) {
                            isLocalPublishEnable = false
                        }
                        break
                    }
                } else {
                    if (selectedStartStep >= startStep && selectedEndStep >= startStep &&
                        selectedStartStep <= endStep && selectedEndStep <= endStep
                    ) {
                        /// both points inside the availability
                        isLocalPublishEnable = true
                    }
                }
            }
            if (type == TYPE.BOOK_SLOT && isLocalPublishEnable) {
                for (arrBookedSlot in arrBookedSlots) {
                    val startStepBookedSlot =
                        stepFromCalender(arrBookedSlot.getTBStartTime()).toFloat()
                    val endStepBookedSlot = stepFromCalender(arrBookedSlot.getTBEndTime()).toFloat()
                    if (selectedStartStep <= startStepBookedSlot && selectedEndStep <= startStepBookedSlot) {
                        /// both points above the availability
                        if (!isLocalPublishEnable) {
                            isLocalPublishEnable = true
                        }
                    } else if (selectedStartStep >= endStepBookedSlot && selectedEndStep >= endStepBookedSlot) {
                        /// both points below the availability
                        if (!isLocalPublishEnable) {
                            isLocalPublishEnable = true
                        }
                    } else {
                        if (isLocalPublishEnable) {
                            isLocalPublishEnable = false
                        }
                        break
                    }
                }
            }
        }
        if (selectedDate.timeInMillis < todaysDate.timeInMillis) {
            isLocalPublishEnable = false
        } else {
            if (selectedDate[Calendar.YEAR] == todaysDate[Calendar.YEAR] && selectedDate[Calendar.MONTH] == todaysDate[Calendar.MONTH] && selectedDate[Calendar.DAY_OF_MONTH] == todaysDate[Calendar.DAY_OF_MONTH]) {
                // today only
                val todaysMinutes =
                    todaysDate[Calendar.HOUR_OF_DAY] * 60 + todaysDate[Calendar.MINUTE]
                if (selection?.startStep ?: 0 < todaysMinutes) {
                    isLocalPublishEnable = false
                }
            }
        }
        val diff = (selection?.endStep?.minus(selection?.startStep ?: 0))?.toFloat() ?: 0f
        if ((diff < minimumSteps || diff > maxSteps) && isLocalPublishEnable && type != TYPE.SET_AVAILABILITY) {
            isLocalPublishEnable = false
        }
        if (isLocalPublishEnable != isPublishEnable) {
            isPublishEnable = isLocalPublishEnable
            mListener?.onPublishStatusChange(isPublishEnable)
            if (isLocalPublishEnable) {
                TAG.logD("Publish Enable")
            } else {
                TAG.logD("Publish Disable")
            }
        }
    }

    fun setmListener(mListener: TimelineListener) {
        this.mListener = mListener
        mListener.onStartChange(getSelectedStartTime())
        mListener.onEndChange(getSelectedEndTime())
    }

    private fun asStep(pixelValue: Float): Float {
        return stepScaleToPixel(pixelToStep(pixelValue).toFloat())
    }

    private fun stepScaleToPixel(stepScaleValue: Float?): Float {
        val gap = lineGap
        val pixelValue = stepScaleValue?.times(gap)
        return pixelValue?.plus(paddingTopBottom) ?: 0f
    }

    private fun pixelToStep(pixelValue: Float): Int {
        val gap = lineGap
        val stepScaleValue = Math.round((pixelValue - paddingTopBottom) / gap).toFloat()
        return if (stepScaleValue < 0) {
            0
        } else if (stepScaleValue > minutesInADay) {
            minutesInADay.toInt()
        } else {
            Math.round(stepScaleValue)
        }
    }

    fun getThumbAt(index: Int): Thumb {
        return arrThumb[index]
    }

    private fun getClosestThumbIndex(coordinate: Float): Int {
        var closest = -1
        if (!arrThumb.isEmpty()) {
            var shortestDistance = thumbFreeArea + thumbWidth / 2 + paddingTop + paddingBottom
            // Oldschool for-loop to have access to index
            for (i in arrThumb.indices) {
                // Find thumb closest to x coordinate
                if (i == 0) {
                    val tcoordinate = stepScaleToPixel(selection?.startStep?.toFloat())
                    val distance = Math.abs(coordinate - tcoordinate)
                    if (distance <= shortestDistance) {
                        shortestDistance = distance
                        closest = i
                    }
                } else {
                    val tcoordinate = stepScaleToPixel(selection?.endStep?.toFloat())
                    val distance = Math.abs(coordinate - tcoordinate)
                    if (distance <= shortestDistance) {
                        shortestDistance = distance
                        closest = i
                    }
                }
            }
        }
        return closest
    }

    companion object {
        private const val MAX_CLICK_DURATION = 1000
        private const val TAG = "TimelineFrameLAyout"
    }

    init {
        init(context, attrs, defStyleAttr)
    }
}