package com.udacity.project4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.udacity.project4.databinding.ActivityRemindersBinding

class RemindersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRemindersBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout for this activity
        binding = DataBindingUtil.setContentView(this,R.layout.activity_reminders)
    }
}