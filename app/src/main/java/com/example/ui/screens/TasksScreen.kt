package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyRewardDay
import com.example.data.model.TaskItem
import com.example.ui.components.RobnTopHeader
import com.example.ui.components.formatNumberStatic
import com.example.ui.theme.*
import com.example.viewmodel.RobnUiState

@Composable
fun TasksScreen(
    state: RobnUiState,
    onClaimDailyStreak: () -> Unit,
    onPerformTask: (TaskItem) -> Unit,
    onClaimTaskReward: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Top Header
        RobnTopHeader(
            level = state.level,
            profitPerHour = state.profitPerHour,
            balance = state.balance
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 85.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Daily Streak Section
            item {
                DailyStreakSection(
                    streakDays = state.streakRewards,
                    currentDay = state.currentStreakDay,
                    canClaim = state.canClaimDailyStreak,
                    onClaim = onClaimDailyStreak
                )
            }

            // 2. Daily Tasks
            item {
                Text(
                    text = "🎯 DAILY TASKS",
                    color = GoldPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            val dailyTasks = state.tasks.filter {
                it.type == com.example.data.model.TaskType.DAILY_TAP ||
                it.type == com.example.data.model.TaskType.DAILY_MINE ||
                it.type == com.example.data.model.TaskType.DAILY_ENERGY
            }

            items(dailyTasks, key = { it.id }) { task ->
                TaskItemCard(
                    task = task,
                    onPerform = { onPerformTask(task) },
                    onClaim = { onClaimTaskReward(task.id) }
                )
            }

            // 3. Social & Partnership Tasks
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🌐 SOCIAL & COMMUNITY TASKS",
                    color = CyberCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            val socialTasks = state.tasks.filter {
                it.type != com.example.data.model.TaskType.DAILY_TAP &&
                it.type != com.example.data.model.TaskType.DAILY_MINE &&
                it.type != com.example.data.model.TaskType.DAILY_ENERGY
            }

            items(socialTasks, key = { it.id }) { task ->
                TaskItemCard(
                    task = task,
                    onPerform = { onPerformTask(task) },
                    onClaim = { onClaimTaskReward(task.id) }
                )
            }
        }
    }
}

@Composable
fun DailyStreakSection(
    streakDays: List<DailyRewardDay>,
    currentDay: Int,
    canClaim: Boolean,
    onClaim: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, Brush.horizontalGradient(listOf(GoldPrimary, CyberEmerald)), RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📅", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "DAILY REWARD STREAK",
                            color = GoldPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Day $currentDay of 10",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                if (canClaim) {
                    Button(
                        onClick = onClaim,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Claim 🎁",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Claimed Today ✅",
                            color = CyberEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable row of 10 days
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(streakDays, key = { it.day }) { dayItem ->
                    StreakDayCard(dayItem = dayItem)
                }
            }
        }
    }
}

@Composable
fun StreakDayCard(dayItem: DailyRewardDay) {
    val isCurrent = dayItem.isCurrent
    val isClaimed = dayItem.isClaimed

    Box(
        modifier = Modifier
            .width(68.dp)
            .height(84.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCurrent) GoldContainer else if (isClaimed) DarkBg else DarkSurfaceVariant)
            .border(
                1.5.dp,
                if (isCurrent) GoldAccent else if (isClaimed) CyberEmerald.copy(alpha = 0.5f) else DarkCardBorder,
                RoundedCornerShape(12.dp)
            )
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Day ${dayItem.day}",
                color = if (isCurrent) GoldAccent else TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isClaimed) "✅" else "🪙",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "+${formatNumberStatic(dayItem.rewardRbn)}",
                color = if (isCurrent) GoldPrimary else TextWhite,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun TaskItemCard(
    task: TaskItem,
    onPerform: () -> Unit,
    onClaim: () -> Unit
) {
    val progress = (task.currentCount.toFloat() / task.targetCount.toFloat()).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (task.isCompleted && !task.isClaimed) CyberEmerald.copy(alpha = 0.8f) else DarkCardBorder,
                RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = task.iconEmoji, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = task.title,
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = task.description,
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "+${formatNumberStatic(task.rewardRbn)} 🪙",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (task.targetCount > 1) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${task.currentCount}/${task.targetCount})",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (task.isClaimed) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = "Claimed", color = TextMuted, fontSize = 11.sp)
                }
            } else if (task.isCompleted) {
                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(text = "Claim", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onPerform,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(text = "Go ➔", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
