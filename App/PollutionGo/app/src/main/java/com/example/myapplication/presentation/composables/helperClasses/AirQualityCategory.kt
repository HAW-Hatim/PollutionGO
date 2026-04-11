package com.example.myapplication.presentation.composables.helperClasses

import androidx.compose.ui.graphics.Color
import org.maplibre.compose.expressions.ast.Expression
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.rgbColor
import org.maplibre.compose.expressions.value.ColorValue

// Categories in order (low to high)
enum class AirQualityCategory(
    val displayName: String,
    val color: Color,
    val rgbColor: Expression<ColorValue>// You can use your AQI colors here
) {
    GOOD("Good", Color(0xFF00E400), rgbColor(const(0), const(218), const(0)) ),
    MODERATE("Moderate", Color(0xFFFFFF00), rgbColor(const(255), const(255), const(0))),
    POLLUTED("Polluted", Color(0xFFFF7E00), rgbColor(const(255), const(126), const(0))),
    VERY_POLLUTED("Very Polluted", Color(0xFF8F3F97), rgbColor(const(255), const(0), const(0)) ),
    SEVERELY_POLLUTED("Severely Polluted", Color(0xFF7E0023), rgbColor(const(126), const(0), const(60)) )
}

// Thresholds: for each category, define the inclusive upper bound.
// The first category starts at 0, last goes to infinity.
data class PollutantScale(
    val name: String,
    val unit: String,
    val thresholds: List<Int>  // size = categories.size - 1
) {
    // Get the range description for a given category index
    fun getRangeString(categoryIndex: Int): String {
        require(categoryIndex in 0..thresholds.size)
        val lower = if (categoryIndex == 0) 0.0 else thresholds[categoryIndex - 1]
        val upper = if (categoryIndex < thresholds.size) thresholds[categoryIndex] else null
        return when (upper) {
            null -> "> $lower $unit"          // last category
            else -> if (lower == 0.0) "< $upper $unit"
            else "$lower – $upper $unit"
        }
    }

    fun getThreshold(airQualityCategory: AirQualityCategory): Number{
        val lowerLimit = when(airQualityCategory){
            AirQualityCategory.GOOD -> 0
            AirQualityCategory.MODERATE -> thresholds[0]
            AirQualityCategory.POLLUTED -> thresholds[1]
            AirQualityCategory.VERY_POLLUTED -> thresholds[2]
            AirQualityCategory.SEVERELY_POLLUTED -> thresholds[3]
        }

        return lowerLimit
    }

    // Optionally, find category for a given value
    fun getCategory(value: Double): Int {
        return thresholds.indexOfFirst { value <= it }.takeIf { it >= 0 } ?: thresholds.size
    }
}