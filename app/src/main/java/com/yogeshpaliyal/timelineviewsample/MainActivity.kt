package com.yogeshpaliyal.timelineviewsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.yogeshpaliyal.timelineview.TYPE
import com.yogeshpaliyal.timelineview.TimelineView
import com.yogeshpaliyal.timelineviewsample.databinding.ActivityMainBinding
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private val arrAvailability by lazy {
        ArrayList<AvailabilityModel>()
    }

    private val arrBookings by lazy {
        ArrayList<BookingModel>()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.switchEditMode.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.timeline.isEditingEnable = isChecked
        }

        binding.btnSeller.setOnClickListener {
            binding.timeline.type = TYPE.SET_AVAILABILITY
        }

        binding.btnBuyer.setOnClickListener {
            binding.timeline.type = TYPE.BOOK_SLOT
        }

        binding.btnAdd.setOnClickListener {
            val selectedDay = binding.timeline.selectedDateOnly.timeInMillis
            val startDate =
                selectedDay + TimeUnit.MINUTES.toMillis(binding.timeline.selectedStartTime.toLong())
            val endDate =
                selectedDay + TimeUnit.MINUTES.toMillis(binding.timeline.selectedEndTime.toLong())

            Log.d("TimelineView", "onCreate: startDate $startDate")
            Log.d("TimelineView", "onCreate: endDate $endDate")
            if (binding.timeline.type == TYPE.SET_AVAILABILITY) {
                arrAvailability.add(AvailabilityModel(Calendar.getInstance().also {
                    it.timeInMillis = startDate
                }, Calendar.getInstance().also {
                    it.timeInMillis = endDate
                }))
                binding.timeline.setArrAvailabilitySlots(arrAvailability.toList())
            } else {
                arrBookings.add(BookingModel(Calendar.getInstance().also {
                    it.timeInMillis = startDate
                }, Calendar.getInstance().also {
                    it.timeInMillis = endDate
                }))
                binding.timeline.setBookingsSlots(arrBookings.toList())
            }
        }
    }
}