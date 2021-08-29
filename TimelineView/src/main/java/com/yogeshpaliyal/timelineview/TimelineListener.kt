package com.yogeshpaliyal.timelineview;

import com.yogeshpaliyal.timelineview.interfaces.TimelineAvailability;

import java.util.Calendar;

public interface TimelineListener {
    void onDateChange();

    void parentScrollTo(int y);

    void onStartChange(int minute);

    void onEndChange(int minute);

    void onCancelClicked(TimelineAvailability bookedSlots);

    void onPublishStatusChange(boolean isEnable);
}
