package com.example.test.screenui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.test.model.WorkoutRecord

@Composable
fun HistoryScreen(workoutRecords: List<WorkoutRecord>) {
    if (workoutRecords.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("아직 운동 기록이 없습니다")
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(workoutRecords.sortedByDescending { it.date }) { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        val context = LocalContext.current
                        record.imagePath?.let { path ->
                            val isDrawable = !path.contains("/") // 경로에 슬래시 없으면 drawable 이름

                            val imageModifier = Modifier
                                .width(100.dp)
                                .height(100.dp)
                                .padding(end = 12.dp)

                            if (isDrawable) {
                                val resId = context.resources.getIdentifier(path, "drawable", context.packageName)
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
