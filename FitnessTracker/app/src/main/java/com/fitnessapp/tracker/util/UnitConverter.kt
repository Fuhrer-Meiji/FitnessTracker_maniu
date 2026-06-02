package com.fitnessapp.tracker.util

object UnitConverter {
    const val KG_TO_LB = 2.20462

    fun kgToLb(kg: Double): Double = kg * KG_TO_LB
    fun lbToKg(lb: Double): Double = lb / KG_TO_LB

    fun formatWeight(weight: Double, unit: String): String {
        return String.format("%.1f %s", weight, unit)
    }

    fun displayWeight(value: Double, currentUnit: String, storedInKg: Boolean = true): Double {
        return if (currentUnit == "kg") {
            if (storedInKg) value else lbToKg(value)
        } else {
            if (storedInKg) kgToLb(value) else value
        }
    }
}
