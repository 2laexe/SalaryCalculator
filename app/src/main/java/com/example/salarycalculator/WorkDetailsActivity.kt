package com.example.salarycalculator

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText

class WorkDetailsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_details)

        val selectedDate = intent.getStringExtra("selected_date")

        val overtimeFirst2HoursInput = findViewById<TextInputEditText>(R.id.overtime_first_2_hours)
        val overtimeMoreThan2HoursInput = findViewById<TextInputEditText>(R.id.overtime_more_than_2_hours)
        val weekendHoursInput = findViewById<TextInputEditText>(R.id.weekend_hours)
        val saveButton = findViewById<Button>(R.id.save_button)

        saveButton.setOnClickListener {
            val overtimeFirst2Hours = overtimeFirst2HoursInput.text.toString()
            val overtimeMoreThan2Hours = overtimeMoreThan2HoursInput.text.toString()
            val weekendHours = weekendHoursInput.text.toString()

            val resultIntent = intent
            resultIntent.putExtra("selected_date", selectedDate)
            resultIntent.putExtra("overtime_first_2_hours", overtimeFirst2Hours)
            resultIntent.putExtra("overtime_more_than_2_hours", overtimeMoreThan2Hours)
            resultIntent.putExtra("weekend_hours", weekendHours)

            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
