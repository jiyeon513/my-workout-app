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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.test.model.WorkoutRecord
import androidx.compose.ui.unit.LayoutDirection


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(workoutRecords: List<WorkoutRecord>) {
    var isGalleryMode by remember { mutableStateOf(false) }

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
                    bottom = 0.dp // 하단 패딩 제거
                )
                .fillMaxSize()
        ) {

            // 제목 아래 보라색 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(
                    onClick = { isGalleryMode = !isGalleryMode },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(if (isGalleryMode) "전체 보기" else "사진만 보기")
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

                if (isGalleryMode) {
                    // 갤러리 모드 (그리드 2열)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sortedRecords) { record ->
                            val context = LocalContext.current
                            record.imagePath?.let { path ->
                                val isDrawable = !path.contains("/")
                                val imageModifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)

                                if (isDrawable) {
                                    val resId = context.resources.getIdentifier(
                                        path, "drawable", context.packageName
                                    )
                                    if (resId != 0) {
                                        Image(
                                            painter = painterResource(resId),
                                            contentDescription = null,
                                            modifier = imageModifier.clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                } else {
                                    Image(
                                        painter = rememberAsyncImagePainter(path),
                                        contentDescription = null,
                                        modifier = imageModifier.clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // 리스트 모드
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(sortedRecords) { record ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6))
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

                                        if (isDrawable) {
                                            val resId = context.resources.getIdentifier(
                                                path,
                                                "drawable",
                                                context.packageName
                                            )
                                            if (resId != 0) {
                                                Image(
                                                    painter = painterResource(resId),
                                                    contentDescription = "운동 완료 사진",
                                                    modifier = imageModifier,
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        } else {
                                            Image(
                                                painter = rememberAsyncImagePainter(path),
                                                contentDescription = "운동 완료 사진",
                                                modifier = imageModifier,
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "🗓 ${record.date} 운동 완료!",
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
            }
        }
    }
}
