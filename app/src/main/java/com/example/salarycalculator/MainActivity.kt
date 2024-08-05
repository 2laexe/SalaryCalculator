package com.example.salarycalculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private val TAX_RATE = 0.13
    private val UNION_FEE = 0.01

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val salaryInput = findViewById<TextInputEditText>(R.id.salary)
        val monthlyHoursInput = findViewById<TextInputEditText>(R.id.monthly_hours)
        val missedHoursInput = findViewById<TextInputEditText>(R.id.missed_hours)
        val allowancesPercentageInput = findViewById<TextInputEditText>(R.id.allowances_percentage)
        val monthlyBonusPercentageInput = findViewById<TextInputEditText>(R.id.monthly_bonus_percentage)
        val managerBonusInput = findViewById<TextInputEditText>(R.id.manager_bonus)
        val overtimeFirst2HoursInput = findViewById<TextInputEditText>(R.id.overtime_first_2_hours)
        val overtimeMoreThan2HoursInput = findViewById<TextInputEditText>(R.id.overtime_more_than_2_hours)
        val weekendWorkHoursInput = findViewById<TextInputEditText>(R.id.weekend_work_hours)
        val calculateButton = findViewById<Button>(R.id.calculate_button)
        val resultText = findViewById<TextView>(R.id.result_text)
        val workedHoursText = findViewById<TextView>(R.id.worked_hours_text)

        calculateButton.setOnClickListener {
            val salary = salaryInput.text.toString().toDoubleOrNull() ?: 0.0
            val monthlyHours = monthlyHoursInput.text.toString().toDoubleOrNull() ?: 0.0
            val missedHours = missedHoursInput.text.toString().toDoubleOrNull() ?: 0.0
            val allowancesPercentage = allowancesPercentageInput.text.toString().toDoubleOrNull() ?: 0.0
            val monthlyBonusPercentage = monthlyBonusPercentageInput.text.toString().toDoubleOrNull() ?: 0.0
            val managerBonus = managerBonusInput.text.toString().toDoubleOrNull() ?: 0.0
            val overtimeFirst2Hours = overtimeFirst2HoursInput.text.toString().toDoubleOrNull() ?: 0.0
            val overtimeMoreThan2Hours = overtimeMoreThan2HoursInput.text.toString().toDoubleOrNull() ?: 0.0
            val weekendWorkHours = weekendWorkHoursInput.text.toString().toDoubleOrNull() ?: 0.0

            // Расчет отработанных обычных часов
            val workedRegularHours = monthlyHours - missedHours

            // Расчет общего количества отработанных часов с учетом сверхурочных, без учета выходных
            val totalWorkedHours = workedRegularHours + overtimeFirst2Hours + overtimeMoreThan2Hours

            // Расчет пропорционального оклада, уменьшаем только за счет пропущенных часов
            val proportionalSalary = salary * (workedRegularHours / monthlyHours)
            val hourlyRate = proportionalSalary / monthlyHours

            // Оплата за регулярные часы работы (без сверхурочных и выходных)
            val regularPay = hourlyRate * workedRegularHours

            // Расчет надбавок и премий на основе отработанных часов, включая сверхурочные
            val allowances = (allowancesPercentage / 100) * salary * (totalWorkedHours / monthlyHours)
            val monthlyBonus = (monthlyBonusPercentage / 100) * salary * (totalWorkedHours / monthlyHours)

            // Расчет переработок
            val overtimeFirst2HoursPay = overtimeFirst2Hours * hourlyRate * 1.5
            val overtimeMoreThan2HoursPay = overtimeMoreThan2Hours * hourlyRate * 2.0

            // Расчет оплаты за работу в выходные
            val weekendWorkPay = weekendWorkHours * hourlyRate * 2.0

            // Общий доход до налогов
            val grossIncome = regularPay + allowances + monthlyBonus + managerBonus + overtimeFirst2HoursPay + overtimeMoreThan2HoursPay + weekendWorkPay

            // Расчет налогов и взносов
            val tax = grossIncome * TAX_RATE
            val unionFee = grossIncome * UNION_FEE

            // Чистый доход
            val netIncome = grossIncome - tax - unionFee

            // Отображение отработанных часов
            workedHoursText.text = "Отработанные часы: %.2f".format(totalWorkedHours)

            // Отображение результата
            resultText.text = """
                Пропорциональная зарплата: %.2f руб.
                Надбавки: %.2f руб.
                Ежемесячная премия: %.2f руб.
                Бонус менеджера: %.2f руб.
                Оплата сверхурочных (первые 2 часа): %.2f руб.
                Оплата сверхурочных (более 2 часов): %.2f руб.
                Оплата за работу в выходные: %.2f руб.
                Общий доход до налогов: %.2f руб.
                Налоги: %.2f руб.
                Профсоюзный взнос: %.2f руб.
                Чистый доход: %.2f руб.
            """.trimIndent().format(
                proportionalSalary, allowances, monthlyBonus, managerBonus, overtimeFirst2HoursPay,
                overtimeMoreThan2HoursPay, weekendWorkPay, grossIncome, tax, unionFee, netIncome
            )
        }
    }
}
