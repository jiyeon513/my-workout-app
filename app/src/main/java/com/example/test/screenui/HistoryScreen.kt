package com.example.test.screenui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.test.model.WorkoutRecord
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import androidx.compose.ui.unit.LayoutDirection

enum class HistoryViewMode {
    LIST, GALLERY, COMPARE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(workoutRecords: List<WorkoutRecord>) {
    var viewMode by remember { mutableStateOf(HistoryViewMode.LIST) }
    var showDropdown by remember { mutableStateOf(false) }
    var showDropdown2 by remember { mutableStateOf(false) }
    var selectedComparisonRecord by remember { mutableStateOf<WorkoutRecord?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "  나의 운동 일기",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = 0.dp
                )
                .fillMaxSize()
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Button(
                        onClick = { showDropdown = true },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("보기 모드 선택")
                    }

                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("리스트 모드") },
                            onClick = {
                                viewMode = HistoryViewMode.LIST
                                showDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("갤러리 모드") },
                            onClick = {
                                viewMode = HistoryViewMode.GALLERY
                                showDropdown = false
                            }
                        )
                    }
                }

                Button(
                    onClick = { viewMode = HistoryViewMode.COMPARE },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("운동 기록 비교")
                }
            }

            if (workoutRecords.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("아직 운동 기록이 없습니다")
                }
            } else {
                val sortedRecords = workoutRecords.sortedByDescending { it.timestamp }
                val latestRecord = sortedRecords.firstOrNull()

                when (viewMode) {
                    HistoryViewMode.COMPARE -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (latestRecord != null) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .padding(bottom = 16.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F0FB))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        val context = LocalContext.current
                                        latestRecord.imagePath?.let { path ->
                                            val isDrawable = !path.contains("/")
                                            val imageModifier = Modifier
                                                .width(100.dp)
                                                .height(100.dp)
                                                .padding(end = 12.dp)
                                                .clip(RoundedCornerShape(12.dp))

                                            if (isDrawable) {
                                                val resId = context.resources.getIdentifier(
                                                    path, "drawable", context.packageName
                                                )
                                                if (resId != 0) {
                                                    Image(
                                                        painter = painterResource(resId),
                                                        contentDescription = null,
                                                        modifier = imageModifier,
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                            } else {
                                                Image(
                                                    painter = rememberAsyncImagePainter(path),
                                                    contentDescription = null,
                                                    modifier = imageModifier,
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "📅 최근 운동 기록",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            latestRecord.logs.forEach { log ->
                                                Text("- ${log.name} (${log.part}): ${log.sets}세트")
                                            }
                                        }
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Button(
                                        onClick = { showDropdown2 = true },
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Text("📅 날짜 선택", fontSize = 16.sp)
                                    }
                                    DropdownMenu(
                                        expanded = showDropdown2,
                                        onDismissRequest = { showDropdown2 = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("1개월 전") },
                                            onClick = {
                                                selectedComparisonRecord = findClosestRecord(sortedRecords, 1)
                                                showDropdown2 = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("3개월 전") },
                                            onClick = {
                                                selectedComparisonRecord = findClosestRecord(sortedRecords, 3)
                                                showDropdown2 = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("6개월 전") },
                                            onClick = {
                                                selectedComparisonRecord = findClosestRecord(sortedRecords, 6)
                                                showDropdown2 = false
                                            }
                                        )
                                    }
                                }
                            }

                            selectedComparisonRecord?.let { record ->
                                Spacer(modifier = Modifier.height(16.dp))
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        val context = LocalContext.current
                                        record.imagePath?.let { path ->
                                            val isDrawable = !path.contains("/")
                                            val imageModifier = Modifier
                                                .width(100.dp)
                                                .height(100.dp)
                                                .padding(end = 12.dp)
                                                .clip(RoundedCornerShape(12.dp))

                                            if (isDrawable) {
                                                val resId = context.resources.getIdentifier(
                                                    path, "drawable", context.packageName
                                                )
                                                if (resId != 0) {
                                                    Image(
                                                        painter = painterResource(resId),
                                                        contentDescription = null,
                                                        modifier = imageModifier,
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                            } else {
                                                Image(
                                                    painter = rememberAsyncImagePainter(path),
                                                    contentDescription = null,
                                                    modifier = imageModifier,
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "📅 ${record.date} 운동 기록",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            record.logs.forEach { log ->
                                                Text("- ${log.name} (${log.part}): ${log.sets}세트")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        when (viewMode) {
                            HistoryViewMode.LIST -> {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(sortedRecords) { record ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp)),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                val context = LocalContext.current
                                                record.imagePath?.let { path ->
                                                    val isDrawable = !path.contains("/")
                                                    val imageModifier = Modifier
                                                        .width(100.dp)
                                                        .height(100.dp)
                                                        .padding(end = 12.dp)
                                                        .clip(RoundedCornerShape(12.dp))

                                                    if (isDrawable) {
                                                        val resId = context.resources.getIdentifier(
                                                            path, "drawable", context.packageName
                                                        )
                                                        if (resId != 0) {
                                                            Image(
                                                                painter = painterResource(resId),
                                                                contentDescription = null,
                                                                modifier = imageModifier,
                                                                contentScale = ContentScale.Crop
                                                            )
                                                        }
                                                    } else {
                                                        Image(
                                                            painter = rememberAsyncImagePainter(path),
                                                            contentDescription = null,
                                                            modifier = imageModifier,
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    }
                                                }

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "📅 ${record.date} 운동 기록",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    record.logs.forEach { log ->
                                                        Text("- ${log.name} (${log.part}): ${log.sets}세트")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }


                            HistoryViewMode.GALLERY -> {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(sortedRecords) { record ->
                                        val context = LocalContext.current
                                        val isDrawable = record.imagePath?.contains("/")?.not() ?: false
                                        val imageModifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(12.dp))

                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .padding(4.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEEE7F8))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                if (record.imagePath != null) {
                                                    if (isDrawable) {
                                                        val resId = context.resources.getIdentifier(
                                                            record.imagePath, "drawable", context.packageName
                                                        )
                                                        if (resId != 0) {
                                                            Image(
                                                                painter = painterResource(resId),
                                                                contentDescription = null,
                                                                modifier = imageModifier,
                                                                contentScale = ContentScale.Crop
                                                            )
                                                        }
                                                    } else {
                                                        Image(
                                                            painter = rememberAsyncImagePainter(record.imagePath),
                                                            contentDescription = null,
                                                            modifier = imageModifier,
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = record.date,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            else -> {}
                        }
                    }

                }
            }
        }
    }
}

fun findClosestRecord(records: List<WorkoutRecord>, monthsAgo: Int): WorkoutRecord? {
    val targetDate = LocalDate.now().minusMonths(monthsAgo.toLong())
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    return records.minByOrNull {
        val date = LocalDate.parse(it.date, formatter)
        ChronoUnit.DAYS.between(date, targetDate).let { Math.abs(it) }
    }
}
