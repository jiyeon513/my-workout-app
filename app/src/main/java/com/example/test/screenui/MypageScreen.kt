package com.example.test.screenui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.*
import com.example.test.model.WorkoutRecord
import com.example.test.MainActivity
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MypageScreen(
    allRecords: List<WorkoutRecord>,
    currentUserId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val userRecords = allRecords.filter { it.userId == currentUserId }

    val totalBackSets = userRecords.flatMap { it.logs }.filter { it.part == "등" }.sumOf { it.sets }
    val totalLegSets = userRecords.flatMap { it.logs }.filter { it.part == "하체" }.sumOf { it.sets }

    val badges = remember { (context as MainActivity).calculateBadges(allRecords, currentUserId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 상단 제목
        Text(
            text = "  My Page",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // "00님의 운동 캘린더"
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFF6A1B9A),
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize
                    )
                ) {
                    append("  $currentUserId")
                }
                append(" 님의 운동 캘린더")
            },
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
        )

        Divider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
        )

        // 📆 달력
        SimpleMonthlyCalendar(userRecords)

        Spacer(modifier = Modifier.height(32.dp))

        // 🏅 배지
        Text("🏅 나의 배지", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            badges.forEach { badge ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val iconId = context.resources.getIdentifier(
                        if (badge.isUnlocked) badge.icon else "badge_locked",
                        "drawable",
                        context.packageName
                    )
                    Image(
                        painter = painterResource(id = iconId),
                        contentDescription = badge.name,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .padding(4.dp)
                    )
                    Text(badge.name, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔍 세트 수 디버깅 표시
        Text("등 세트: $totalBackSets / 하체 세트: $totalLegSets", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun SimpleMonthlyCalendar(userRecords: List<WorkoutRecord>) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    val recordedDates = remember(userRecords) {
        userRecords.map { it.date }.toSet()
    }

    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

    Column {
        // 월 변경
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Text("◀")
            }
            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.KOREAN)} ${currentMonth.year}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Text("▶")
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 요일
        val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            for (day in daysOfWeek) {
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 날짜 출력
        val firstDay = currentMonth.atDay(1)
        val offset = firstDay.dayOfWeek.value % 7
        val daysInMonth = currentMonth.lengthOfMonth()
        var day = 1
        val totalCells = offset + daysInMonth
        val numRows = (totalCells + 6) / 7

        for (row in 0 until numRows) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    if (cellIndex < offset || day > daysInMonth) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val dateStr = currentMonth.atDay(day).format(formatter)
                        val hasRecord = recordedDates.contains(dateStr)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasRecord) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp)
                                ) {}
                            }
                            Text(
                                text = "$day",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (hasRecord)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                        day++
                    }
                }
            }
        }
    }
}
