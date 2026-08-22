package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaderboardEntry
import com.example.data.model.ReferralFriend
import com.example.ui.components.RobnTopHeader
import com.example.ui.components.formatNumberStatic
import com.example.ui.theme.*
import com.example.viewmodel.RobnUiState

@Composable
fun FriendsLeaderboardScreen(
    state: RobnUiState,
    onInviteFriend: () -> Unit,
    onClaimCommission: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Friends, 1 = Leaderboard
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val shareInvite = {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Play Robn Game & Earn RBN!")
            putExtra(
                Intent.EXTRA_TEXT,
                "🪙 Join me on Robn Game (RBN) - The ultimate crypto tap-to-earn! Use my invite code: ${state.referralCode} to get +25,000 RBN bonus: https://t.me/RobnGameBot?start=${state.referralCode}"
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "Invite via"))
        onInviteFriend()
    }

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

        // Tab Selector (Friends vs Leaderboard)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurfaceVariant)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedTab == 0) GoldPrimary else Color.Transparent)
                    .clickable { selectedTab = 0 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👥 Friends (${state.referralFriends.size})",
                    color = if (selectedTab == 0) Color.Black else TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedTab == 1) GoldPrimary else Color.Transparent)
                    .clickable { selectedTab = 1 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🏆 Leaderboard",
                    color = if (selectedTab == 1) Color.Black else TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        if (selectedTab == 0) {
            // FRIENDS TAB
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 85.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Referral Hero Banner
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Brush.horizontalGradient(listOf(GoldPrimary, CyberPink)), RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "🤝 INVITE FRIENDS, EARN BIG!",
                                color = GoldPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Get +25,000 RBN for each friend (+50,000 for Telegram Premium) plus 10% of all their mining profits!",
                                color = TextMuted,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Referral Code Box
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkBg)
                                    .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Your Referral Code", color = TextMuted, fontSize = 10.sp)
                                    Text(text = state.referralCode, color = GoldAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(state.referralCode))
                                    }
                                ) {
                                    Text(text = "Copy 📋", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = shareInvite,
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                            ) {
                                Text(
                                    text = "Invite a Friend (+25,000 RBN) 🚀",
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Friends List Header
                item {
                    Text(
                        text = "YOUR SQUAD (${state.referralFriends.size})",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                items(state.referralFriends, key = { it.id }) { friend ->
                    FriendCard(friend = friend, onClaim = { onClaimCommission(friend.id) })
                }
            }
        } else {
            // LEADERBOARD TAB
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 85.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "🌍 GLOBAL TOP PLAYERS",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                items(state.leaderboard, key = { it.rank }) { entry ->
                    LeaderboardRow(entry = entry)
                }
            }
        }
    }
}

@Composable
fun FriendCard(
    friend: ReferralFriend,
    onClaim: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
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
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(friend.avatarColorHex).copy(alpha = 0.2f))
                        .border(1.dp, Color(friend.avatarColorHex), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = friend.name.first().toString(), color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = friend.name,
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (friend.isPremium) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "⭐", fontSize = 11.sp)
                        }
                    }
                    Text(
                        text = "${friend.username} • ${friend.levelTitle}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            if (friend.coinsGenerated > 0) {
                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "+${formatNumberStatic(friend.coinsGenerated)} 🪙",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "Active ⚡",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun LeaderboardRow(entry: LeaderboardEntry) {
    val isCurrent = entry.isCurrentUser

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isCurrent) GoldContainer else DarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (isCurrent) GoldAccent else if (entry.rank <= 3) GoldPrimary.copy(alpha = 0.5f) else DarkCardBorder,
                RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Rank Number / Badge
                Box(
                    modifier = Modifier.width(32.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    when (entry.rank) {
                        1 -> Text(text = "🥇", fontSize = 18.sp)
                        2 -> Text(text = "🥈", fontSize = 18.sp)
                        3 -> Text(text = "🥉", fontSize = 18.sp)
                        else -> Text(
                            text = "#${entry.rank}",
                            color = if (isCurrent) GoldAccent else TextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.name,
                            color = if (isCurrent) GoldAccent else TextWhite,
                            fontSize = 13.sp,
                            fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = entry.levelTitle,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${formatNumberStatic(entry.balance)} 🪙",
                    color = GoldPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "+${formatNumberStatic(entry.profitPerHour)}/h",
                    color = CyberEmerald,
                    fontSize = 10.sp
                )
            }
        }
    }
}
