package com.yogeshpaliyal.timelineview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.core.content.res.ResourcesCompat;

import com.yogeshpaliyal.commons_utils.DateTimeHelper;
import com.yogeshpaliyal.commons_utils.DisplayHelper;
import com.yogeshpaliyal.commons_utils.LogHelper;
import com.yogeshpaliyal.timelineview.interfaces.TimelineAvailability;
import com.yogeshpaliyal.timelineview.interfaces.TimelineBooked;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimelineView extends FrameLayout {

    private static final int MAX_CLICK_DURATION = 1000;
    private static final String TAG = "TimelineFrameLAyout";

    Drawable drawableAva;
    Drawable drawableBooking;

    private boolean isAutoScrolled;

    private Long mStartClickTime;

    private TimelineListener mListener;

    private ArrayList<String> arrTimes = new ArrayList<>();

    private boolean isEditingEnable = false;
    private String TIME_FORMAT = "hh:mm a";
    private float startPadding = convertToPx(30);
    private TYPE type = TYPE.BOOK_SLOT;
    private boolean isPublishEnable = false;
    private float startIndex = 50;
    private Paint descPaint, textPaint, mThumbPaint1, mThumbPaint2, mThumbTextPaint;
    private Calendar currentCalender, selectedDate = Calendar.getInstance();
    private Calendar todaysDate = Calendar.getInstance();
    private float lineGap = convertToPx(6);
    private float thumbFreeArea = convertToPx(5);
    private float paddingTopBottom = convertToPx(20);
    private float minimumSteps = 5;
    private float maxSteps = 0;
    private float radius = convertToPx(15);
    private float lineHeightBold = convertToPx(1);
    private float textSize = convertSpToPx(14);
    private float thumbHeight = convertToPx(30);
    private float thumbWidth = convertToPx(100);
    private float thumbRightMargin = thumbWidth + convertToPx(20);
    private ArrayList<Thumb> arrThumb = new ArrayList<>();
    private List<TimelineAvailability> arrGlobalData = new ArrayList();
    private List<TimelineAvailability> arrAvailabilitySlots = new ArrayList();
    private List<TimelineBooked> arrBookedSlots = new ArrayList();
    private List<TimelineBooked> arrBookedSlotsRaw = new ArrayList(); // non filtered list
    private SelectedArea selection;
    private int currentThumbIndex = 0;
    private Thumb currentThumb = null;
    private boolean isOutsideButtons = false;

    private Drawable selectedDrawable;


    public TimelineView(Context context) {
        this(context, null);
        //init(cont);
    }

    public TimelineView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.timelineViewStyle);
    }

    public TimelineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public void setArrAvailabilitySlots(List<TimelineAvailability> arrAvailabilitySlots) {
        this.arrGlobalData = arrAvailabilitySlots;
        filterTodays();
        invalidate();
    }

    public void setBookingsSlots(List<TimelineBooked> arrBookedSlots) {
        this.arrBookedSlotsRaw = arrBookedSlots;
        filterTodays();
        invalidate();
    }

    public void setMinimumSteps(float minimumSteps) {
        if (minimumSteps > 0) {
            this.minimumSteps = minimumSteps;
        }
        selection = new SelectedArea(selectedDrawable, startIndex, startIndex + this.minimumSteps);
    }

    public void setMaxStepsSteps(float maxSteps) {
        if (maxSteps > 0) {
            this.maxSteps = maxSteps;
        }
    }

    public Calendar getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Calendar selectedDate) {
        isAutoScrolled = false;
        this.selectedDate = selectedDate;
        if (mListener != null) {
            mListener.onDateChange();
        }
        filterTodays();
        invalidate();
    }

    public Calendar getTodaysDate() {
        return todaysDate;
    }

    public void setTodaysDate(Calendar todaysDate) {
        this.todaysDate = todaysDate;
    }

    public Calendar getSelectedStartTime() {
        return DateTimeHelper.convertDateStrIntoCalendar(arrTimes.get((int) selection.getStartStep()), TIME_FORMAT);
    }

    public Calendar getSelectedStartDateTime() {
        return DateTimeHelper.getCalendar(selectedDate.getTimeInMillis()+DateTimeHelper.convertDateStrIntoCalendar(arrTimes.get((int) selection.getStartStep()), TIME_FORMAT).getTimeInMillis(),false, false,false,false);
    }

    public Calendar getSelectedEndTime() {
        return DateTimeHelper.convertDateStrIntoCalendar(arrTimes.get((int) selection.getEndStep()), TIME_FORMAT);
    }

    public Calendar getSelectedEndDateTime() {
        return DateTimeHelper.getCalendar(selectedDate.getTimeInMillis()+DateTimeHelper.convertDateStrIntoCalendar(arrTimes.get((int) selection.getEndStep()), TIME_FORMAT).getTimeInMillis(),false, false,false,false);
    }

    @Override
    protected void onSizeChanged(int viewWidth, int viewHeight, int oldw, int oldh) {

    }

    public void setStartPosition(int coordinate) {
        float startIndex = pixelToStep(coordinate);
        selection = new SelectedArea(selectedDrawable, startIndex, startIndex + this.minimumSteps);
    }

    public boolean isEditingEnable() {
        return isEditingEnable;
    }

    public void setEditingEnable(boolean editingEnable) {
        isEditingEnable = editingEnable;
        if (mListener != null) {
            mListener.onStartChange(getSelectedStartTime());
            mListener.onEndChange(getSelectedEndTime());
        }
        invalidate();

    }

    public void setType(TYPE type) {
        this.type = type;
        invalidate();
    }

    public TYPE getType() {
        return this.type;
    }

    private float descPaintSize = convertSpToPx(14);
    private float thumbPaintSize = convertSpToPx(13);

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        currentCalender = Calendar.getInstance();
        currentCalender.set(Calendar.AM_PM, Calendar.AM);
        currentCalender.set(Calendar.MINUTE, 0);
        currentCalender.set(Calendar.HOUR_OF_DAY, 0);

        descPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        descPaint.setColor(Color.BLACK);

        mThumbTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mThumbTextPaint.setColor(Color.WHITE);

        fillArrayTimes();

        mThumbPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);

        mThumbPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);

        final Resources.Theme theme = context.getTheme();
        final Resources res = getResources();

        TypedArray a = theme.obtainStyledAttributes(attrs, R.styleable.TimelineView, defStyleAttr, 0);
        descPaintSize = a.getDimension(R.styleable.TimelineView_timelineDescTextSize, res.getDimensionPixelSize(R.dimen.default_desc_text_size));
        thumbPaintSize = a.getDimension(R.styleable.TimelineView_timelineThumbTextSize, res.getDimensionPixelSize(R.dimen.default_thumb_text_size));

        mThumbPaint1.setColor(a.getColor(R.styleable.TimelineView_timelineThumb1Bg, Color.BLACK));

        mThumbPaint2.setColor(a.getColor(R.styleable.TimelineView_timelineThumb2Bg, Color.BLACK));

        selectedDrawable = a.getDrawable(R.styleable.TimelineView_timelineBgSelection);

        if (selectedDrawable == null) {
            selectedDrawable = ResourcesCompat.getDrawable(res, R.drawable.bg_default_selected, theme);
        }

        drawableAva = a.getDrawable(R.styleable.TimelineView_timelineBgAvailability);
        if (drawableAva == null) {
            drawableAva = ResourcesCompat.getDrawable(res, R.drawable.bg_default_availability, theme);
        }


        drawableBooking = a.getDrawable(R.styleable.TimelineView_timelineBgBooking);
        if (drawableBooking == null) {
            drawableBooking = ResourcesCompat.getDrawable(res, R.drawable.bg_default_booking, theme);
        }
        a.recycle();


        descPaint.setTextSize(descPaintSize);

        mThumbTextPaint.setTextSize(thumbPaintSize);

        textPaint.setTextSize(textSize);

        currentCalender = Calendar.getInstance();
        currentCalender.set(Calendar.AM_PM, Calendar.AM);
        currentCalender.set(Calendar.MINUTE, 0);
        currentCalender.set(Calendar.HOUR_OF_DAY, 0);


        initSelectionArea();
        initThumb();

        this.setWillNotDraw(false);
    }

    private void initThumb() {
        Thumb thumb1 = new Thumb();
        arrThumb.add(thumb1);

        Thumb thumb2 = new Thumb();
        arrThumb.add(thumb2);
    }

    public float getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(float startIndex) {
        this.startIndex = startIndex;
    }

    private void initSelectionArea() {
        selection = new SelectedArea(selectedDrawable, startIndex, startIndex + minimumSteps);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        TimelineRuler timelineRuler = new TimelineRuler(getContext(), lineGap, arrTimes);
        timelineRuler.setTopPadding(paddingTopBottom);
        timelineRuler.setPaddingStart(startPadding);
        addView(timelineRuler);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawAvailabilitySlots(canvas);


        if (isEditingEnable) {
            if (selectedDate.getTimeInMillis() >= todaysDate.getTimeInMillis()) {
                drawSelected(canvas);
                drawThumb(canvas);
            }
        }

        checkRegion();
    }

    private boolean isMoreStepsThanMin(int currentThumbIndex, float position, float minimumSteps) {
        float distanceBetweenThumb = 0;
        if (currentThumbIndex == 0) {
            distanceBetweenThumb = stepScaleToPixel(selection.getEndStep()) - position;
        } else if (currentThumbIndex == 1) {
            distanceBetweenThumb = position - stepScaleToPixel(selection.getStartStep());
        }
        distanceBetweenThumb += paddingTopBottom;
        return pixelToStep(distanceBetweenThumb) >= minimumSteps;
    }

    private boolean isMoreStepsThanMax(int currentThumbIndex, float position, float maxSteps) {
        float distanceBetweenThumb = 0;

        if (maxSteps <= 0) {
            return false;
        }

        if (currentThumbIndex == 0) {
            distanceBetweenThumb = stepScaleToPixel(selection.getEndStep()) - position;
        } else if (currentThumbIndex == 1) {
            distanceBetweenThumb = position - stepScaleToPixel(selection.getStartStep());
        }
        distanceBetweenThumb += paddingTopBottom;
        return pixelToStep(distanceBetweenThumb) >= maxSteps;
    }

    private int stepFromCalender(Calendar calendar) {
        return calendar.get(Calendar.MINUTE) + (calendar.get(Calendar.HOUR_OF_DAY) * 60);
    }

    private Boolean isInSelectedData(Calendar selectedDate, Calendar startCal, Calendar endCal) {
        return (selectedDate.get(Calendar.YEAR) == startCal.get(Calendar.YEAR) &&
                selectedDate.get(Calendar.MONTH) == startCal.get(Calendar.MONTH) &&
                selectedDate.get(Calendar.DAY_OF_MONTH) == startCal.get(Calendar.DAY_OF_MONTH)) ||
                (selectedDate.get(Calendar.YEAR) == endCal.get(Calendar.YEAR) &&
                        selectedDate.get(Calendar.MONTH) == endCal.get(Calendar.MONTH) &&
                        selectedDate.get(Calendar.DAY_OF_MONTH) == endCal.get(Calendar.DAY_OF_MONTH));
    }

    private void filterTodays() {
        arrAvailabilitySlots = new ArrayList<>();
        for (TimelineAvailability arrAvailabilitySlot : this.arrGlobalData) {
            if (isInSelectedData(selectedDate, arrAvailabilitySlot.getTAStartTime(), arrAvailabilitySlot.getTAEndTime())) {
                arrAvailabilitySlots.add(arrAvailabilitySlot);
            }
        }
        for (TimelineBooked timelineBooked : arrBookedSlotsRaw) {
            if (isInSelectedData(selectedDate, timelineBooked.getTBStartTime(), timelineBooked.getTBEndTime())) {
                arrBookedSlotsRaw.add(timelineBooked);
            }
        }
    }

    public TimelineAvailability getAvailabilitySelected() {
        if (isPublishEnable) {
            for (TimelineAvailability arrAvailabilitySlot : arrAvailabilitySlots) {
                Calendar startCalender = arrAvailabilitySlot.getTAStartTime();
                Calendar endCalender = arrAvailabilitySlot.getTAEndTime();
                float stepStart;
                float stepEnd;

                if (startCalender.get(Calendar.DAY_OF_MONTH) < selectedDate.get(Calendar.DAY_OF_MONTH)) {
                    stepStart = 0;
                    stepEnd = stepFromCalender(arrAvailabilitySlot.getTAEndTime());
                } else if (endCalender.get(Calendar.DAY_OF_MONTH) > selectedDate.get(Calendar.DAY_OF_MONTH)) {
                    stepStart = stepFromCalender(arrAvailabilitySlot.getTAStartTime());
                    stepEnd = arrTimes.size();
                } else {
                    stepStart = stepFromCalender(arrAvailabilitySlot.getTAStartTime());
                    stepEnd = stepFromCalender(arrAvailabilitySlot.getTAEndTime());
                }


                if (stepStart <= selection.getStartStep() && stepEnd >= selection.getEndStep()) {
                    return arrAvailabilitySlot;
                }

            }
            return null;
        } else {
            return null;
        }
    }

    @SuppressLint("NewApi")
    private void drawAvailabilitySlots(Canvas canvas) {


        float minStep = -1;

        for (TimelineAvailability arrAvailabilitySlot : this.arrAvailabilitySlots) {

            Calendar startCalender = arrAvailabilitySlot.getTAStartTime();
            Calendar endCalender = arrAvailabilitySlot.getTAEndTime();
            float stepStart;
            float stepEnd;

            if (startCalender.get(Calendar.DAY_OF_MONTH) < selectedDate.get(Calendar.DAY_OF_MONTH)) {
                stepStart = 0;
                stepEnd = stepFromCalender(arrAvailabilitySlot.getTAEndTime());
            } else if (endCalender.get(Calendar.DAY_OF_MONTH) > selectedDate.get(Calendar.DAY_OF_MONTH)) {
                stepStart = stepFromCalender(arrAvailabilitySlot.getTAStartTime());
                stepEnd = arrTimes.size();
            } else {
                stepStart = stepFromCalender(arrAvailabilitySlot.getTAStartTime());
                stepEnd = stepFromCalender(arrAvailabilitySlot.getTAEndTime());
            }

            if (minStep == -1) {
                minStep = stepStart;
            }

            if (minStep > stepStart) {
                minStep = stepStart;
            }


            // float stepStart = stepFromCalender(arrAvailabilitySlot.getStartTime());
            // float stepEnd = stepFromCalender(arrAvailabilitySlot.getEndTime());

            float y1 = stepScaleToPixel(stepStart);
            float y2 = stepScaleToPixel(stepEnd);

            drawableAva.setBounds(0, (int) y1, getMeasuredWidth(), (int) y2);
            drawableAva.draw(canvas);


            if (!isEditingEnable && arrAvailabilitySlot.getTAStartTime().getTimeInMillis() >= todaysDate.getTimeInMillis()) {
                float halfThumbWidth = (thumbWidth / 2);
                float halfThumbHeight = (thumbHeight / 2);


                float left = getMeasuredWidth() - thumbRightMargin;
                float top = (((y2 - y1) / 2) - halfThumbHeight) + y1;
                if (type == TYPE.SET_AVAILABILITY) {


                    // TODO Cancel button setup
                    /*Thumb thumb = new Thumb(null);
                    thumb.setPosition(top);
                    arrAvailabilitySlot.setCancelButton(thumb);*/

                    canvas.drawRoundRect(new RectF(left, top, left + thumbWidth, top + thumbHeight), radius, radius, mThumbPaint2);

                    String text = "Cancel";

                    Rect rect = new Rect();
                    mThumbTextPaint.getTextBounds(text, 0, text.length(), rect);

                    int halfTextWidth = rect.width() / 2;

                    float textx = halfThumbWidth - (halfTextWidth) + left;
                    int yPos = (int) ((thumbHeight / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));


                    canvas.drawText(text, textx, yPos + top, mThumbTextPaint);
                }


                String desc1 = getTimeFromStep(startCalender, endCalender);
                Rect bounds = new Rect();
                descPaint.getTextBounds(desc1, 0, desc1.length(), bounds);
                int height = bounds.height();

                String desc2 = " USD/1 Min";
                // LogHelper.logD("TestingNull", "Price => " + arrAvailabilitySlot.getPrice());
                Rect bounds1 = new Rect();
                descPaint.getTextBounds(desc2, 0, desc2.length(), bounds1);
                int height1 = bounds1.height();


                canvas.drawText(desc1, halfThumbWidth - (bounds.width() / 2) + left, top - height - height1 - 10, descPaint);

                canvas.drawText(desc2, halfThumbWidth - (bounds1.width() / 2) + left, top - height1, descPaint);

            }
        }

        for (TimelineBooked arrBookedSlot : arrBookedSlots) {
            float by1 = stepScaleToPixel(stepFromCalender(arrBookedSlot.getTBStartTime()));
            float by2 = stepScaleToPixel(stepFromCalender(arrBookedSlot.getTBEndTime()));
            drawableBooking.setBounds((int) startPadding, (int) by1, getMeasuredWidth(), (int) by2);
            drawableBooking.draw(canvas);
        }

        if (!isAutoScrolled) {

            if (selectedDate.get(Calendar.YEAR) == todaysDate.get(Calendar.YEAR) &&
                    selectedDate.get(Calendar.MONTH) == todaysDate.get(Calendar.MONTH) &&
                    selectedDate.get(Calendar.DAY_OF_MONTH) == todaysDate.get(Calendar.DAY_OF_MONTH)) {
                // today only

                int s = getStepAfterCurrentTime();
                if (s != -1) {
                    LogHelper.logD(TAG, "scroll to after time");
                    if (mListener != null)
                        mListener.parentScrollTo(s);
                } else {
                    int todaysMinutes = todaysDate.get(Calendar.HOUR_OF_DAY) * 60 + todaysDate.get(Calendar.MINUTE);
                    if (mListener != null)
                        mListener.parentScrollTo((int) stepScaleToPixel(todaysMinutes));
                    LogHelper.logD(TAG, "scroll to current time");

                }
            } else {
                if (minStep > 5) {
                    if (mListener != null)
                        mListener.parentScrollTo((int) stepScaleToPixel(minStep - 5));
                } else {
                    if (mListener != null)
                        mListener.parentScrollTo(0);
                }
            }
            isAutoScrolled = true;
        }
    }

    private int getStepAfterCurrentTime() {
        int step = -1;
        for (TimelineAvailability arrAvailabilitySlot : arrAvailabilitySlots) {
            if (arrAvailabilitySlot.getTAStartTime().getTimeInMillis() < todaysDate.getTimeInMillis()
                    && arrAvailabilitySlot.getTAEndTime().getTimeInMillis() > todaysDate.getTimeInMillis()) {
                /// in between booking slot
                break;
            } else if (arrAvailabilitySlot.getTAStartTime().getTimeInMillis() > todaysDate.getTimeInMillis()) {

                LogHelper.logD(TAG, "Start time more than current time");

                int todaysMinutes = arrAvailabilitySlot.getTAStartTime().get(Calendar.HOUR_OF_DAY) * 60 + arrAvailabilitySlot.getTAStartTime().get(Calendar.MINUTE);
                step = (int) stepScaleToPixel(todaysMinutes);
                break;
            }
        }
        return step;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private String getTimeFromStep() {
        Calendar calendar1 = DateTimeHelper.convertDateStrIntoCalendar(arrTimes.get((int) selection.getStartStep()), TIME_FORMAT);
        Calendar calendar2 = DateTimeHelper.convertDateStrIntoCalendar(arrTimes.get((int) selection.getEndStep()), TIME_FORMAT);
        long diff = calendar2.getTimeInMillis() - calendar1.getTimeInMillis();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        return minutes + " Minutes";
    }

    private String getTimeFromStep(Calendar calendar1, Calendar calendar2) {
        long diff = calendar2.getTimeInMillis() - calendar1.getTimeInMillis();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        int hours = (int) (minutes / 60);
        if (hours <= 0) {
            return minutes + " Minutes";
        } else {
            long min = (minutes - hours * 60);
            String hrs = hours + " Hours";
            if (min > 0) {
                return hrs + ", " + (minutes - hours * 60) + " Minutes";
            }
            return hrs;
        }
    }

    private void drawSelected(Canvas canvas) {
        float y1 = stepScaleToPixel(selection.getStartStep());
        float y2 = stepScaleToPixel(selection.getEndStep());

        float right = getMeasuredWidth() - thumbRightMargin + thumbWidth / 8;

        selection.getDrawable().setBounds((int) startPadding, (int) y1, (int) right, (int) y2);
        selection.getDrawable().draw(canvas);

        String text = getTimeFromStep();
        Rect rect = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), rect);
        float textx = (thumbWidth / 2) - (rect.width() / 2);
        int yPos = (int) (((y2 - y1) / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));

        canvas.drawText(text, textx + right, yPos + y1, textPaint);
    }

    private int convertToPx(float dp) {
        return DisplayHelper.convertDpToPx(getContext(), dp);
    }

    private int convertSpToPx(float sp) {
        return DisplayHelper.convertSpToPx(getContext(), sp);
    }

    private void drawThumb(Canvas canvas) {
        for (int i = 0; i < arrThumb.size(); i++) {
            // if (arrThumb.get(i).getDrawable() != null) {
            float dx = getMeasuredWidth() - thumbRightMargin;
            float dy = 0;

            String text = "";
            if (i == 0) {
                dy = stepScaleToPixel(selection.getStartStep()) - thumbHeight / 2;
                canvas.drawRoundRect(new RectF(dx, dy, dx + thumbWidth, dy + thumbHeight), radius, radius, mThumbPaint1);
                text = "Start";
            } else {
                dy = stepScaleToPixel(selection.getEndStep()) - thumbHeight / 2;
                canvas.drawRoundRect(new RectF(dx, dy, dx + thumbWidth, dy + thumbHeight), radius, radius, mThumbPaint2);
                text = "Stop";
            }

            Rect rect = new Rect();
            mThumbTextPaint.getTextBounds(text, 0, text.length(), rect);
            float textx = (thumbWidth / 2) - (rect.width() / 2);
            int yPos = (int) ((thumbHeight / 2) - ((mThumbTextPaint.descent() + mThumbTextPaint.ascent()) / 2));
            canvas.drawText(text, textx + dx, yPos + dy, mThumbTextPaint);
            // }
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        float coordinate = event.getY();
        int action = event.getAction();
        float coordinateX = event.getX();


        // Find thumb closest to event coordinate on screen touch
        if (action == MotionEvent.ACTION_DOWN) {
            mStartClickTime = Calendar.getInstance().getTimeInMillis();
            isOutsideButtons = false;
            if (coordinateX < getMeasuredWidth() - thumbRightMargin || coordinateX > getMeasuredWidth() - thumbRightMargin + thumbWidth) {
                isOutsideButtons = true;
                return true;
            }
            if (isEditingEnable && selectedDate.getTimeInMillis() >= todaysDate.getTimeInMillis()) {
                currentThumbIndex = getClosestThumbIndex(coordinate);

                if (currentThumbIndex == -1) {
                    return false;
                }

                currentThumb = getThumbAt(currentThumbIndex);

                int[] state = new int[]{android.R.attr.state_window_focused, android.R.attr.state_pressed};
                if (currentThumb.getDrawable() != null)
                    currentThumb.getDrawable().setState(state);
                getParent().requestDisallowInterceptTouchEvent(true);
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
            mStartClickTime = null;
        }


        if (action == MotionEvent.ACTION_UP) {
            if (currentThumb != null && currentThumb.getDrawable() != null) {
                int[] state = new int[]{};
                currentThumb.getDrawable().setState(state);
            }
            if (mStartClickTime != null) {
                long clickDuration = Calendar.getInstance().getTimeInMillis() - mStartClickTime;
                if (clickDuration >= MAX_CLICK_DURATION) {
                    // long click
                    float step = pixelToStep(asStep(coordinate));
                    selection.setStartStep(step);
                    selection.setEndStep(step + minimumSteps);
                }
            }
        }


        if (isEditingEnable && selectedDate.getTimeInMillis() >= todaysDate.getTimeInMillis() && !isOutsideButtons) {
            coordinate = asStep(coordinate);
            float step = pixelToStep(coordinate);

            if (step >= 0 && step < arrTimes.size()) {
                float maxSteps = 0;
                float minimumSteps = 1;
                if (isMoreStepsThanMax(currentThumbIndex, coordinate, maxSteps)) {
                    LogHelper.logD(TAG, "More than max");
                    if (currentThumbIndex == 0) {
                        selection.setStartStep(selection.getEndStep() - maxSteps);
                        // LogHelper.logD(TAG, "Start step " + (selection.getEndStep() - maxSteps));
                    } else {
                        selection.setEndStep(selection.getStartStep() + maxSteps);
                        // LogHelper.logD(TAG, "End step " + (selection.getStartStep() + maxSteps));

                    }
                } else if (isMoreStepsThanMin(currentThumbIndex, coordinate, minimumSteps)) {
                    LogHelper.logD(TAG, "More than min");

                    if (currentThumbIndex == 0) {
                        selection.setStartStep(pixelToStep(coordinate));
                    } else {
                        selection.setEndStep(pixelToStep(coordinate));
                    }
                } else {
                    LogHelper.logD(TAG, "Else case");
                    if (currentThumbIndex == 0) {
                        selection.setStartStep(selection.getEndStep() - minimumSteps);
                        // LogHelper.logD(TAG, "Start step " + (selection.getEndStep() - minimumSteps));
                    } else {
                        selection.setEndStep(selection.getStartStep() + minimumSteps);
                        //LogHelper.logD(TAG, "End step " + (selection.getStartStep() + minimumSteps));

                    }
                }
                if (mListener != null) {
                    if (currentThumbIndex == 0) {
                        // start
                        mListener.onStartChange(DateTimeHelper.convertDateStrIntoCalendar(arrTimes.get((int) selection.getStartStep()), TIME_FORMAT));
                    } else {
                        // end
                        mListener.onEndChange(DateTimeHelper.convertDateStrIntoCalendar(arrTimes.get((int) selection.getEndStep()), TIME_FORMAT));
                    }
                }
            }
        }
        invalidate();

        return true;
    }

    private void checkRegion() {
        boolean isLocalPublishEnable = false;
        if (type == TYPE.SET_AVAILABILITY) {
            isLocalPublishEnable = true;
        }

        if (!isEditingEnable) {
            isLocalPublishEnable = false;
        } else {
            float selectedStartStep = selection.getStartStep();
            float selectedEndStep = selection.getEndStep();

            for (TimelineAvailability availabilitySlot : arrAvailabilitySlots) {
                Calendar startCalender = availabilitySlot.getTAStartTime();
                Calendar endCalender = availabilitySlot.getTAEndTime();
                float startStep = 0;
                float endStep = 0;

                if (startCalender.get(Calendar.DAY_OF_MONTH) < selectedDate.get(Calendar.DAY_OF_MONTH)) {
                    startStep = 0;
                    endStep = stepFromCalender(availabilitySlot.getTAEndTime());
                } else if (endCalender.get(Calendar.DAY_OF_MONTH) > selectedDate.get(Calendar.DAY_OF_MONTH)) {
                    startStep = stepFromCalender(availabilitySlot.getTAStartTime());
                    endStep = arrTimes.size();
                } else {
                    startStep = stepFromCalender(availabilitySlot.getTAStartTime());
                    endStep = stepFromCalender(availabilitySlot.getTAEndTime());
                }

                if (type == TYPE.SET_AVAILABILITY) {

                    if (selectedStartStep <= startStep && selectedEndStep <= startStep) {
                        /// both points above the availability
                        if (!isLocalPublishEnable) {
                            isLocalPublishEnable = true;
                        }
                    } else if (selectedStartStep >= endStep && selectedEndStep >= endStep) {
                        /// both points below the availability
                        if (!isLocalPublishEnable) {
                            isLocalPublishEnable = true;
                        }
                    } else {
                        if (isLocalPublishEnable) {
                            isLocalPublishEnable = false;
                        }
                        break;
                    }
                } else {
                    if ((selectedStartStep >= startStep && selectedEndStep >= startStep) &&
                            (selectedStartStep <= endStep && selectedEndStep <= endStep)) {
                        /// both points inside the availability
                        isLocalPublishEnable = true;
                    }
                }
            }

            if (type == TYPE.BOOK_SLOT && isLocalPublishEnable) {

                for (TimelineBooked arrBookedSlot : arrBookedSlots) {
                    float startStepBookedSlot = stepFromCalender(arrBookedSlot.getTBStartTime());
                    float endStepBookedSlot = stepFromCalender(arrBookedSlot.getTBEndTime());
                    if (selectedStartStep <= startStepBookedSlot && selectedEndStep <= startStepBookedSlot) {
                        /// both points above the availability
                        if (!isLocalPublishEnable) {
                            isLocalPublishEnable = true;
                        }
                    } else if (selectedStartStep >= endStepBookedSlot && selectedEndStep >= endStepBookedSlot) {
                        /// both points below the availability
                        if (!isLocalPublishEnable) {
                            isLocalPublishEnable = true;
                        }
                    } else {
                        if (isLocalPublishEnable) {
                            isLocalPublishEnable = false;
                        }
                        break;
                    }

                }
            }

        }


        if (selectedDate.getTimeInMillis() < todaysDate.getTimeInMillis()) {
            isLocalPublishEnable = false;
        } else {
            if (selectedDate.get(Calendar.YEAR) == todaysDate.get(Calendar.YEAR) &&
                    selectedDate.get(Calendar.MONTH) == todaysDate.get(Calendar.MONTH) &&
                    selectedDate.get(Calendar.DAY_OF_MONTH) == todaysDate.get(Calendar.DAY_OF_MONTH)) {
                // today only
                int todaysMinutes = todaysDate.get(Calendar.HOUR_OF_DAY) * 60 + todaysDate.get(Calendar.MINUTE);

                if (selection.getStartStep() < todaysMinutes) {
                    isLocalPublishEnable = false;
                }
            }
        }

        float diff = selection.getEndStep() - selection.getStartStep();
        if ((diff < minimumSteps || diff > maxSteps) && isLocalPublishEnable && type != TYPE.SET_AVAILABILITY) {
            isLocalPublishEnable = false;
        }

        if (isLocalPublishEnable != isPublishEnable) {
            isPublishEnable = isLocalPublishEnable;

            if (mListener != null)
                mListener.onPublishStatusChange(isPublishEnable);
            if (isLocalPublishEnable) {
                LogHelper.logD(TAG, "Publish Enable");
            } else {
                LogHelper.logD(TAG, "Publish Disable");
            }
        }
    }

    public void setmListener(TimelineListener mListener) {
        this.mListener = mListener;
        mListener.onStartChange(getSelectedStartTime());
        mListener.onEndChange(getSelectedEndTime());
    }

    private float asStep(float pixelValue) {
        return stepScaleToPixel(pixelToStep(pixelValue));
    }

    private float stepScaleToPixel(float stepScaleValue) {
        float gap = lineGap;
        float pixelValue = stepScaleValue * (gap);
        return pixelValue + paddingTopBottom;
    }

    private float pixelToStep(float pixelValue) {
        float gap = lineGap;
        float stepScaleValue = Math.round((pixelValue - paddingTopBottom) / gap);
        if (stepScaleValue < 0) {
            return 0;
        } else if (stepScaleValue > arrTimes.size()) {
            return arrTimes.size();
        } else {
            return Math.round(stepScaleValue);
        }
    }

    public Thumb getThumbAt(int index) {
        return arrThumb.get(index);
    }

    private int getClosestThumbIndex(float coordinate) {
        int closest = -1;
        if (!arrThumb.isEmpty()) {
            float shortestDistance = thumbFreeArea + thumbWidth / 2 + getPaddingTop() + getPaddingBottom();
            // Oldschool for-loop to have access to index
            for (int i = 0; i < arrThumb.size(); i++) {
                // Find thumb closest to x coordinate
                if (i == 0) {
                    float tcoordinate = stepScaleToPixel(selection.getStartStep());
                    float distance = Math.abs(coordinate - tcoordinate);
                    if (distance <= shortestDistance) {
                        shortestDistance = distance;
                        closest = i;
                    }
                } else {
                    float tcoordinate = stepScaleToPixel(selection.getEndStep());
                    float distance = Math.abs(coordinate - tcoordinate);
                    if (distance <= shortestDistance) {
                        shortestDistance = distance;
                        closest = i;

                    }
                }

            }
        }
        return closest;
    }

    private void fillArrayTimes() {
        Calendar endDate = Calendar.getInstance();


        while (true) {
            arrTimes.add(DateTimeHelper.formatCalendar(currentCalender, TIME_FORMAT));
            currentCalender.add(Calendar.MINUTE, 1);
            if (endDate.get(Calendar.DAY_OF_MONTH) != currentCalender.get(Calendar.DAY_OF_MONTH)) {
                break;
            }
        }
    }


}