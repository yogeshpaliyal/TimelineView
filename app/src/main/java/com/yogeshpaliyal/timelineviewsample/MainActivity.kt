package com.yogeshpaliyal.timelineviewsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.yogeshpaliyal.timelineview.TYPE
import com.yogeshpaliyal.timelineview.TimelineView
import com.yogeshpaliyal.timelineviewsample.databinding.ActivityMainBinding
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private val arrAvailability by lazy {
        ArrayList<AvailabilityModel>()
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

        binding.btnAdd.setOnClickListener{
            val startDate = binding.timeline.selectedStartDateTime
            val endDate = binding.timeline.selectedEndDateTime
            if (binding.timeline.type == TYPE.SET_AVAILABILITY) {
                arrAvailability.add(AvailabilityModel(startDate,endDate))
                binding.timeline.setArrAvailabilitySlots(arrAvailability.toList())
            }else{
                binding.timeline.setArrAvailabilitySlots(arrAvailability.toList())
            }
        }
    }
}