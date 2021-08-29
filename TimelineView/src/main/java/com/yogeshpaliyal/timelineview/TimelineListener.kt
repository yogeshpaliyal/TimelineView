package com.yogeshpaliyal.timelineview

import com.yogeshpaliyal.timelineview.interfaces.TimelineAvailability

interface TimelineListener {
    fun onDateChange()
    fun parentScrollTo(y: Int)
    fun onStartChange(minute: Int)
    fun onEndChange(minute: Int)
    fun onCancelClicked(bookedSlots: TimelineAvailability?)
    fun onPublishStatusChange(isEnable: Boolean)
}