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
            items(workoutRecords.sortedByDescending { it.date }) { record ->                 Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // 날짜 표시
                        Text(
                            text = "🗓 ${record.date} 운동 완료!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // 운동 기록 리스트
                        record.logs.forEach { log ->
                            Text("- ${log.name} (${log.part}): ${log.sets}세트")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 오운완 사진이 있을 경우 표시
                        record.imagePath?.let { path ->
                            Image(
                                painter = rememberAsyncImagePainter(path),
                                contentDescription = "운동 완료 사진",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(top = 8.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}
