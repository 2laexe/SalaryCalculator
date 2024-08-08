package com.example.salarycalculator

import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*


class WorkDetailsActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dateTextView: TextView
    private lateinit var overtimeFirst1HoursInput: EditText
    private lateinit var overtimeFirst2HoursInput: EditText
    private lateinit var overtimeMoreThan2HoursInput: EditText
    private lateinit var weekendHoursInput: EditText
    private lateinit var saveButton: Button
    private lateinit var datePickerButton: Button

    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0
    private var selectedDay: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_details)

        sharedPreferences = getSharedPreferences("SalaryPrefs", MODE_PRIVATE)

        dateTextView = findViewById(R.id.date_text_view)
        overtimeFirst1HoursInput = findViewById(R.id.overtime_first_1_hours_input)
        overtimeFirst2HoursInput = findViewById(R.id.overtime_first_2_hours_input)
        overtimeMoreThan2HoursInput = findViewById(R.id.overtime_more_than_2_hours_input)
        weekendHoursInput = findViewById(R.id.weekend_hours_input)
        saveButton = findViewById(R.id.save_button)
        datePickerButton = findViewById(R.id.date_picker_button)

        selectedYear = intent.getIntExtra("year", Calendar.getInstance().get(Calendar.YEAR))
        selectedMonth = intent.getIntExtra("month", Calendar.getInstance().get(Calendar.MONTH) + 1)

        datePickerButton.setOnClickListener {
            showDatePickerDialog()
        }

        saveButton.setOnClickListener {
            saveWorkDetails()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = selectedYear
        val month = selectedMonth - 1 // Month is 0-indexed in DatePickerDialog
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(this, this, year, month, day).show()
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        selectedYear = year
        selectedMonth = month + 1
        selectedDay = dayOfMonth

        val date = String.format("%04d-%02d-%02d", year, selectedMonth, dayOfMonth)
        dateTextView.text = date

        // Load stored values for the selected date
        overtimeFirst1HoursInput.setText(sharedPreferences.getFloat("${date}_overtime_first_1_hours", 0.0f).toString())
        overtimeFirst2HoursInput.setText(sharedPreferences.getFloat("${date}_overtime_first_2_hours", 0.0f).toString())
        overtimeMoreThan2HoursInput.setText(sharedPreferences.getFloat("${date}_overtime_more_than_2_hours", 0.0f).toString())
        weekendHoursInput.setText(sharedPreferences.getFloat("${date}_weekend_hours", 0.0f).toString())
    }

    private fun saveWorkDetails() {
        val date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth, selectedDay)

        val overtimeFirst1Hours = overtimeFirst1HoursInput.text.toString().toFloatOrNull() ?: 0.0f
        val overtimeFirst2Hours = overtimeFirst2HoursInput.text.toString().toFloatOrNull() ?: 0.0f
        val overtimeMoreThan2Hours = overtimeMoreThan2HoursInput.text.toString().toFloatOrNull() ?: 0.0f
        val weekendHours = weekendHoursInput.text.toString().toFloatOrNull() ?: 0.0f

        with(sharedPreferences.edit()) {
            putFloat("${date}_overtime_first_1_hours", overtimeFirst2Hours)
            putFloat("${date}_overtime_first_2_hours", overtimeFirst2Hours)
            putFloat("${date}_overtime_more_than_2_hours", overtimeMoreThan2Hours)
            putFloat("${date}_weekend_hours", weekendHours)
            apply()
        }

        Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show()
    }
}
