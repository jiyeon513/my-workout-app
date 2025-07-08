package com.example.test

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.test.model.ExerciseLog
import com.example.test.model.WorkoutRecord
import com.example.test.model.Badge
import com.example.test.screenui.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.tooling.preview.Preview
import android.graphics.Bitmap
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.graphics.Color


class MainActivity : ComponentActivity() {
    private val workoutRecords = mutableStateListOf<WorkoutRecord>()
    private var currentUserId by mutableStateOf<String?>(null)
    private var currentPage by mutableStateOf("login")
    private var prevPage by mutableStateOf("home")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        copyDrawableToGallery(this, "before", "before_photo")
        copyDrawableToGallery(this, "after", "after_photo")

        val saved = loadWorkoutRecordsFromFile()
        workoutRecords.clear()
        workoutRecords.addAll(saved)

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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MyMultiPageApp() {
        val userRecords = workoutRecords.filter { it.userId == currentUserId }
        val pageTitle = when (currentPage) {
            "home" -> "  오늘의 운동 기록"
            "history" -> "   나의 운동 갤러리"
            "settings" -> "   운동 분석 차트"
            "mypage" -> "   마이페이지"
            else -> ""
        }
        Scaffold(

            topBar = {
                if (currentPage in listOf("home", "history", "settings")) {
                    TopAppBar(
                        modifier = Modifier
                            .padding(top = 50.dp),
                        title = {
                            Text(pageTitle, style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 24.sp, fontWeight = FontWeight.Bold))
                        },
                        actions = {
                            IconButton(onClick = {
                                prevPage = currentPage
                                currentPage = "mypage"
                            }) {
                                Icon(Icons.Default.AccountCircle, contentDescription = "마이페이지", tint = Color(0xFFBDBDBD), modifier = Modifier.size(32.dp))
                            }
                        },
                    )
                }
            },
            bottomBar = {
                if (currentPage !in listOf("login", "signup", "mypage")) {
                    NavigationBar {
                        NavigationBarItem(selected = currentPage == "home", onClick = { currentPage = "home" },
                            label = { Text("기록 생성") }, icon = { Text("🏋️") })
                        NavigationBarItem(selected = currentPage == "history", onClick = { currentPage = "history" },
                            label = { Text("일기장") }, icon = { Text("📔") })
                        NavigationBarItem(selected = currentPage == "settings", onClick = { currentPage = "settings" },
                            label = { Text("AI PT쌤") }, icon = { Text("🤖") })
                        NavigationBarItem(
                            selected = currentPage == "chat",
                            onClick = { currentPage = "chat" },
                            label = { Text("채팅") },
                            icon = { Text("💬") }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 50.dp,
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = innerPadding.calculateBottomPadding()
                )) {
                when (currentPage) {
                    "login" -> LoginScreen(
                        onLoginSuccess = { userId ->
                            currentUserId = userId
                            val userHasNoRecord = workoutRecords.none { it.userId == userId }
                            if (userHasNoRecord) {
                                val dummy = generateDummyWorkoutRecords(userId)
                                workoutRecords.addAll(dummy)
                                saveWorkoutRecordsToFile()
                            }
                            currentPage = "home"
                        },
                        onSignupClick = { currentPage = "signup" }
                    )
                    "signup" -> SignUpScreen(
                        onSignupSuccess = { userId ->
                            currentUserId = userId
                            currentPage = "login"
                        },
                        onBackClick = { currentPage = "login" }
                    )
                    "home" -> HomeScreen(
                        currentUserId = currentUserId!!,
                        userRecords = userRecords,
                        onRecordSaved = { record ->
                            val fullRecord = record.copy(userId = currentUserId!!)
                            workoutRecords.add(fullRecord)
                            saveWorkoutRecordsToFile()
                        }
                    )
                    "history" -> HistoryScreen(userRecords)
                    "settings" -> SettingsScreen(userRecords)
                    "mypage" -> MypageScreen(
                        allRecords = workoutRecords,
                        currentUserId = currentUserId!!,
                        onBackClick = { currentPage = prevPage }
                    )
                    "chat" -> ChatScreen()

                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun AppPreview() {
        MyMultiPageApp()
    }

    private fun generateDummyWorkoutRecords(userId: String): List<WorkoutRecord> {
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
                ExerciseLog(name = name, sets = (1..4).random(), date = dateStr, part = part)
            }
            WorkoutRecord(
                userId = userId,
                date = dateStr,
                logs = logs,
                imagePath = image,
                timestamp = dateObj.toEpochDay() * 1000L
            )
        }
    }

    private fun copyDrawableToGallery(context: Context, drawableName: String, displayName: String) {
        val resId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
        if (resId == 0) return
        val bitmap = BitmapFactory.decodeResource(context.resources, resId)
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "${displayName}_${System.currentTimeMillis()}.jpg")
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

    // ✅ 배지 계산 함수
    fun calculateBadges(records: List<WorkoutRecord>, userId: String): List<Badge> {
        val userRecords = records.filter { it.userId == userId }

        val totalBackSets = userRecords.flatMap { it.logs }
            .filter { it.part == "등" }
            .sumOf { it.sets }

        val totalLegReps = userRecords.flatMap { it.logs }
            .filter { it.part == "하체" }
            .sumOf { it.sets }


//        println("🔍 [DEBUG] 등 세트 수: $totalBackSets / 하체 세트 수: $totalLegSets")


        return listOf(
            Badge(
                id = "back_100",
                name = "등근육 장인",
                description = "등 운동 100세트 완료",
                icon = "badge_back",
                isUnlocked = totalBackSets >= 100
            ),
            Badge(
                id = "leg_50",
                name = "강철 하체",
                description = "하체 운동 50세트 완료",
                icon = "badge_leg",
                isUnlocked = totalLegReps >= 50
            )
        )
    }


}
