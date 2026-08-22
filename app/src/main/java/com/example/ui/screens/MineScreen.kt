package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CardCategory
import com.example.data.model.UpgradeCard
import com.example.ui.components.RobnTopHeader
import com.example.ui.components.formatNumberStatic
import com.example.ui.theme.*
import com.example.viewmodel.RobnUiState

@Composable
fun MineScreen(
    state: RobnUiState,
    onBuyCard: (String) -> Unit,
    onClaimCombo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(CardCategory.MARKETS) }
    var cardToUpgradeModal by remember { mutableStateOf<UpgradeCard?>(null) }

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

        // Daily Combo Jackpot Banner
        DailyComboBanner(
            comboState = state.dailyCombo,
            allCards = state.cards,
            onClaimCombo = onClaimCombo,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Category Selector Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedCategory.ordinal,
            containerColor = Color.Transparent,
            contentColor = GoldPrimary,
            edgePadding = 16.dp,
            divider = {},
            indicator = {},
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            CardCategory.values().forEach { category ->
                val isSelected = selectedCategory == category
                Tab(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) GoldPrimary else DarkSurfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) GoldAccent else DarkCardBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.title,
                            color = if (isSelected) Color.Black else TextWhite,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Cards Grid
        val filteredCards = state.cards.filter { it.category == selectedCategory }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredCards, key = { it.id }) { card ->
                UpgradeCardItem(
                    card = card,
                    userBalance = state.balance,
                    onCardClick = { cardToUpgradeModal = card },
                    onQuickUpgrade = { onBuyCard(card.id) }
                )
            }
        }
    }

    // Upgrade Confirmation Dialog
    cardToUpgradeModal?.let { card ->
        val currentCard = state.cards.find { it.id == card.id } ?: card
        CardDetailDialog(
            card = currentCard,
            userBalance = state.balance,
            onDismiss = { cardToUpgradeModal = null },
            onConfirmUpgrade = {
                onBuyCard(currentCard.id)
                cardToUpgradeModal = null
            }
        )
    }
}

@Composable
fun DailyComboBanner(
    comboState: com.example.data.model.DailyComboState,
    allCards: List<UpgradeCard>,
    onClaimCombo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.5.dp, Brush.horizontalGradient(listOf(GoldPrimary, CyberCyan)), RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔥", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DAILY COMBO",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "+5,000,000 🪙",
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3 Card Slots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                comboState.comboCardIds.forEachIndexed { index, cardId ->
                    val isFound = comboState.foundCardIds.contains(cardId)
                    val card = allCards.find { it.id == cardId }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(68.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isFound) DarkSurfaceVariant else DarkBg)
                            .border(
                                1.5.dp,
                                if (isFound) CyberEmerald else DarkCardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isFound && card != null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = card.iconEmoji, fontSize = 20.sp)
                                Text(
                                    text = card.name,
                                    color = TextWhite,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Found ✅",
                                    color = CyberEmerald,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = "❓", fontSize = 20.sp)
                                Text(
                                    text = "Card ${index + 1}",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            if (comboState.foundCardIds.size == 3 && !comboState.isClaimed) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onClaimCombo,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Text(
                        text = "Claim 5,000,000 RBN Jackpot! 🏆",
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (comboState.isClaimed) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "✅ Today's Combo Claimed! Resets in 14h 22m",
                    color = CyberEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun UpgradeCardItem(
    card: UpgradeCard,
    userBalance: Long,
    onCardClick: () -> Unit,
    onQuickUpgrade: () -> Unit
) {
    val cost = card.currentCost()
    val canAfford = userBalance >= cost

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (card.level > 0) GoldPrimary.copy(alpha = 0.4f) else DarkCardBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onCardClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Emoji & Level Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = card.iconEmoji, fontSize = 20.sp)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (card.level > 0) GoldContainer else DarkSurfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (card.level > 0) "lvl ${card.level}" else "lvl 0",
                        color = if (card.level > 0) GoldAccent else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = card.name,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Profit Per Hour increase
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Text(
                    text = "Profit/h: ",
                    color = TextMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = "+${formatNumberStatic(card.nextLevelProfitIncrease())}",
                    color = CyberEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cost Button
            Button(
                onClick = onQuickUpgrade,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    disabledContainerColor = DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
            ) {
                Text(
                    text = "🪙 ${formatNumberStatic(cost)}",
                    color = if (canAfford) Color.Black else TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CardDetailDialog(
    card: UpgradeCard,
    userBalance: Long,
    onDismiss: () -> Unit,
    onConfirmUpgrade: () -> Unit
) {
    val cost = card.currentCost()
    val canAfford = userBalance >= cost

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, GoldPrimary, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                        .border(2.dp, GoldAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = card.iconEmoji, fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = card.name,
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = card.description,
                    color = TextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkBg)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Current Level", color = TextMuted, fontSize = 11.sp)
                        Text(text = "Lvl ${card.level}", color = GoldAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "+Profit / Hour", color = TextMuted, fontSize = 11.sp)
                        Text(text = "+${formatNumberStatic(card.nextLevelProfitIncrease())}", color = CyberEmerald, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onConfirmUpgrade,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        disabledContainerColor = DarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = if (canAfford) "Upgrade for 🪙 ${formatNumberStatic(cost)}" else "Not Enough 🪙 ${formatNumberStatic(cost)}",
                        color = if (canAfford) Color.Black else TextMuted,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
