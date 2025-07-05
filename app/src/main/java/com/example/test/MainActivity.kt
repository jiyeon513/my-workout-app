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
import com.example.test.screenui.HomeScreen
import com.example.test.screenui.HistoryScreen
import com.example.test.screenui.SettingsScreen
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class MainActivity : ComponentActivity() {

    private val exerciseLogs = mutableStateListOf<ExerciseLog>()
    private var currentPage by mutableStateOf("home")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 앱 시작 시 저장된 기록 불러오기
        val loadedLogs = loadExerciseLogsFromFile()
        exerciseLogs.addAll(loadedLogs)

        enableEdgeToEdge()

        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                MyMultiPageApp()
            }
        }
    }

    // ✅ JSON으로 파일 저장
    private fun saveExerciseLogsToFile() {
        val gson = Gson()
        val json = gson.toJson(exerciseLogs)
        val file = File(filesDir, "exercise_logs.json")
        file.writeText(json)
    }

    // ✅ JSON 파일에서 로드
    private fun loadExerciseLogsFromFile(): List<ExerciseLog> {
        val file = File(filesDir, "exercise_logs.json")
        if (!file.exists()) return emptyList()

        val json = file.readText()
        val gson = Gson()
        val type = object : TypeToken<List<ExerciseLog>>() {}.type
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
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)) {
                when (currentPage) {
                    "home" -> HomeScreen { logs ->
                        exerciseLogs.addAll(logs)
                        saveExerciseLogsToFile() // 기록 저장
                    }
                    "history" -> HistoryScreen(exerciseLogs)
                    "settings" -> SettingsScreen()
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun AppPreview() {
        MyMultiPageApp()
    }
}

