package com.example.test.screenui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.model.Exercise
import com.example.test.model.ExerciseLog
import com.example.test.model.WorkoutRecord
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun HomeScreen(onRecordSaved: (WorkoutRecord) -> Unit) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showSnackbar by remember { mutableStateOf(false) }

    val exerciseList = listOf(
        Exercise(1, "벤치프레스", "가슴 근육을 키우는 대표 운동", "가슴"),
        Exercise(2, "딥스", "하부 가슴 자극", "가슴"),
        Exercise(3, "푸쉬업", "기본적인 체중 가슴 운동", "가슴"),
        Exercise(4, "랫풀다운", "등의 광배근을 자극", "등"),
        Exercise(5, "바벨로우", "전체 등 근육을 강화", "등"),
        Exercise(6, "숄더 프레스", "어깨 전면 근육 발달", "어깨"),
        Exercise(7, "스쿼트", "하체 전반에 자극", "하체"),
        Exercise(8, "런지", "하체 균형과 근력 강화", "하체"),
        Exercise(9, "크런치", "복부 상부 자극", "복부"),
        Exercise(10, "레그레이즈", "복부 하부를 자극", "복부"),
        Exercise(11, "플랭크", "복부와 코어 안정성 강화", "복부")
    )

    var selectedPart by remember { mutableStateOf("") }
    var showPartDropdown by remember { mutableStateOf(false) }
    val selectedExercises = remember { mutableStateMapOf<Exercise, Int>() }
    val favorites = remember { mutableStateListOf<Int>() }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            val logs = selectedExercises.map { (exercise, sets) ->
                ExerciseLog(name = exercise.name, sets = sets, date = today, part = exercise.part)
            }
            val imagePath = copyUriToInternalStorage(context, uri)
            val record = WorkoutRecord(date = today, logs = logs, imagePath = imagePath, timestamp = System.currentTimeMillis())
            onRecordSaved(record)
            selectedExercises.clear()
            showSnackbar = true
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            val logs = selectedExercises.map { (exercise, sets) ->
                ExerciseLog(name = exercise.name, sets = sets, date = today, part = exercise.part)
            }
            val imagePath = saveBitmapToInternalStorage(context, bitmap)
            val record = WorkoutRecord(date = today, logs = logs, imagePath = imagePath)
            onRecordSaved(record)
            selectedExercises.clear()
            showSnackbar = true
        }
    }

    val filteredList = when (selectedTabIndex) {
        0 -> when (selectedPart) {
            "" -> exerciseList
            "즐겨찾기" -> exerciseList.filter { it.id in favorites }
            else -> exerciseList.filter { it.part == selectedPart }
        }
        1 -> selectedExercises.keys.toList()
        else -> emptyList()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    snackbarData = data
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = 0.dp
                )
        ) {

            // Snackbar 실행
            LaunchedEffect(showSnackbar) {
                if (showSnackbar) {
                    val snackbarJob = snackbarHostState.showSnackbar(
                        message = " ✅ 기록 생성이 완료되었습니다!"
                    )
                    delay(1000) // 2초 후 자동 종료
                    snackbarHostState.currentSnackbarData?.dismiss()
                    showSnackbar = false
                }
            }


            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }) {
                    Text("전체 보기", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }) {
                    Text("선택 보기", modifier = Modifier.padding(16.dp))
                }
            }

            if (selectedTabIndex == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Button(onClick = { showPartDropdown = true }) {
                        Text(if (selectedPart.isEmpty()) "필터: 전체" else "필터: $selectedPart")
                    }

                    DropdownMenu(expanded = showPartDropdown, onDismissRequest = { showPartDropdown = false }) {
                        DropdownMenuItem(text = { Text("전체") }, onClick = {
                            selectedPart = ""
                            showPartDropdown = false
                        })
                        listOf("즐겨찾기", "가슴", "등", "어깨", "하체", "복부").forEach { part ->
                            DropdownMenuItem(
                                text = { Text(part) },
                                onClick = {
                                    selectedPart = part
                                    showPartDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                items(filteredList) { exercise ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if ((selectedExercises[exercise] ?: 0) > 0) Color(0xFFBBDEFB) else Color(0xFFF8F9FA)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${exercise.name} (${exercise.part})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconToggleButton(
                                    checked = exercise.id in favorites,
                                    onCheckedChange = {
                                        if (exercise.id in favorites) favorites.remove(exercise.id)
                                        else favorites.add(exercise.id)
                                    }
                                ) {
                                    Text(if (exercise.id in favorites) "★" else "☆", fontSize = 20.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(exercise.description)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("세트 수 :   ", fontWeight = FontWeight.SemiBold)
                                var expanded by remember { mutableStateOf(false) }
                                Box {
                                    Button(
                                        onClick = { expanded = true },
                                        modifier = Modifier
                                            .height(28.dp)
                                            .width(70.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            if ((selectedExercises[exercise] ?: 0) == 0) "선택"
                                            else "${selectedExercises[exercise]}세트",
                                            fontSize = 14.sp
                                        )
                                    }

                                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        (1..5).forEach { count ->
                                            DropdownMenuItem(
                                                text = { Text("$count 세트") },
                                                onClick = {
                                                    selectedExercises[exercise] = count
                                                    expanded = false
                                                }
                                            )
                                        }
                                        DropdownMenuItem(
                                            text = { Text("선택 취소") },
                                            onClick = {
                                                selectedExercises.remove(exercise)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📷")
                            Text(text = " 사진 선택", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    },
                    text = { Text("사진 첨부 방식을 선택해 주세요") },
                    confirmButton = {
                        TextButton(onClick = {
                            showDialog = false
                            galleryLauncher.launch("image/*")
                        }) {
                            Text("갤러리")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDialog = false
                            cameraLauncher.launch(null)
                        }) {
                            Text("카메라")
                        }
                    }
                )
            }

            Button(
                onClick = {
                    if (selectedExercises.isNotEmpty()) {
                        showDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = selectedExercises.isNotEmpty()
            ) {
                Text("✅ 오늘의 운동 기록 생성")
            }
        }
    }
}

fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String {
    val filename = "IMG_${System.currentTimeMillis()}.jpg"
    val file = File(context.filesDir, filename)
    val outputStream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    outputStream.flush()
    outputStream.close()
    return file.absolutePath
}

fun copyUriToInternalStorage(context: Context, uri: Uri): String {
    val inputStream = context.contentResolver.openInputStream(uri)!!
    val filename = "IMG_${System.currentTimeMillis()}.jpg"
    val file = File(context.filesDir, filename)
    val outputStream = FileOutputStream(file)

    inputStream.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }

    return file.absolutePath
}
