package com.yogeshpaliyal.timelineview;

import com.yogeshpaliyal.timelineview.interfaces.TimelineAvailability;

import java.util.Calendar;

public interface TimelineListener {
    void onDateChange();

    void parentScrollTo(int y);

    void onStartChange(Calendar calendar);

    void onEndChange(Calendar calendar);

    void onCancelClicked(TimelineAvailability bookedSlots);

    void onPublishStatusChange(boolean isEnable);
}
