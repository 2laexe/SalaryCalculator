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
    private lateinit var middlemonthlyHoursInput: TextInputEditText
    private lateinit var missedHoursInput: TextInputEditText
    private lateinit var allowancesPercentageInput: TextInputEditText
    private lateinit var monthlyBonusPercentageInput: TextInputEditText
    private lateinit var managerBonusInput: TextInputEditText
    private lateinit var calculateButton: Button
    private lateinit var workedHoursText: TextView
    private lateinit var resultText: TextView
    private lateinit var calendarButton: Button
    private lateinit var overtimeFirst1HoursText: TextView
    private lateinit var overtimeFirst2HoursText: TextView
    private lateinit var overtimeMoreThan2HoursText: TextView
    private lateinit var weekendHoursText: TextView

    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0

    private val TAX_RATE = 0.13
    private val UNION_FEE = 0.01
    private val evening_rate = 0.2
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
        middlemonthlyHoursInput = findViewById(R.id.middle_monthly_hours)
        missedHoursInput = findViewById(R.id.missed_hours)
        allowancesPercentageInput = findViewById(R.id.allowances_percentage)
        monthlyBonusPercentageInput = findViewById(R.id.monthly_bonus_percentage)
        managerBonusInput = findViewById(R.id.manager_bonus)
        calculateButton = findViewById(R.id.calculate_button)
        workedHoursText = findViewById(R.id.worked_hours_text)
        resultText = findViewById(R.id.result_text)
        calendarButton = findViewById(R.id.calendar_button)
        overtimeFirst1HoursText = findViewById(R.id.overtime_first_1_hours_text)
        overtimeFirst2HoursText = findViewById(R.id.overtime_first_2_hours_text)
        overtimeMoreThan2HoursText = findViewById(R.id.overtime_more_than_2_hours_text)
        weekendHoursText = findViewById(R.id.weekend_hours_text)

        setupSpinners()
        loadPreferences()

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
                loadPreferences() // Load data for the selected year and month
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedMonth = months[position].toInt()
                loadPreferences() // Load data for the selected year and month
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
        val middlemonthlyHours = middlemonthlyHoursInput.text.toString().toDoubleOrNull() ?: 0.0
        val missedHours = missedHoursInput.text.toString().toDoubleOrNull() ?: 0.0
        val allowancesPercentage = allowancesPercentageInput.text.toString().toDoubleOrNull() ?: 0.0
        val monthlyBonusPercentage = monthlyBonusPercentageInput.text.toString().toDoubleOrNull() ?: 0.0
        val managerBonus = managerBonusInput.text.toString().toDoubleOrNull() ?: 0.0

        var overtimeFirst1Hours = 0.0
        var overtimeFirst2Hours = 0.0
        var overtimeMoreThan2Hours = 0.0
        var weekendWorkHours = 0.0

        for (day in 1..31) {
            val dateKey = "$selectedYear-${String.format("%02d", selectedMonth)}-${String.format("%02d", day)}"
            overtimeFirst1Hours += sharedPreferences.getFloat("${dateKey}_overtime_first_1_hours", 0.0f).toDouble()
            overtimeFirst2Hours += sharedPreferences.getFloat("${dateKey}_overtime_first_2_hours", 0.0f).toDouble()
            overtimeMoreThan2Hours += sharedPreferences.getFloat("${dateKey}_overtime_more_than_2_hours", 0.0f).toDouble()
            weekendWorkHours += sharedPreferences.getFloat("${dateKey}_weekend_hours", 0.0f).toDouble()
        }

        overtimeFirst1HoursText.text = "Сверхурочные до 6: $overtimeFirst1Hours"
        overtimeFirst2HoursText.text = "Сверхурочные до 7: $overtimeFirst2Hours"
        overtimeMoreThan2HoursText.text = "Сверхурочные (более 2 часов): $overtimeMoreThan2Hours"
        weekendHoursText.text = "Часы работы в выходные дни: $weekendWorkHours"

        val workedHours = monthlyHours - missedHours
        workedHoursText.text = "Отработанные часы: $workedHours"

        // Расчет всех часов
        val allHours = workedHours + overtimeFirst1Hours + overtimeFirst2Hours + overtimeMoreThan2Hours + weekendWorkHours
        findViewById<TextView>(R.id.all_hours_text).text = "Всего часов: $allHours"

        // Расчет оклада по должности
        val proportionalSalary = (salary / monthlyHours) * workedHours

        // Расчет надбавок и премий
        val allowances = (allowancesPercentage / 100) * proportionalSalary
        val monthlyBonus = (proportionalSalary / workedHours * allHours) * (monthlyBonusPercentage / 100)

        // Расчет переработок и работы в выходные на основе пропорционального оклада
        val overtimeFirst1HoursPay = ((proportionalSalary + allowances) / middlemonthlyHours) * (overtimeFirst1Hours * 1.5) // Часы переработки до 6
        val overtimeFirst2HoursPay = ((proportionalSalary + allowances) / middlemonthlyHours) * (overtimeFirst2Hours * 1.5) // Часы переработки до 7
        val overtimeMoreThan2HoursPay = ((proportionalSalary + allowances) / middlemonthlyHours) * (overtimeMoreThan2Hours * 2)  // Часы переработки (более 2 часов)
        val weekendWorkPay = ((proportionalSalary + allowances) / workedHours) * (weekendWorkHours * 2.0) // Часы работы в выходные
        val evening = ((proportionalSalary / workedHours) * evening_rate) * (overtimeFirst1Hours + overtimeMoreThan2Hours)
        val totalEarnings = proportionalSalary + allowances + monthlyBonus + managerBonus + overtimeFirst1HoursPay + overtimeFirst2HoursPay + overtimeMoreThan2HoursPay + weekendWorkPay + evening
        val taxDeduction = totalEarnings * TAX_RATE
        val unionFeeDeduction = totalEarnings * UNION_FEE

        val netSalary = totalEarnings - taxDeduction - unionFeeDeduction

        // Воспроизведение звука, если общий доход больше 100000 руб.
        if (totalEarnings > 100000) {
            val mediaPlayer = MediaPlayer.create(this, R.raw.high_income_sound)
            mediaPlayer.start()
        }

        resultText.text = """
            Оклад по должности: %.2f руб.
            Надбавки: %.2f руб.
            Ежемесячная премия: %.2f руб.
            Премия руководителя: %.2f руб.
            Оплата сверхурочных до 6: %.2f руб.
            Оплата сверхурочных до 7: %.2f руб.
            Оплата сверхурочных (более 2 часов): %.2f руб.
            Оплата вечерних: %.2f руб.
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
            overtimeFirst1HoursPay,
            overtimeFirst2HoursPay,
            overtimeMoreThan2HoursPay,
            evening,
            weekendWorkPay,
            totalEarnings,
            taxDeduction,
            unionFeeDeduction,
            netSalary
        )
    }

    private fun loadPreferences() {
        val keyPrefix = "$selectedYear-$selectedMonth"

        salaryInput.setText(sharedPreferences.getString("${keyPrefix}_salary", "0"))
        monthlyHoursInput.setText(sharedPreferences.getString("${keyPrefix}_monthly_hours", "0"))
        middlemonthlyHoursInput.setText(sharedPreferences.getString("${keyPrefix}_middlemonthly_hours", "0"))
        missedHoursInput.setText(sharedPreferences.getString("${keyPrefix}_missed_hours", "0"))
        allowancesPercentageInput.setText(sharedPreferences.getString("${keyPrefix}_allowances_percentage", "0"))
        monthlyBonusPercentageInput.setText(sharedPreferences.getString("${keyPrefix}_monthly_bonus_percentage", "0"))
        managerBonusInput.setText(sharedPreferences.getString("${keyPrefix}_manager_bonus", "0"))
    }

    private fun savePreferences() {
        val keyPrefix = "$selectedYear-$selectedMonth"

        val editor = sharedPreferences.edit()
        editor.putInt("selected_year", selectedYear)
        editor.putInt("selected_month", selectedMonth)
        editor.putString("${keyPrefix}_salary", salaryInput.text.toString())
        editor.putString("${keyPrefix}_monthly_hours", monthlyHoursInput.text.toString())
        editor.putString("${keyPrefix}_middlemonthly_hours", middlemonthlyHoursInput.text.toString())
        editor.putString("${keyPrefix}_missed_hours", missedHoursInput.text.toString())
        editor.putString("${keyPrefix}_allowances_percentage", allowancesPercentageInput.text.toString())
        editor.putString("${keyPrefix}_monthly_bonus_percentage", monthlyBonusPercentageInput.text.toString())
        editor.putString("${keyPrefix}_manager_bonus", managerBonusInput.text.toString())
        editor.apply()
    }

    override fun onStop() {
        super.onStop()
        savePreferences()
    }
}
