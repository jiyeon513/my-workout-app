package com.example.test.screenui

import android.graphics.Color as GColor
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.RangeSlider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.test.model.ExerciseLog
import com.example.test.model.WorkoutRecord
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(workoutRecords: List<WorkoutRecord>) {
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy.MM.dd") }
    val scope = rememberCoroutineScope()

    val sortedDates = remember(workoutRecords) {
        workoutRecords.mapNotNull { runCatching { LocalDate.parse(it.date, formatter) }.getOrNull() }.distinct().sorted()
    }
    val dateList = remember(sortedDates) {
        sortedDates.mapIndexed { idx, date -> idx to date }.toMap()
    }
    val maxIndex = (sortedDates.size - 1).coerceAtLeast(1)
    var range by remember { mutableStateOf(0f..maxIndex.toFloat()) }
    val startDate by remember { derivedStateOf { dateList[range.start.toInt()] ?: sortedDates.firstOrNull() } }
    val endDate by remember { derivedStateOf { dateList[range.endInclusive.toInt()] ?: sortedDates.lastOrNull() } }

    var summary by remember { mutableStateOf("") }
    var partData by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var dailyData by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }
    var analysisDone by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("🤖 AI 피티쌤 분석", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        if (sortedDates.isEmpty()) {
            Text("기록된 날짜가 없습니다.")
            return@Column
        }

        Text(
            "선택된 기간: ${startDate?.format(formatter)} ~ ${endDate?.format(formatter)}",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 16.sp
        )
        Spacer(Modifier.height(8.dp))

        RangeSlider(
            value = range,
            onValueChange = { range = it },
            valueRange = 0f..maxIndex.toFloat(),
            steps = maxIndex - 1
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (startDate != null && endDate != null) {
                    scope.launch {
                        isLoading = true
                        val logs = filterLogsInRange(workoutRecords, startDate, endDate, formatter)
                        val (s, p) = withContext(Dispatchers.Default) { generateSummaryAndData(logs) }
                        summary = s
                        partData = p
                        dailyData = withContext(Dispatchers.Default) { getDailySetCount(logs) }
                        analysisDone = true
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("분석하기")
        }

        Spacer(Modifier.height(24.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (analysisDone) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("📊 최근 운동 분석 요약", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        summary.split("\n").forEach { line ->
                            Text(line, fontSize = 15.sp, lineHeight = 20.sp)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                if (partData.isNotEmpty()) RadarChartView(partData)
                Spacer(Modifier.height(24.dp))
                if (dailyData.isNotEmpty()) LineChartView(dailyData)
            }
        }
    }
}

fun filterLogsInRange(records: List<WorkoutRecord>, from: LocalDate?, to: LocalDate?, formatter: DateTimeFormatter): List<ExerciseLog> {
    return records
        .filter {
            val date = runCatching { LocalDate.parse(it.date, formatter) }.getOrNull()
            date != null && from != null && to != null && !date.isBefore(from) && !date.isAfter(to)
        }
        .flatMap { it.logs }
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

fun getDailySetCount(logs: List<ExerciseLog>): Map<String, Int> {
    return logs.groupBy { it.date }
        .mapValues { it.value.sumOf { it.sets } }
        .toSortedMap()
}

@Composable
fun RadarChartView(data: Map<String, Int>) {
    val safeLabels = data.keys.map { if (it.isBlank()) "기타" else it }
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(450.dp).padding(bottom = 32.dp),
        factory = { context ->
            RadarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                xAxis.valueFormatter = IndexAxisValueFormatter(safeLabels)
                xAxis.textSize = 14f
                xAxis.labelRotationAngle = -45f
                xAxis.setLabelCount(5, true)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.textColor = GColor.DKGRAY
                yAxis.axisMinimum = 0f
                yAxis.axisMaximum = (data.values.maxOrNull() ?: 0).toFloat() + 2
                yAxis.setDrawLabels(false)
                val dataSet = RadarDataSet(data.values.map { RadarEntry(it.toFloat()) }, "운동 부위").apply {
                    color = GColor.rgb(33, 150, 243)
                    fillColor = GColor.rgb(33, 150, 243)
                    setDrawFilled(true)
                    valueTextSize = 14f
                    lineWidth = 2f
                    setDrawValues(false)
                }
                this.data = RadarData(dataSet)
                animateXY(1500, 1500)
                invalidate()
            }
        }
    )
}

@Composable
fun LineChartView(dailyData: Map<String, Int>) {
    val labels = dailyData.keys.map { it.takeLast(5) }
    val entries = dailyData.entries.mapIndexed { index, entry ->
        Entry(index.toFloat(), entry.value.toFloat())
    }

    AndroidView(
        modifier = Modifier.fillMaxWidth().height(400.dp).padding(bottom = 32.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setExtraOffsets(16f, 16f, 16f, 16f)

                xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                xAxis.granularity = 1f
                xAxis.setLabelCount(5, true)
                xAxis.labelRotationAngle = -45f
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.textSize = 12f

                axisLeft.axisMinimum = 0f
                axisRight.isEnabled = false

                val dataSet = LineDataSet(entries, "총 세트 수").apply {
                    color = GColor.rgb(98, 0, 238)
                    fillColor = GColor.rgb(187, 134, 252)
                    setDrawFilled(true)
                    fillAlpha = 100
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    lineWidth = 1.5f
                    circleRadius = 2f
                    setDrawValues(false)
                }
                data = LineData(dataSet)
                animateXY(1000, 1000)
                invalidate()
            }
        }
    )
}