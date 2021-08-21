package com.yogeshpaliyal.timelineview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.yogeshpaliyal.commons_utils.DisplayHelper;

import java.util.ArrayList;

public class TimelineRuler extends View {
    private static final String TAG = "TimelineRuler";
    private float paddingTopBottom = convertToPx(20);
    private float paddingStart = convertToPx(20);
    private String TIME_FORMAT = "hh:mm a";
    private Paint timePaint, textPaint;
    private float lineGap;
    private float smallLineWidth = convertToPx(15);
    private float longLineWidth = convertToPx(30);

    private float lineBoldStroke = convertToPx(2);
    private float lineThinStroke = convertToPx(1);

    private float textSize = convertSpToPx(14);

    private ArrayList<String> arrTimes = new ArrayList<>();


    public TimelineRuler(Context context, float lineGap, ArrayList<String> arrTimes) {
        super(context);
        this.lineGap = lineGap;
        this.arrTimes = arrTimes;
        init();
    }

    public TimelineRuler(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimelineRuler(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("NewApi")
    public TimelineRuler(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onSizeChanged(int viewWidth, int viewHeight, int oldw, int oldh) {

    }

    private void init() {
        timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timePaint.setColor(Color.BLACK);
        timePaint.setStrokeWidth(lineBoldStroke);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(textSize);
    }

    public void setTopPadding(float topPadding) {
        paddingTopBottom = topPadding;
        invalidate();
    }

    public void setPaddingStart(float paddingStart) {
        this.paddingStart = paddingStart;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float startPointX = paddingStart;
        float startPoint = paddingTopBottom;

        for (int i = 0; i < arrTimes.size(); i++) {


            if (i % 10 == 0) {
                timePaint.setStrokeWidth(lineBoldStroke);
                canvas.drawLine(startPointX, startPoint, startPointX + longLineWidth, startPoint, timePaint);
                canvas.drawText(arrTimes.get(i), startPointX + longLineWidth + 10, startPoint + textSize / 2, textPaint);
            } else {
                timePaint.setStrokeWidth(lineThinStroke);
                canvas.drawLine(startPointX, startPoint, startPointX + smallLineWidth, startPoint, timePaint);
            }

            startPoint = startPoint + lineGap;
        }


    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredWidth = getMeasuredWidth();
        int desiredHeight = (int) (arrTimes.size() * (lineGap)) + (2 * (int) paddingTopBottom);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }


    private float convertToPx(float dp) {
        return DisplayHelper.convertDpToPx(getContext(), dp);
    }

    private float convertSpToPx(float sp) {
        return DisplayHelper.convertSpToPx(getContext(), sp);
    }


}
