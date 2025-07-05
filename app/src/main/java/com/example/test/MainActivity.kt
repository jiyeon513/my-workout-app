package com.example.test

import android.os.Bundle
import android.content.ContentValues
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.content.Context
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
import android.graphics.Bitmap


class MainActivity : ComponentActivity() {
    private val workoutRecords = mutableStateListOf<WorkoutRecord>()
    private var currentPage by mutableStateOf("home")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 갤러리에 비포/애프터 이미지 복사
        copyDrawableToGallery(this, "before", "before_photo")
        copyDrawableToGallery(this, "after", "after_photo")

        // ✅ 강제 덮어쓰기 (개발용)
        val dummy = generateDummyWorkoutRecords()
        workoutRecords.clear()
        workoutRecords.addAll(dummy)
        saveWorkoutRecordsToFile()

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

    // ✅ 더미 생성 함수 (5일 간격, 날짜별 before/after 이미지 분기)
    private fun generateDummyWorkoutRecords(): List<WorkoutRecord> {
        val dummyExercises = listOf(
            "벤치프레스" to "가슴",
            "랫풀다운" to "등",
            "스쿼트" to "하체",
            "숄더 프레스" to "어깨",
            "크런치" to "복부"
        )

        val today = LocalDate.now()
        return (1..150 step 5).map { i ->
            val dateObj = today.minusDays(i.toLong())
            val dateStr = dateObj.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))

            val threshold = LocalDate.of(2025, 6, 1)
            val image = if (dateObj.isAfter(threshold)) "after" else "before"

            val logs = dummyExercises.map { (name, part) ->
                ExerciseLog(name = name, sets = (2..5).random(), date = dateStr, part = part)
            }

            WorkoutRecord(date = dateStr, logs = logs, imagePath = image, timestamp = dateObj.toEpochDay() * 1000L)
        }
    }

    // ✅ drawable 리소스를 갤러리에 복사
    private fun copyDrawableToGallery(context: Context, drawableName: String, displayName: String) {
        val resId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
        if (resId == 0) return

        val bitmap = BitmapFactory.decodeResource(context.resources, resId)

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
        }
    }
}
