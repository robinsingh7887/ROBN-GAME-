package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.LevelProgressBar
import com.example.ui.components.RobnTopHeader
import com.example.ui.components.formatNumberStatic
import com.example.ui.theme.*
import com.example.viewmodel.RobnUiState

@Composable
fun TapScreen(
    state: RobnUiState,
    onTap: (x: Float, y: Float, haptic: () -> Unit) -> Unit,
    onActivateTurbo: () -> Unit,
    onActivateFullEnergy: () -> Unit,
    onNavigateToBoost: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    val triggerHaptic: () -> Unit = {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(25)
            }
        } catch (_: Exception) {}
    }

    // Scale animation on press
    var isPressed by remember { mutableStateOf(false) }
    var rotationX by remember { mutableFloatStateOf(0f) }
    var rotationY by remember { mutableFloatStateOf(0f) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "coinScale"
    )

    // Infinite breathing glow for coin
    val infiniteTransition = rememberInfiniteTransition(label = "coinAura")
    val auraGlow by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraGlow"
    )

    val energyProgress = (state.currentEnergy.toFloat() / state.maxEnergy.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header Section
        Column(modifier = Modifier.fillMaxWidth()) {
            RobnTopHeader(
                level = state.level,
                profitPerHour = state.profitPerHour,
                balance = state.balance
            )
            LevelProgressBar(
                level = state.level,
                balance = state.balance
            )
        }

        // Central Coin Counter
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🪙",
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatNumberStatic(state.balance),
                    color = TextWhite,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = "RBN COIN",
                color = GoldPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            // Turbo boost active banner
            if (state.boostState.isTurboActive) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(TurboOrange.copy(alpha = 0.2f))
                        .border(1.dp, TurboOrange, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(text = "🔥", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "TURBO 5X ACTIVE (${state.boostState.turboSecondsLeft}s)",
                        color = TurboOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3D Coin Interactive Area with Floating Particles
        Box(
            modifier = Modifier
                .size(280.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Turbo / Normal Aura Glow Ring
            val auraColor = if (state.boostState.isTurboActive) TurboOrange else GoldPrimary
            Box(
                modifier = Modifier
                    .size(260.dp * auraGlow)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                auraColor.copy(alpha = if (state.boostState.isTurboActive) 0.45f else 0.2f),
                                auraColor.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Outer Neon Border Circle
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .border(
                        3.dp,
                        Brush.sweepGradient(
                            listOf(
                                GoldPrimary,
                                CyberCyan,
                                GoldAccent,
                                CyberEmerald,
                                GoldPrimary
                            )
                        ),
                        CircleShape
                    )
            )

            // The Coin Image with 3D Tilt Graphics Layer
            Box(
                modifier = Modifier
                    .size(225.dp)
                    .scale(scale)
                    .graphicsLayer {
                        this.rotationX = rotationX
                        this.rotationY = rotationY
                        cameraDistance = 12f * density
                    }
                    .clip(CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { offset ->
                                isPressed = true
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                rotationY = (offset.x - centerX) / centerX * 15f
                                rotationX = -(offset.y - centerY) / centerY * 15f

                                onTap(offset.x, offset.y, triggerHaptic)

                                tryAwaitRelease()
                                isPressed = false
                                rotationX = 0f
                                rotationY = 0f
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.robn_coin_tap),
                    contentDescription = "Robn Coin Tap",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Floating +N point badges
            state.tapEffects.forEach { effect ->
                FloatingTapNumber(effect.amount)
            }
        }

        // Energy & Quick Boosters Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Energy Counter and Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⚡", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${state.currentEnergy} / ${state.maxEnergy}",
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "+${3 + state.boostState.rechargeSpeedLevel - 1}/s",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Animated Energy Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(DarkSurfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(energyProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    CyberCyan,
                                    EnergyGlow,
                                    GoldPrimary
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Boost Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Turbo Boost Button
                Button(
                    onClick = onActivateTurbo,
                    enabled = state.boostState.turboAvailable > 0 && !state.boostState.isTurboActive,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TurboOrange,
                        disabledContainerColor = DarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(text = "🔥", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Turbo (${state.boostState.turboAvailable}/${state.boostState.turboMax})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.boostState.turboAvailable > 0) Color.White else TextMuted
                    )
                }

                // Full Energy Refill Button
                Button(
                    onClick = onActivateFullEnergy,
                    enabled = state.boostState.fullEnergyAvailable > 0 && state.currentEnergy < state.maxEnergy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EnergyBlue,
                        disabledContainerColor = DarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(text = "⚡", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Refill (${state.boostState.fullEnergyAvailable}/${state.boostState.fullEnergyMax})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.boostState.fullEnergyAvailable > 0) Color.White else TextMuted
                    )
                }

                // Boost Upgrades Center
                OutlinedButton(
                    onClick = onNavigateToBoost,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(GoldPrimary, CyberCyan))),
                    modifier = Modifier
                        .weight(0.9f)
                        .height(44.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Text(text = "🚀 Boost", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FloatingTapNumber(amount: Long) {
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        offsetY.animateTo(
            targetValue = -120f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 650, easing = LinearEasing)
        )
    }

    Box(
        modifier = Modifier
            .offset(y = offsetY.value.dp)
            .graphicsLayer(alpha = alpha.value)
    ) {
        Text(
            text = "+$amount",
            color = GoldAccent,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.titleLarge
        )
    }
}
