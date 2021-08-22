package com.yogeshpaliyal.timelineviewsample

import com.yogeshpaliyal.timelineview.Thumb
import com.yogeshpaliyal.timelineview.interfaces.TimelineAvailability
import java.util.*

data class AvailabilityModel(val startTime: String) : TimelineAvailability{
    override var thumb: Thumb?
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun getTAStartTime(): Calendar {

    }

    override fun getTAEndTime(): Calendar {

    }

    override fun setCancelButton(thumb: Thumb?) {

    }

}
