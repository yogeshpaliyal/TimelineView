package com.yogeshpaliyal.timelineview.interfaces

import com.yogeshpaliyal.timelineview.Thumb
import java.util.*

interface TimelineAvailability {

    var thumb : Thumb?

    fun getTAStartTime() : Calendar
    fun getTAEndTime() : Calendar

    fun getCancelButton()= thumb
    fun setCancelButton(thumb: Thumb?)
}