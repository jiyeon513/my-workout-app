package com.example.test.screenui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
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
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import com.example.test.model.Badge

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
    var selectedBadge by remember { mutableStateOf<Badge?>(null) }
    var thisMonthWorkoutCount by remember { mutableStateOf(0) }

    val unlockedCount = badges.count { it.isUnlocked }
    val totalCount = badges.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "  My Page",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = { onBackClick() },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "뒤로가기",
                    tint = Color(0xFFBDBDBD)
                )
            }
        }

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
            modifier = Modifier.padding(top = 10.dp, bottom = 12.dp)
        )

        Divider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
        )

        // ✅ 캘린더 & 운동 설명
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SimpleMonthlyCalendar(
                    userRecords = userRecords,
                    onMonthWorkoutCountCalculated = { thisMonthWorkoutCount = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = "● 원이 쳐진 날짜는 운동한 날입니다.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "이번 달에는 총 ${thisMonthWorkoutCount}번 운동했어요 💪",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ✅ 배지 + 획득 현황
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "🏅 나의 배지",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                "$unlockedCount / $totalCount",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
        ) {
            items(badges.distinctBy { it.id }) { badge ->
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
                            .clickable { selectedBadge = badge }
                    )
                    Text(
                        badge.name,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("등 세트: $totalBackSets / 하체 세트: $totalLegSets", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
    }

    // ✅ 배지 설명 팝업
    selectedBadge?.let { badge ->
        AlertDialog(
            onDismissRequest = { selectedBadge = null },
            confirmButton = {
                TextButton(onClick = { selectedBadge = null }) {
                    Text("닫기")
                }
            },
            title = {
                Text(text = badge.name, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val iconId = context.resources.getIdentifier(
                        if (badge.isUnlocked) badge.icon else "badge_locked",
                        "drawable",
                        context.packageName
                    )
                    Image(
                        painter = painterResource(id = iconId),
                        contentDescription = badge.name,
                        modifier = Modifier
                            .size(128.dp)
                            .padding(bottom = 12.dp)
                    )
                    Text(
                        text = badge.description,
                        textAlign = TextAlign.Center
                    )
                }
            }
        )
    }
}

@Composable
fun SimpleMonthlyCalendar(
    userRecords: List<WorkoutRecord>,
    onMonthWorkoutCountCalculated: (Int) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    val recordedDates = remember(userRecords) {
        userRecords.map { it.date }.toSet()
    }

    // 현재 달에 해당하는 운동 날짜만 필터링
    val thisMonthWorkoutDates = recordedDates.filter { dateStr ->
        try {
            val date = LocalDate.parse(dateStr, formatter)
            YearMonth.from(date) == currentMonth
        } catch (e: Exception) {
            false
        }
    }.toSet()

    // 운동 횟수 전달
    LaunchedEffect(currentMonth, userRecords) {
        onMonthWorkoutCountCalculated(thisMonthWorkoutDates.size)
    }

    Column {
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
