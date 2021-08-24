package com.yogeshpaliyal.timelineviewsample

import com.yogeshpaliyal.timelineview.Thumb
import com.yogeshpaliyal.timelineview.interfaces.TimelineAvailability
import java.util.*

data class AvailabilityModel(val startTime: Calendar,val endTime: Calendar,) : TimelineAvailability{

    override fun getTAStartTime(): Calendar  = startTime

    override fun getTAEndTime(): Calendar = endTime

}
