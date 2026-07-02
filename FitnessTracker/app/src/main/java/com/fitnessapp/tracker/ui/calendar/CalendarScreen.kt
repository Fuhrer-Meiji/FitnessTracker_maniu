package com.fitnessapp.tracker.ui.calendar

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitnessapp.tracker.data.model.RecordType
import com.fitnessapp.tracker.data.model.WorkoutSet
import com.fitnessapp.tracker.ui.theme.LocalThemeColors
import com.fitnessapp.tracker.util.DateUtils
import com.fitnessapp.tracker.util.UnitConverter
import java.util.*

private data class CalendarCell(
    val dayNumber: Int,
    val timestamp: Long,
    val isCurrentMonth: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val themeColors = LocalThemeColors.current
    val context = LocalContext.current
    
    var showReportDialog by remember { mutableStateOf(false) }
    var showAnnualReport by remember { mutableStateOf(false) }
    
    var reportDialogTitle by remember { mutableStateOf("") }
    var reportDialogContent by remember { mutableStateOf("") }

    val today = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }

    val cells = remember(state.currentYear, state.currentMonth) {
        val list = mutableListOf<CalendarCell>()
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, state.currentYear)
            set(Calendar.MONTH, state.currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val dayOffset = (firstDayOfWeek - Calendar.MONDAY + 7) % 7

        // Previous month days
        val prevMonthCal = Calendar.getInstance().apply {
            timeInMillis = cal.timeInMillis
            add(Calendar.MONTH, -1)
        }
        val daysInPrevMonth = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in dayOffset - 1 downTo 0) {
            val day = daysInPrevMonth - i
            prevMonthCal.set(Calendar.DAY_OF_MONTH, day)
            list.add(CalendarCell(day, prevMonthCal.timeInMillis, false))
        }

        // Current month days
        for (day in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            list.add(CalendarCell(day, cal.timeInMillis, true))
        }

        // Next month days to pad to multiple of 7
        val nextMonthCal = Calendar.getInstance().apply {
            timeInMillis = cal.timeInMillis
            add(Calendar.MONTH, 1)
        }
        val totalCells = list.size
        val totalRows = (totalCells + 6) / 7
        val cellsCount = totalRows * 7
        val nextDays = cellsCount - totalCells
        for (day in 1..nextDays) {
            nextMonthCal.set(Calendar.DAY_OF_MONTH, day)
            list.add(CalendarCell(day, nextMonthCal.timeInMillis, false))
        }
        list
    }

    val borderGradient = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.15f),
            themeColors.primary.copy(alpha = 0.3f),
            Color.White.copy(alpha = 0.05f)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // 1. Calendar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Month & Year
            Column {
                Text(
                    text = "${state.currentMonth + 1}月",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${state.currentYear}年",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Right Side: Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Settings
                HeaderActionButton(
                    icon = Icons.Default.Settings,
                    label = "设置",
                    color = Color(0xFF29B6F6),
                    onClick = {
                        Toast.makeText(context, "请点击底部导航的 [设置] 进行配置", Toast.LENGTH_SHORT).show()
                    }
                )

                // Annual Report (年报) -> Opens custom full-screen dialog showing Lianlian style stats
                HeaderActionButton(
                    icon = Icons.AutoMirrored.Filled.ShowChart,
                    label = "年报",
                    color = Color(0xFFFFB300),
                    onClick = {
                        showAnnualReport = true
                    }
                )

                // Monthly Report (月报)
                HeaderActionButton(
                    icon = Icons.Default.CalendarMonth,
                    label = "月报",
                    color = Color(0xFFAB47BC),
                    onClick = {
                        val currentMonthWorkoutsCount = state.workoutBadges.filterKeys {
                            val c = Calendar.getInstance()
                            c.timeInMillis = it
                            c.get(Calendar.MONTH) == state.currentMonth && c.get(Calendar.YEAR) == state.currentYear
                        }.values.flatten().size

                        reportDialogTitle = "${state.currentMonth + 1}月 月度训练报告"
                        reportDialogContent = "您在本月完成了 ${currentMonthWorkoutsCount} 次训练！\n当前连续训练天数：${state.consecutiveStreak} 天。\n\n本月训练共消耗约 ${(currentMonthWorkoutsCount * 320)} kcal 热量！"
                        showReportDialog = true
                    }
                )
            }
        }

        // 2. Month Selector Navigation Arrows
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.prevMonth() }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "上个月", tint = themeColors.primary)
            }
            Text(
                text = "${state.currentYear}年${state.currentMonth + 1}月",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(onClick = { viewModel.nextMonth() }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "下个月", tint = themeColors.primary)
            }
        }

        // 3. Week headers: Mon - Sun (一, 二, 三, 四, 五, 六, 日)
        val dayLabels = listOf("一", "二", "三", "四", "五", "六", "日")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            dayLabels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // 4. Calendar Cells Grid
        val totalRows = cells.size / 7
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF16161C), RoundedCornerShape(12.dp))
                .border(BorderStroke(0.5.dp, borderGradient), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            for (rowIndex in 0 until totalRows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (colIndex in 0 until 7) {
                        val cellIndex = rowIndex * 7 + colIndex
                        val cell = cells[cellIndex]
                        
                        val isToday = cell.timestamp == today
                        val isSelected = state.selectedDay == cell.timestamp
                        val badges = state.workoutBadges[cell.timestamp] ?: emptyList()
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(95.dp)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when {
                                        isSelected -> themeColors.primary.copy(alpha = 0.2f)
                                        isToday -> themeColors.primary.copy(alpha = 0.08f)
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable {
                                    viewModel.selectDay(cell.timestamp)
                                }
                                .let {
                                    if (isToday || isSelected) {
                                        it.border(
                                            BorderStroke(
                                                1.dp, 
                                                if (isSelected) themeColors.primary else themeColors.primary.copy(alpha = 0.4f)
                                            ),
                                            RoundedCornerShape(6.dp)
                                        )
                                    } else it
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 4.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Cell Header (Day Number only)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = cell.dayNumber.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = if (isToday || badges.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            !cell.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                            isToday -> themeColors.primary
                                            else -> MaterialTheme.colorScheme.onBackground
                                        }
                                    )
                                }

                                Spacer(Modifier.height(4.dp))

                                // stacked badges
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    badges.take(3).forEach { badge ->
                                        CalendarCapsuleBadge(badge = badge)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Streak stat banner
        Spacer(Modifier.height(16.dp))
        Text(
            text = "已完成 ${state.totalCount} 次训练，已连续 ${state.consecutiveStreak} 次训练",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        // 6. Selected Day details list at bottom
        if (state.selectedDay != null) {
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, borderGradient)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${DateUtils.formatDay(state.selectedDay!!)} 训练详情",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        IconButton(onClick = { viewModel.selectDay(state.selectedDay!!) }) {
                            Text("✕", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(Modifier.height(12.dp))

                    if (state.dayWorkouts.isEmpty()) {
                        Text(
                            text = "该日无训练记录",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        state.dayWorkouts.forEach { detail ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = detail.exerciseName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                TextButton(
                                    onClick = { viewModel.deleteWorkout(detail.workoutId) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("删除本次", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                            
                            detail.sets.forEach { set ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 8.dp, top = 3.dp, bottom = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row {
                                        Text(
                                            text = "第 ${set.setNumber} 组",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.width(16.dp))
                                        when (set.recordType) {
                                            RecordType.STRENGTH -> {
                                                val w = UnitConverter.displayWeight(set.weight ?: 0.0, state.currentUnit)
                                                Text(
                                                    text = String.format("%.1f %s", w, state.currentUnit), 
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                                if (set.reps != null) {
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(
                                                        text = "× ${set.reps} 次", 
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onBackground
                                                    )
                                                }
                                            }
                                            RecordType.REPS -> {
                                                Text(
                                                    text = "${set.reps} 次", 
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                            }
                                            RecordType.DURATION -> {
                                                Text(
                                                    text = "${set.durationSeconds} 秒", 
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "✕",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .clickable { viewModel.deleteSetFromWorkout(set) }
                                            .padding(4.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
    }

    // 7. General report dialog (Monthly Report)
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(reportDialogTitle, fontWeight = FontWeight.Bold) },
            text = { Text(reportDialogContent, fontSize = 14.sp) },
            confirmButton = {
                Button(onClick = { showReportDialog = false }) {
                    Text("知道了")
                }
            }
        )
    }

    // 8. Custom Full Screen Annual Report (Lianlian style Monthly summary for the year)
    if (showAnnualReport) {
        Dialog(
            onDismissRequest = { showAnnualReport = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF0C0C12) // Space dark color
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${state.currentYear} 年度训练报告",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Lianlian Fitness 智能成果年报",
                                fontSize = 12.sp,
                                color = themeColors.primaryLight
                            )
                        }
                        IconButton(
                            onClick = { showAnnualReport = false },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Bento Grid Summary Cards (4 stats)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BentoCard(
                            title = "年度训练次数",
                            value = "${state.yearlyWorkoutCount} 次",
                            icon = Icons.Default.CalendarMonth,
                            color = themeColors.primary,
                            modifier = Modifier.weight(1f)
                        )
                        BentoCard(
                            title = "累计消耗热量",
                            value = "${state.yearlyCalories.toInt()} kcal",
                            icon = Icons.Default.LocalFireDepartment,
                            color = Color(0xFFFFA726),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BentoCard(
                            title = "力量累计载重",
                            value = "${state.yearlyVolume.toInt()} kg",
                            icon = Icons.Default.FitnessCenter,
                            color = Color(0xFF26A69A),
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Find the peak month (highest workouts count)
                        val peakMonth = state.yearlyAchievements.maxByOrNull { it.workoutCount }
                        val peakValue = if (peakMonth != null && peakMonth.workoutCount > 0) {
                            "${peakMonth.month}月 (${peakMonth.workoutCount}次)"
                        } else {
                            "暂无数据"
                        }
                        BentoCard(
                            title = "最常锻炼月份",
                            value = peakValue,
                            icon = Icons.Default.Timeline,
                            color = Color(0xFFAB47BC),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(28.dp))

                    // 12 Months achievements title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(16.dp)
                                .background(themeColors.primary, RoundedCornerShape(2.dp))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "年度各月训练分布",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // 12 Month comparative bars & stats
                    val maxWorkoutsInMonth = state.yearlyAchievements.maxOfOrNull { it.workoutCount } ?: 1
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF161622), RoundedCornerShape(16.dp))
                            .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        state.yearlyAchievements.forEach { achievement ->
                            val progress = if (maxWorkoutsInMonth > 0) {
                                achievement.workoutCount.toFloat() / maxWorkoutsInMonth
                            } else {
                                0f
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Month Label
                                Text(
                                    text = "${achievement.month}月",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.width(32.dp)
                                )

                                Spacer(Modifier.width(8.dp))

                                // Progress bar
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.05f))
                                ) {
                                    if (progress > 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(progress)
                                                .background(
                                                    Brush.horizontalGradient(
                                                        colors = listOf(themeColors.primary, themeColors.primaryLight)
                                                    ),
                                                    CircleShape
                                                )
                                        )
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                // Counts & Volume/kcal stats on the right
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.width(110.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${achievement.workoutCount}次",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (achievement.workoutCount > 0) themeColors.primaryLight else Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier.width(36.dp)
                                    )
                                    
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (achievement.workoutCount > 0) {
                                            Text(
                                                text = "${achievement.calories.toInt()} kcal",
                                                fontSize = 10.sp,
                                                color = Color(0xFFFFB74D),
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "${achievement.volume.toInt()} kg",
                                                fontSize = 9.sp,
                                                color = Color(0xFF80CBC4)
                                            )
                                        } else {
                                            Text(
                                                text = "--",
                                                fontSize = 10.sp,
                                                color = Color.White.copy(alpha = 0.2f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Coach/AI suggestion card at the very bottom
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = themeColors.primary.copy(alpha = 0.08f)),
                        border = BorderStroke(0.5.dp, themeColors.primary.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡",
                                fontSize = 22.sp
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = if (state.yearlyWorkoutCount > 0) {
                                    "非常棒！您在 ${state.currentYear} 年保持了出色的训练习惯，继续坚持练练，突破自我，马牛与您并肩同行！"
                                } else {
                                    "新的一年，新的篇章。开始您在 ${state.currentYear} 年的第一次训练吧，马牛将记录您的每一次流汗与蜕变！"
                                },
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun BentoCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun HeaderActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun CalendarCapsuleBadge(badge: CalendarWorkoutBadge) {
    val themeColors = LocalThemeColors.current
    
    val backgroundColor = if (badge.isCardio) {
        Color(0xFFFF9800).copy(alpha = 0.22f) // Transparent Orange
    } else {
        themeColors.primary.copy(alpha = 0.22f) // Transparent Purple
    }
    
    val textColor = if (badge.isCardio) {
        Color(0xFFFFB74D) // Light Orange text
    } else {
        themeColors.primaryLight // Light Purple text
    }

    val borderStroke = BorderStroke(
        width = 0.5.dp,
        color = if (badge.isCardio) Color(0xFFFFB74D).copy(alpha = 0.4f) else themeColors.primary.copy(alpha = 0.4f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .border(borderStroke, RoundedCornerShape(4.dp))
            .padding(vertical = 2.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = badge.mainValue,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = badge.subValue,
                fontSize = 7.sp,
                color = textColor.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
