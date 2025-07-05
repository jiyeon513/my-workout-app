package com.example.test.screenui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.model.Exercise
import com.example.test.model.ExerciseLog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(onLogSaved: (List<ExerciseLog>) -> Unit) {
    val exerciseList = listOf(
        Exercise(1, "벤치프레스", "가슴 근육을 키우는 대표 운동", "가슴"),
        Exercise(2, "딥스", "하부 가슴 자극", "가슴"),
        Exercise(3, "푸쉬업", "기본적인 체중 가슴 운동", "가슴"),
        Exercise(4, "랫풀다운", "등의 광배근을 자극", "등"),
        Exercise(5, "바벨로우", "전체 등 근육을 강화", "등"),
        Exercise(6, "숄더 프레스", "어깨 전면 근육 발달", "어깨"),
        Exercise(7, "스쿼트", "하체 전반에 자극", "하체"),
        Exercise(8, "런지", "하체 균형과 근력 강화", "하체")
    )

    var selectedPart by remember { mutableStateOf("") }
    var showPartDropdown by remember { mutableStateOf(false) }
    val selectedExercises = remember { mutableStateMapOf<Exercise, Int>() }
    val favorites = remember { mutableStateListOf<Int>() }

    var selectedTabIndex by remember { mutableStateOf(0) }

    val filteredList = when (selectedTabIndex) {
        0 -> when (selectedPart) {
            "" -> exerciseList
            "즐겨찾기" -> exerciseList.filter { it.id in favorites }
            else -> exerciseList.filter { it.part == selectedPart }
        }
        1 -> selectedExercises.keys.toList()
        else -> emptyList()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
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

                DropdownMenu(
                    expanded = showPartDropdown,
                    onDismissRequest = { showPartDropdown = false }
                ) {
                    DropdownMenuItem(text = { Text("전체") }, onClick = {
                        selectedPart = ""
                        showPartDropdown = false
                    })
                    listOf("즐겨찾기", "가슴", "등", "어깨", "하체").forEach { part ->
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
                ExerciseRowWithSetSelector(
                    exercise = exercise,
                    selectedSet = selectedExercises[exercise] ?: 0,
                    isFavorite = exercise.id in favorites,
                    onFavoriteToggle = {
                        if (exercise.id in favorites) favorites.remove(exercise.id)
                        else favorites.add(exercise.id)
                    },
                    onSetChange = { set ->
                        if (set == 0) selectedExercises.remove(exercise)
                        else selectedExercises[exercise] = set
                    }
                )
            }
        }

        Button(
            onClick = {
                val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                val logs = selectedExercises.map { (exercise, sets) ->
                    ExerciseLog(name = exercise.name, sets = sets, date = today)
                }
                onLogSaved(logs)
                selectedExercises.clear()
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

@Composable
fun ExerciseRowWithSetSelector(
    exercise: Exercise,
    selectedSet: Int,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onSetChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(4.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selectedSet > 0) Color(0xFFBBDEFB) else Color(0xFFF8F9FA)
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
                    checked = isFavorite,
                    onCheckedChange = { onFavoriteToggle() }
                ) {
                    Text(if (isFavorite) "★" else "☆", fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(exercise.description)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("세트 수: ", fontWeight = FontWeight.SemiBold)
                SetDropdown(selectedSet, onSetChange)
            }
        }
    }
}

@Composable
fun SetDropdown(selected: Int, onSetChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(
            onClick = { expanded = true },
            modifier = Modifier
                .height(32.dp)
                .width(70.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(if (selected == 0) "선택" else "${selected}세트", fontSize = 15.sp)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            (1..5).forEach { count ->
                DropdownMenuItem(
                    text = { Text("$count 세트") },
                    onClick = {
                        onSetChange(count)
                        expanded = false
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("선택 취소") },
                onClick = {
                    onSetChange(0)
                    expanded = false
                }
            )
        }
    }
}