package com.example.salarycalculator

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class WorkDetailsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_details)

        sharedPreferences = getSharedPreferences("SalaryPrefs", MODE_PRIVATE)

        selectedYear = intent.getIntExtra("year", 0)
        selectedMonth = intent.getIntExtra("month", 0)

        val overtimeFirst2HoursInput = findViewById<EditText>(R.id.overtime_first_2_hours)
        val overtimeMoreThan2HoursInput = findViewById<EditText>(R.id.overtime_more_than_2_hours)
        val weekendHoursInput = findViewById<EditText>(R.id.weekend_hours)
        val calendarView = findViewById<CalendarView>(R.id.calendar_view)
        val saveButton = findViewById<Button>(R.id.save_button)

        // Получение и установка даты в календаре
        val calendar = Calendar.getInstance()
        calendar.set(selectedYear, selectedMonth - 1, 1)
        calendarView.date = calendar.timeInMillis

        // Форматирование даты
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Обработка выбора даты в календаре
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = dateFormat.format(calendar.time)
            loadSavedDataForSelectedDate()
        }

        saveButton.setOnClickListener {
            saveDataForSelectedDate(
                overtimeFirst2HoursInput.text.toString().toDoubleOrNull() ?: 0.0,
                overtimeMoreThan2HoursInput.text.toString().toDoubleOrNull() ?: 0.0,
                weekendHoursInput.text.toString().toDoubleOrNull() ?: 0.0
            )
            finish()
        }

        // Загрузка сохраненных данных для выбранной даты
        loadSavedDataForSelectedDate()
    }

    private fun loadSavedDataForSelectedDate() {
        if (selectedDate.isNotEmpty()) {
            val overtimeFirst2HoursInput = findViewById<EditText>(R.id.overtime_first_2_hours)
            val overtimeMoreThan2HoursInput = findViewById<EditText>(R.id.overtime_more_than_2_hours)
            val weekendHoursInput = findViewById<EditText>(R.id.weekend_hours)

            overtimeFirst2HoursInput.setText(sharedPreferences.getFloat("${selectedDate}_overtime_first_2_hours", 0.0f).toString())
            overtimeMoreThan2HoursInput.setText(sharedPreferences.getFloat("${selectedDate}_overtime_more_than_2_hours", 0.0f).toString())
            weekendHoursInput.setText(sharedPreferences.getFloat("${selectedDate}_weekend_hours", 0.0f).toString())
        }
    }

    private fun saveDataForSelectedDate(overtimeFirst2Hours: Double, overtimeMoreThan2Hours: Double, weekendHours: Double) {
        if (selectedDate.isNotEmpty()) {
            val editor = sharedPreferences.edit()
            editor.putFloat("${selectedDate}_overtime_first_2_hours", overtimeFirst2Hours.toFloat())
            editor.putFloat("${selectedDate}_overtime_more_than_2_hours", overtimeMoreThan2Hours.toFloat())
            editor.putFloat("${selectedDate}_weekend_hours", weekendHours.toFloat())
            editor.apply()
        }
    }
}
