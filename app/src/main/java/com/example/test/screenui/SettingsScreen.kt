package com.example.test.screenui

import android.graphics.Color as GColor
import androidx.compose.foundation.BorderStroke
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
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(workoutRecords: List<WorkoutRecord>) {
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy.MM.dd") }
    val scope = rememberCoroutineScope()

    val sortedDates = remember(workoutRecords) {
        workoutRecords.mapNotNull { runCatching { LocalDate.parse(it.date, formatter) }.getOrNull() }
            .distinct()
            .sorted()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "  나의 운동 분석",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())

                .padding(innerPadding)
                .padding(16.dp)
        ) {

            if (sortedDates.isEmpty()) {
                Text("기록된 날짜가 없습니다.")
                return@Column
            }

            // 날짜 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5FF)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("선택된 기간", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${startDate?.format(formatter)} ~ ${endDate?.format(formatter)}",
                        fontSize = 15.sp,
                        color = Color(0xFF555555)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // RangeSlider
            RangeSlider(
                value = range,
                onValueChange = { range = it },
                valueRange = 0f..maxIndex.toFloat(),
                steps = maxIndex - 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)   // ✅ 48dp 정도면 원형으로 보임
                    .padding(horizontal = 8.dp),
                colors = SliderDefaults.colors(
                    activeTrackColor = Color(0xFF7E57C2),
                    inactiveTrackColor = Color(0xFFD1C4E9),
                    thumbColor = Color(0xFF7E57C2),  // ✅ 진한 색 넣으면 더 명확
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                )
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
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
            ) {
                Text("분석하기", color = Color.White)
            }

            Spacer(Modifier.height(24.dp))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (analysisDone) {
                Column(
                    Modifier
                        .fillMaxSize()
//                        .verticalScroll(rememberScrollState())
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(6.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("📊 최근 운동 분석 요약", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(Modifier.height(12.dp))

                            val lines = summary.split("\n")
                            lines.forEach {
                                Text(it, fontSize = 15.sp, lineHeight = 20.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))


                    if (partData.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = "🕸️ 부위별 세트 비율",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            RadarChartView(partData)

                            // ✅ 설명 박스 추가
                            val weakestPart = partData.minByOrNull { it.value }?.key ?: "없음"
                            if (weakestPart != "없음") {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)), // 밝은 오렌지톤
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Text(
                                        text = "💡 '$weakestPart' 부위가 상대적으로 적게 훈련되었어요.\n균형 잡힌 루틴을 위해 이 부위도 함께 챙겨보세요!",
                                        modifier = Modifier.padding(16.dp),
                                        fontSize = 14.sp,
                                        color = Color(0xFF5D4037),
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "📈 총 세트 수 변화 추이",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    // ✅ LineChart 아래에 들어갈 코드
                    if (dailyData.isNotEmpty()) {
                        LineChartView(dailyData)

                        // 날짜 정렬 및 차이 계산
                        val sortedDates = dailyData.keys.sorted()
                        val firstDate = sortedDates.firstOrNull()
                        val lastDate = sortedDates.lastOrNull()
                        val firstValue = firstDate?.let { dailyData[it] } ?: 0
                        val lastValue = lastDate?.let { dailyData[it] } ?: 0
                        val diff = lastValue - firstValue

                        // 날짜 포맷 및 설명 문구
                        val chartDiffText = when {
                            diff > 0 -> "📈 ${firstDate}보다 ${lastDate}에 ${diff}세트 더 수행했어요!"
                            diff < 0 -> "📉 ${firstDate}보다 ${lastDate}에 ${-diff}세트 줄었어요. 운동 루틴을 다시 조정해보세요."
                            else -> "😐 ${firstDate}와 ${lastDate}의 운동량이 같아요."
                        }

                        Text(
                            text = "📅 X축: 날짜    ⬆️ Y축: 세트 수",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(end = 4.dp, top = 4.dp)
                        )

                        // 💬 박스로 출력
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)), //
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {




                            Text(
                                text = chartDiffText,
                                modifier = Modifier.padding(16.dp),
                                fontSize = 14.sp,
                                color = Color(0xFF5D4037),
                                lineHeight = 20.sp
                            )
                        }


                    }


                }
            }
        }
    }
}

fun filterLogsInRange(
    records: List<WorkoutRecord>,
    from: LocalDate?,
    to: LocalDate?,
    formatter: DateTimeFormatter
): List<ExerciseLog> {
    return records.filter {
        val date = runCatching { LocalDate.parse(it.date, formatter) }.getOrNull()
        date != null && from != null && to != null && !date.isBefore(from) && !date.isAfter(to)
    }.flatMap { it.logs }
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
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp)
            .padding(bottom = 32.dp),
        factory = { context ->
            RadarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                xAxis.valueFormatter = IndexAxisValueFormatter(safeLabels)
                xAxis.textSize = 14f
                xAxis.labelRotationAngle = 0f
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
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(bottom = 32.dp),
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

                axisLeft.axisMinimum = (dailyData.values.minOrNull()?.toFloat()?.minus(2f)) ?: 0f



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