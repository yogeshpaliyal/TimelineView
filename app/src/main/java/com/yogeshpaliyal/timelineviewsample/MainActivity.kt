package com.yogeshpaliyal.timelineviewsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.yogeshpaliyal.timelineview.TimelineView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val timeLineView = findViewById<TimelineView>(R.id.timeline)
        timeLineView?.isEditingEnable = true
    }
}