package com.example.salarycalculator

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
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
    private lateinit var salaryInput: TextInputEditText
    private lateinit var monthlyHoursInput: TextInputEditText
    private lateinit var missedHoursInput: TextInputEditText
    private lateinit var allowancesPercentageInput: TextInputEditText
    private lateinit var monthlyBonusPercentageInput: TextInputEditText
    private lateinit var managerBonusInput: TextInputEditText
    private lateinit var calculateButton: Button
    private lateinit var workedHoursText: TextView
    private lateinit var resultText: TextView
    private lateinit var calendarButton: Button
    private lateinit var overtimeFirst2HoursText: TextView
    private lateinit var overtimeMoreThan2HoursText: TextView
    private lateinit var weekendHoursText: TextView

    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0

    private val TAX_RATE = 0.13
    private val UNION_FEE = 0.01
    private val REQUEST_CODE_WORK_DETAILS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Воспроизведение звука при запуске приложения
        val mediaPlayer = MediaPlayer.create(this, R.raw.welcome_sound)
        mediaPlayer.start()

        sharedPreferences = getSharedPreferences("SalaryPrefs", MODE_PRIVATE)

        yearSpinner = findViewById(R.id.year_spinner)
        monthSpinner = findViewById(R.id.month_spinner)
        salaryInput = findViewById(R.id.salary)
        monthlyHoursInput = findViewById(R.id.monthly_hours)
        missedHoursInput = findViewById(R.id.missed_hours)
        allowancesPercentageInput = findViewById(R.id.allowances_percentage)
        monthlyBonusPercentageInput = findViewById(R.id.monthly_bonus_percentage)
        managerBonusInput = findViewById(R.id.manager_bonus)
        calculateButton = findViewById(R.id.calculate_button)
        workedHoursText = findViewById(R.id.worked_hours_text)
        resultText = findViewById(R.id.result_text)
        calendarButton = findViewById(R.id.calendar_button)
        overtimeFirst2HoursText = findViewById(R.id.overtime_first_2_hours_text)
        overtimeMoreThan2HoursText = findViewById(R.id.overtime_more_than_2_hours_text)
        weekendHoursText = findViewById(R.id.weekend_hours_text)

        setupSpinners()
        loadPreferences() // Load saved preferences

        calendarButton.setOnClickListener {
            openCalendar()
        }

        calculateButton.setOnClickListener {
            calculateSalary()
        }
    }

    private fun setupSpinners() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 10..currentYear + 10).toList()
        val months = (1..12).map { it.toString() }

        yearSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        monthSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)

        yearSpinner.setSelection(years.indexOf(currentYear))
        monthSpinner.setSelection(Calendar.getInstance().get(Calendar.MONTH))

        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedYear = years[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedMonth = months[position].toInt()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun openCalendar() {
        val intent = Intent(this, WorkDetailsActivity::class.java)
        intent.putExtra("year", selectedYear)
        intent.putExtra("month", selectedMonth)
        startActivityForResult(intent, REQUEST_CODE_WORK_DETAILS)
    }

    private fun calculateSalary() {
        val salary = salaryInput.text.toString().toDoubleOrNull() ?: 0.0
        val monthlyHours = monthlyHoursInput.text.toString().toDoubleOrNull() ?: 0.0
        val missedHours = missedHoursInput.text.toString().toDoubleOrNull() ?: 0.0
        val allowancesPercentage = allowancesPercentageInput.text.toString().toDoubleOrNull() ?: 0.0
        val monthlyBonusPercentage = monthlyBonusPercentageInput.text.toString().toDoubleOrNull() ?: 0.0
        val managerBonus = managerBonusInput.text.toString().toDoubleOrNull() ?: 0.0

        var overtimeFirst2Hours = 0.0
        var overtimeMoreThan2Hours = 0.0
        var weekendWorkHours = 0.0

        for (day in 1..31) {
            val dateKey = "$selectedYear-${String.format("%02d", selectedMonth)}-${String.format("%02d", day)}"
            overtimeFirst2Hours += sharedPreferences.getFloat("${dateKey}_overtime_first_2_hours", 0.0f).toDouble()
            overtimeMoreThan2Hours += sharedPreferences.getFloat("${dateKey}_overtime_more_than_2_hours", 0.0f).toDouble()
            weekendWorkHours += sharedPreferences.getFloat("${dateKey}_weekend_hours", 0.0f).toDouble()
        }

        overtimeFirst2HoursText.text = "Сверхурочные (первые 2 часа): $overtimeFirst2Hours"
        overtimeMoreThan2HoursText.text = "Сверхурочные (более 2 часов): $overtimeMoreThan2Hours"
        weekendHoursText.text = "Часы работы в выходные дни: $weekendWorkHours"

        val workedHours = monthlyHours - missedHours + overtimeFirst2Hours + overtimeMoreThan2Hours
        workedHoursText.text = "Отработанные часы: $workedHours"

        // Расчет пропорциональной зарплаты
        val proportionalSalary = salary * ((monthlyHours - missedHours) / monthlyHours)
        val hourlyRate = proportionalSalary / monthlyHours + ((proportionalSalary / workedHours)* allowancesPercentage / 100)
        val proportionalhourlyRate = (proportionalSalary / workedHours) + ((proportionalSalary / workedHours)* allowancesPercentage / 100)

        // Расчет отработанных часов
        val regularPay = hourlyRate * (monthlyHours - missedHours)
        val proportionalSalary2 = salary * ((workedHours ) / monthlyHours)

        // Расчет надбавок и премий на основе пропорционального оклада
        val allowances = (allowancesPercentage / 100) * proportionalSalary2
        val monthlyBonus = (monthlyBonusPercentage / 100) * proportionalSalary2

        // Расчет переработок и работы в выходные на основе пропорционального оклада
        val overtimeFirst2HoursPay = overtimeFirst2Hours * proportionalhourlyRate * 1.5 // Часы переработки (первые 2 часа)
        val overtimeMoreThan2HoursPay = overtimeMoreThan2Hours  * proportionalhourlyRate * 2.0 // Часы переработки (более 2 часов)
        val weekendWorkPay = weekendWorkHours * proportionalhourlyRate  * 2.0 // Часы работы в выходные

        val totalEarnings = proportionalSalary + allowances + monthlyBonus + managerBonus + overtimeFirst2HoursPay + overtimeMoreThan2HoursPay + weekendWorkPay
        val taxDeduction = totalEarnings * TAX_RATE
        val unionFeeDeduction = totalEarnings * UNION_FEE

        val netSalary = totalEarnings - taxDeduction - unionFeeDeduction

        // Воспроизведение звука, если общий доход больше 100000 руб.
        if (totalEarnings > 100000) {
            val mediaPlayer = MediaPlayer.create(this, R.raw.high_income_sound)
            mediaPlayer.start()
        }

        resultText.text = """
            Оклад: %.2f руб.
            Надбавки: %.2f руб.
            Ежемесячная премия: %.2f руб.
            Премия руководителя: %.2f руб.
            Оплата сверхурочных (первые 2 часа): %.2f руб.
            Оплата сверхурочных (более 2 часов): %.2f руб.
            Оплата за работу в выходные: %.2f руб.
            Общий доход до налогов: %.2f руб.
            Налоги: %.2f руб.
            Профсоюзный взнос: %.2f руб.
            Чистый доход: %.2f руб.
        """.trimIndent().format(
            proportionalSalary,
            allowances,
            monthlyBonus,
            managerBonus,
            overtimeFirst2HoursPay,
            overtimeMoreThan2HoursPay,
            weekendWorkPay,
            totalEarnings,
            taxDeduction,
            unionFeeDeduction,
            netSalary
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_WORK_DETAILS && resultCode == RESULT_OK) {
            calculateSalary()
        }
    }

    override fun onPause() {
        super.onPause()
        savePreferences() // Save preferences when the activity is paused
    }

    private fun savePreferences() {
        val editor = sharedPreferences.edit()
        editor.putInt("selected_year", selectedYear)
        editor.putInt("selected_month", selectedMonth)
        editor.putString("salary", salaryInput.text.toString())
        editor.putString("monthly_hours", monthlyHoursInput.text.toString())
        editor.putString("missed_hours", missedHoursInput.text.toString())
        editor.putString("allowances_percentage", allowancesPercentageInput.text.toString())
        editor.putString("monthly_bonus_percentage", monthlyBonusPercentageInput.text.toString())
        editor.putString("manager_bonus", managerBonusInput.text.toString())
        editor.apply()
    }

    private fun loadPreferences() {
        selectedYear = sharedPreferences.getInt("selected_year", Calendar.getInstance().get(Calendar.YEAR))
        selectedMonth = sharedPreferences.getInt("selected_month", Calendar.getInstance().get(Calendar.MONTH) + 1) // MONTH is 0-based

        salaryInput.setText(sharedPreferences.getString("salary", ""))
        monthlyHoursInput.setText(sharedPreferences.getString("monthly_hours", ""))
        missedHoursInput.setText(sharedPreferences.getString("missed_hours", ""))
        allowancesPercentageInput.setText(sharedPreferences.getString("allowances_percentage", ""))
        monthlyBonusPercentageInput.setText(sharedPreferences.getString("monthly_bonus_percentage", ""))
        managerBonusInput.setText(sharedPreferences.getString("manager_bonus", ""))
    }
}
