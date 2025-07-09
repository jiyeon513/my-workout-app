package com.example.test.model

data class Exercise(
    val id: Int,
    val name: String,
    val description: String,
    val part: String
)

data class ExerciseLog(
    val name: String,
    val sets: Int,
    val date: String,
    val part: String
)

data class WorkoutRecord(
    val userId: String,
    val date: String,
    val logs: List<ExerciseLog>,
    val imagePath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class User(
    val id: String,
    val password: String,
    val age: Int? = null  // ✅ age를 nullable로 수정, 기본값 null
)

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val icon: String, // drawable 리소스 이름 (ex: badge_back)
    val isUnlocked: Boolean
)
