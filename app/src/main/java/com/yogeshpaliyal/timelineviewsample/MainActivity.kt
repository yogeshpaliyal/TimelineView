package com.yogeshpaliyal.timelineviewsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.yogeshpaliyal.timelineview.TYPE
import com.yogeshpaliyal.timelineview.TimelineView
import com.yogeshpaliyal.timelineviewsample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private val arrAvaialability by lazy {

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.switchEditMode.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.timeline.isEditingEnable = isChecked
        }

        binding.btnSeller.setOnClickListener {
            binding.timeline.setType(TYPE.SET_AVAILABILITY)
        }

        binding.btnBuyer.setOnClickListener {
            binding.timeline.setType(TYPE.BOOK_SLOT)
        }

        binding.btnAdd.setOnClickListener{
            binding.timeline.ava
        }
    }
}