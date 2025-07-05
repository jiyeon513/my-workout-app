package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.test.model.ExerciseLog
import com.example.test.model.WorkoutRecord
import com.example.test.screenui.HomeScreen
import com.example.test.screenui.HistoryScreen
import com.example.test.screenui.SettingsScreen
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private val workoutRecords = mutableStateListOf<WorkoutRecord>()
    private var currentPage by mutableStateOf("home")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val loadedRecords = loadWorkoutRecordsFromFile()
        workoutRecords.addAll(loadedRecords)

        // 🔹 앱에 기록이 하나도 없을 경우, 테스트용 1달치 더미 데이터 추가
        if (workoutRecords.isEmpty()) {
            workoutRecords.addAll(generateDummyWorkoutRecords())
        }

        enableEdgeToEdge()
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                MyMultiPageApp()
            }
        }
    }

    private fun saveWorkoutRecordsToFile() {
        val gson = Gson()
        val json = gson.toJson(workoutRecords)
        val file = File(filesDir, "workout_records.json")
        file.writeText(json)
    }

    private fun loadWorkoutRecordsFromFile(): List<WorkoutRecord> {
        val file = File(filesDir, "workout_records.json")
        if (!file.exists()) return emptyList()
        val json = file.readText()
        val gson = Gson()
        val type = object : TypeToken<List<WorkoutRecord>>() {}.type
        return gson.fromJson(json, type)
    }

    @Composable
    fun MyMultiPageApp() {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentPage == "home",
                        onClick = { currentPage = "home" },
                        label = { Text("기록 생성") },
                        icon = { Text("🏋️") }
                    )
                    NavigationBarItem(
                        selected = currentPage == "history",
                        onClick = { currentPage = "history" },
                        label = { Text("일기장") },
                        icon = { Text("📔") }
                    )
                    NavigationBarItem(
                        selected = currentPage == "settings",
                        onClick = { currentPage = "settings" },
                        label = { Text("AI PT쌤") },
                        icon = { Text("🤖") }
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                when (currentPage) {
                    "home" -> HomeScreen { record ->
                        workoutRecords.add(record)
                        saveWorkoutRecordsToFile()
                    }
                    "history" -> HistoryScreen(workoutRecords)
                    "settings" -> SettingsScreen(workoutRecords)
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun AppPreview() {
        MyMultiPageApp()
    }

    // ✅ 테스트용 더미 운동 기록 생성 (최근 30일)
    private fun generateDummyWorkoutRecords(): List<WorkoutRecord> {
        val dummyExercises = listOf(
            "벤치프레스" to "가슴",
            "랫풀다운" to "등",
            "스쿼트" to "하체",
            "숄더 프레스" to "어깨",
            "크런치" to "복부"
        )

        val today = LocalDate.now()
        return (1 until 30).map { i ->
            val date = today.minusDays(i.toLong()).format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            val logs = dummyExercises.map { (name, part) ->
                ExerciseLog(name = name, sets = (2..5).random(), date = date, part = part)
            }
            WorkoutRecord(date = date, logs = logs, imagePath = null)
        }
    }
}
