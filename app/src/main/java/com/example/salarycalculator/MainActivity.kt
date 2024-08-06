package com.example.salarycalculator

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var yearSpinner: Spinner
    private lateinit var monthSpinner: Spinner
    private val TAX_RATE = 0.13
    private val UNION_FEE = 0.01
    private val REQUEST_CODE_WORK_DETAILS = 1
    private val workDetails = mutableMapOf<String, WorkDetail>()

    data class WorkDetail(
        var overtimeFirst2Hours: Double,
        var overtimeMoreThan2Hours: Double,
        var weekendHours: Double
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация SharedPreferences
        sharedPreferences = getSharedPreferences("SalaryPrefs", MODE_PRIVATE)

        // Инициализация Spinner для выбора года
        yearSpinner = findViewById(R.id.year_spinner)
        val years = arrayOf(2023, 2024, 2025, 2026, 2027)
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = yearAdapter

        // Инициализация Spinner для выбора месяца
        monthSpinner = findViewById(R.id.month_spinner)
        val months = arrayOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpinner.adapter = monthAdapter

        // Инициализация других элементов
        val salaryInput = findViewById<TextInputEditText>(R.id.salary)
        val monthlyHoursInput = findViewById<TextInputEditText>(R.id.monthly_hours)
        val missedHoursInput = findViewById<TextInputEditText>(R.id.missed_hours)
        val allowancesPercentageInput = findViewById<TextInputEditText>(R.id.allowances_percentage)
        val monthlyBonusPercentageInput = findViewById<TextInputEditText>(R.id.monthly_bonus_percentage)
        val managerBonusInput = findViewById<TextInputEditText>(R.id.manager_bonus)
        val calculateButton = findViewById<Button>(R.id.calculate_button)
        val resultText = findViewById<TextView>(R.id.result_text)
        val workedHoursText = findViewById<TextView>(R.id.worked_hours_text)
        val calendarButton = findViewById<Button>(R.id.calendar_button)
        val overtimeFirst2HoursText = findViewById<TextView>(R.id.overtime_first_2_hours_text)
        val overtimeMoreThan2HoursText = findViewById<TextView>(R.id.overtime_more_than_2_hours_text)
        val weekendHoursText = findViewById<TextView>(R.id.weekend_hours_text)

        calendarButton.setOnClickListener {
            showDatePicker()
        }

        calculateButton.setOnClickListener {
            calculateSalary()
        }

        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateFieldsForSelectedMonthAndYear()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateFieldsForSelectedMonthAndYear()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
            val intent = Intent(this, WorkDetailsActivity::class.java)
            intent.putExtra("selected_date", selectedDate)
            startActivityForResult(intent, REQUEST_CODE_WORK_DETAILS)
        }, year, month, day)

        datePickerDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_WORK_DETAILS && resultCode == RESULT_OK) {
            data?.let {
                val selectedDate = it.getStringExtra("selected_date")
                val overtimeFirst2Hours = it.getStringExtra("overtime_first_2_hours")?.toDoubleOrNull() ?: 0.0
                val overtimeMoreThan2Hours = it.getStringExtra("overtime_more_than_2_hours")?.toDoubleOrNull() ?: 0.0
                val weekendHours = it.getStringExtra("weekend_hours")?.toDoubleOrNull() ?: 0.0

                if (selectedDate != null) {
                    workDetails[selectedDate] = WorkDetail(overtimeFirst2Hours, overtimeMoreThan2Hours, weekendHours)
                    updateWorkDetailsSummary()
                }
            }
        }
    }

    private fun updateWorkDetailsSummary() {
        var totalOvertimeFirst2Hours = 0.0
        var totalOvertimeMoreThan2Hours = 0.0
        var totalWeekendHours = 0.0

        for (workDetail in workDetails.values) {
            totalOvertimeFirst2Hours += workDetail.overtimeFirst2Hours
            totalOvertimeMoreThan2Hours += workDetail.overtimeMoreThan2Hours
            totalWeekendHours += workDetail.weekendHours
        }

        findViewById<TextView>(R.id.overtime_first_2_hours_text).text = "Сверхурочные первые 2 часа: $totalOvertimeFirst2Hours"
        findViewById<TextView>(R.id.overtime_more_than_2_hours_text).text = "Сверхурочные свыше 2 часов: $totalOvertimeMoreThan2Hours"
        findViewById<TextView>(R.id.weekend_hours_text).text = "Часы работы в выходные: $totalWeekendHours"
    }

    private fun updateFieldsForSelectedMonthAndYear() {
        val year = yearSpinner.selectedItem.toString()
        val month = monthSpinner.selectedItemPosition + 1

        val key = "$year-$month"
        val salary = sharedPreferences.getString("${key}_salary", "") ?: ""
        val monthlyHours = sharedPreferences.getString("${key}_monthly_hours", "") ?: ""
        val missedHours = sharedPreferences.getString("${key}_missed_hours", "") ?: ""
        val allowancesPercentage = sharedPreferences.getString("${key}_allowances_percentage", "") ?: ""
        val monthlyBonusPercentage = sharedPreferences.getString("${key}_monthly_bonus_percentage", "") ?: ""
        val managerBonus = sharedPreferences.getString("${key}_manager_bonus", "") ?: ""

        findViewById<TextInputEditText>(R.id.salary).setText(salary)
        findViewById<TextInputEditText>(R.id.monthly_hours).setText(monthlyHours)
        findViewById<TextInputEditText>(R.id.missed_hours).setText(missedHours)
        findViewById<TextInputEditText>(R.id.allowances_percentage).setText(allowancesPercentage)
        findViewById<TextInputEditText>(R.id.monthly_bonus_percentage).setText(monthlyBonusPercentage)
        findViewById<TextInputEditText>(R.id.manager_bonus).setText(managerBonus)

        updateWorkDetailsSummary()
    }

    private fun calculateSalary() {
        val salaryInput = findViewById<TextInputEditText>(R.id.salary).text.toString().toDoubleOrNull() ?: return
        val monthlyHoursInput = findViewById<TextInputEditText>(R.id.monthly_hours).text.toString().toDoubleOrNull() ?: return
        val missedHoursInput = findViewById<TextInputEditText>(R.id.missed_hours).text.toString().toDoubleOrNull() ?: return
        val allowancesPercentageInput = findViewById<TextInputEditText>(R.id.allowances_percentage).text.toString().toDoubleOrNull() ?: return
        val monthlyBonusPercentageInput = findViewById<TextInputEditText>(R.id.monthly_bonus_percentage).text.toString().toDoubleOrNull() ?: return
        val managerBonusInput = findViewById<TextInputEditText>(R.id.manager_bonus).text.toString().toDoubleOrNull() ?: return

        var totalOvertimeFirst2Hours = 0.0
        var totalOvertimeMoreThan2Hours = 0.0
        var totalWeekendHours = 0.0

        for (workDetail in workDetails.values) {
            totalOvertimeFirst2Hours += workDetail.overtimeFirst2Hours
            totalOvertimeMoreThan2Hours += workDetail.overtimeMoreThan2Hours
            totalWeekendHours += workDetail.weekendHours
        }

        val totalWorkedHours = monthlyHoursInput - missedHoursInput
        val allowances = salaryInput * (allowancesPercentageInput / 100)
        val monthlyBonus = salaryInput * (monthlyBonusPercentageInput / 100)
        val overtimeFirst2 = totalOvertimeFirst2Hours * 1.5
        val overtimeMoreThan2 = totalOvertimeMoreThan2Hours * 2.0
        val weekendWork = totalWeekendHours * 2.0

        val totalSalary = salaryInput + allowances + monthlyBonus + managerBonusInput +
                overtimeFirst2 + overtimeMoreThan2 + weekendWork
        val taxedSalary = totalSalary - (totalSalary * TAX_RATE) - (totalSalary * UNION_FEE)

        findViewById<TextView>(R.id.result_text).text = "Итоговая зарплата: $taxedSalary"
        findViewById<TextView>(R.id.worked_hours_text).text = "Отработанные часы: $totalWorkedHours"
    }
}
