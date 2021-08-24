package com.yogeshpaliyal.timelineview.interfaces

import com.yogeshpaliyal.timelineview.Thumb
import java.util.*

interface TimelineAvailability {
    fun getTAStartTime() : Calendar
    fun getTAEndTime() : Calendar
}