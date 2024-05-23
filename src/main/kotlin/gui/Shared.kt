package gui

// DaysOfWeekUtils.kt

val daysOfWeek = listOf(
    "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Holidays"
)

// Create a map from the daysOfWeek list
val daysOfWeekMap = daysOfWeek.mapIndexed { index, day -> day to index }.toMap()
