package com.example.test.screenui

import android.graphics.Color as GColor
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.test.model.ExerciseLog
import com.example.test.model.WorkoutRecord
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(workoutRecords: List<WorkoutRecord>) {
    var summary by remember { mutableStateOf("요약을 위해 버튼을 눌러주세요") }
    var partData by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("🤖 AI 피티쌤 분석", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    val allLogs = workoutRecords.flatMap { it.logs }
                    val (generatedSummary, generatedPartData) = withContext(Dispatchers.Default) {
                        generateSummaryAndData(allLogs)
                    }
                    summary = generatedSummary
                    partData = generatedPartData
                    isLoading = false
                }
            },
            enabled = !isLoading
        ) {
            Text("요약하기", color = Color.White)
        }

        Spacer(Modifier.height(24.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column {
                Box(Modifier.fillMaxWidth().border(1.dp, Color.Gray, RoundedCornerShape(12.dp)).padding(16.dp)) {
                    Column {
                        Text("📊 최근 운동 분석 요약", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        summary.split("\n").forEach { line ->
                            Text(line, fontSize = 15.sp, lineHeight = 20.sp)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                if (partData.isNotEmpty()) {
                    RadarChartView(partData)
                }
            }
        }
    }
}

fun generateSummaryAndData(logs: List<ExerciseLog>): Pair<String, Map<String, Int>> {
    if (logs.isEmpty()) return "운동 기록이 없습니다." to emptyMap()
    val totalSets = logs.sumOf { it.sets }
    val days = logs.map { it.date }.distinct().size
    val partCount = logs.groupBy { it.part }.mapValues { it.value.sumOf { it.sets } }
    val mostTargeted = partCount.maxByOrNull { it.value }?.key ?: "없음"
    val partSummary = partCount.entries.joinToString("\n") { "- ${it.key}: ${it.value}세트" }

    val summary = """
총 운동 세트 수: ${totalSets}세트
운동한 날짜 수: ${days}일
가장 많이 훈련한 부위: ${mostTargeted}

부위별 세트 분포:
${partSummary}

⚠️ 전신의 균형을 위해 부족한 부위를 보완해보세요!
""".trimIndent()

    return summary to partCount
}

@Composable
fun RadarChartView(data: Map<String, Int>) {
    val safeLabels = data.keys.map { if (it.isBlank()) "기타" else it }
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(300.dp),
        factory = { context ->
            RadarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(safeLabels)
                    textSize = 14f
                    position = XAxis.XAxisPosition.BOTTOM
                    textColor = GColor.DKGRAY
                }
                yAxis.apply {
                    axisMinimum = 0f
                    axisMaximum = (data.values.maxOrNull() ?: 0).toFloat() + 2
                    setDrawLabels(false)
                }
                val radarDataSet = RadarDataSet(
                    data.values.map { RadarEntry(it.toFloat()) }, "운동 부위"
                ).apply {
                    color = GColor.rgb(33, 150, 243)
                    fillColor = GColor.rgb(33, 150, 243)
                    setDrawFilled(true)
                    valueTextSize = 14f
                    lineWidth = 2f
                    setDrawValues(false)
                }
                this.data = RadarData(radarDataSet)
                animateXY(1500, 1500)
                invalidate()
            }
        }
    )
}
