package com.example.test.model


data class Exercise(val id: Int, val name: String, val description: String, val part: String)
data class ExerciseLog(val name: String, val sets: Int, val date: String, val part: String)

data class WorkoutRecord(
    val date: String,
    val logs: List<ExerciseLog>,
    val imagePath: String? = null
)