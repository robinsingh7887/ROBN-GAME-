package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.formatNumberStatic
import com.example.ui.theme.*
import com.example.viewmodel.RobnUiState

@Composable
fun BoostScreen(
    state: RobnUiState,
    onActivateTurbo: () -> Unit,
    onActivateFullEnergy: () -> Unit,
    onUpgradeMultiTap: () -> Unit,
    onUpgradeEnergyLimit: () -> Unit,
    onUpgradeRechargeSpeed: () -> Unit,
    onSubmitMorseLetter: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var currentMorseInput by remember { mutableStateOf("") }
    var showMorseGuide by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(scrollState)
            .padding(bottom = 90.dp)
    ) {
        // Balance Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your Share Balance",
                color = TextMuted,
                fontSize = 12.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(text = "🪙", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatNumberStatic(state.balance),
                    color = TextWhite,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // 1. Free Daily Boosters Section
        Text(
            text = "⚡ FREE DAILY BOOSTERS",
            color = GoldPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Full Energy Card
            FreeBoostCard(
                title = "Full Energy",
                available = "${state.boostState.fullEnergyAvailable}/${state.boostState.fullEnergyMax}",
                icon = "🔋",
                accentColor = EnergyBlue,
                isEnabled = state.boostState.fullEnergyAvailable > 0,
                onClick = onActivateFullEnergy,
                modifier = Modifier.weight(1f)
            )

            // Turbo Boost Card
            FreeBoostCard(
                title = "Turbo (5x)",
                available = if (state.boostState.isTurboActive) "${state.boostState.turboSecondsLeft}s left"
                else "${state.boostState.turboAvailable}/${state.boostState.turboMax}",
                icon = "🔥",
                accentColor = TurboOrange,
                isEnabled = state.boostState.turboAvailable > 0 && !state.boostState.isTurboActive,
                onClick = onActivateTurbo,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Permanent Upgrades Section
        Text(
            text = "🚀 BOOSTERS & UPGRADES",
            color = GoldPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            letterSpacing = 1.sp
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Multitap
            UpgradeItemRow(
                title = "Multitap",
                level = state.boostState.multiTapLevel,
                description = "+1 coin per tap per level",
                cost = state.boostState.multiTapCost(),
                userBalance = state.balance,
                icon = "👆",
                onUpgrade = onUpgradeMultiTap
            )

            // Energy Limit
            UpgradeItemRow(
                title = "Energy Limit",
                level = state.boostState.energyLimitLevel,
                description = "+500 max energy capacity",
                cost = state.boostState.energyLimitCost(),
                userBalance = state.balance,
                icon = "⚡",
                onUpgrade = onUpgradeEnergyLimit
            )

            // Recharge Speed
            UpgradeItemRow(
                title = "Recharging Speed",
                level = state.boostState.rechargeSpeedLevel,
                description = "+1 energy recovered per second",
                cost = state.boostState.rechargeSpeedCost(),
                userBalance = state.balance,
                icon = "⏱️",
                onUpgrade = onUpgradeRechargeSpeed
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Daily Secret Morse Cipher Terminal
        DailyCipherCard(
            cipherState = state.dailyCipher,
            currentInput = currentMorseInput,
            showGuide = showMorseGuide,
            onToggleGuide = { showMorseGuide = !showMorseGuide },
            onAddDot = { currentMorseInput += "." },
            onAddDash = { currentMorseInput += "-" },
            onClear = { currentMorseInput = "" },
            onSubmit = {
                if (currentMorseInput.isNotBlank()) {
                    onSubmitMorseLetter(currentMorseInput)
                    currentMorseInput = ""
                }
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun FreeBoostCard(
    title: String,
    available: String,
    icon: String,
    accentColor: Color,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = modifier
            .border(1.dp, if (isEnabled) accentColor.copy(alpha = 0.6f) else DarkCardBorder, RoundedCornerShape(16.dp))
            .clickable(enabled = isEnabled, onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = available,
                color = if (isEnabled) accentColor else TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun UpgradeItemRow(
    title: String,
    level: Int,
    description: String,
    cost: Long,
    userBalance: Long,
    icon: String,
    onUpgrade: () -> Unit
) {
    val canAfford = userBalance >= cost

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
                .padding(12.dp),
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
                        .background(DarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "lvl $level",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = description,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Button(
                onClick = onUpgrade,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    disabledContainerColor = DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.height(38.dp)
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
fun DailyCipherCard(
    cipherState: com.example.data.model.DailyCipherState,
    currentInput: String,
    showGuide: Boolean,
    onToggleGuide: () -> Unit,
    onAddDot: () -> Unit,
    onAddDash: () -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.5.dp, Brush.horizontalGradient(listOf(CyberCyan, CyberEmerald)), RoundedCornerShape(20.dp))
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
                    Text(text = "📻", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DAILY MORSE CIPHER",
                        color = CyberCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "+1,000,000 🪙",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Solved Word Slots (e.g., R O B N)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                cipherState.secretWord.forEachIndexed { index, char ->
                    val isRevealed = index < cipherState.solvedLetters.length
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isRevealed) CyberEmerald.copy(alpha = 0.2f) else DarkBg)
                            .border(
                                1.5.dp,
                                if (isRevealed) CyberEmerald else DarkCardBorder,
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isRevealed) char.toString() else "_",
                            color = if (isRevealed) CyberEmerald else TextMuted,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            if (cipherState.isSolved) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "🎉 CYPHER CRACKED! 1,000,000 RBN claimed!",
                    color = CyberEmerald,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                Spacer(modifier = Modifier.height(14.dp))

                // Current Morse Buffer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBg)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentInput.isEmpty()) "Tap . (Dot) or - (Dash) below" else currentInput,
                        color = if (currentInput.isEmpty()) TextMuted else GoldPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Morse Tap Pad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAddDot,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(text = "• (Dot)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    }

                    Button(
                        onClick = onAddDash,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(text = "— (Dash)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    }

                    Button(
                        onClick = onClear,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(text = "✕", color = TextMuted, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onSubmit,
                    enabled = currentInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        disabledContainerColor = DarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "Submit Letter",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onToggleGuide,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = if (showGuide) "Hide Morse Code Guide ▴" else "Show Morse Code Guide ▾",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                AnimatedVisibility(visible = showGuide) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "💡 Clue: Secret word is 'ROBN'", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "R:  • — •  (.-.)", color = TextWhite, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text(text = "O:  — — —  (---)", color = TextWhite, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text(text = "B:  — • • • (-...)", color = TextWhite, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text(text = "N:  — •     (-.)", color = TextWhite, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}
