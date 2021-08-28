package com.yogeshpaliyal.timelineviewsample

import com.yogeshpaliyal.timelineview.interfaces.TimelineBooked
import java.util.*

data class BookingModel(val startTime: Calendar, val endTime: Calendar) : TimelineBooked{
    override fun getTBStartTime(): Calendar = startTime

    override fun getTBEndTime(): Calendar = endTime

}
