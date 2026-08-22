package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.OfflineEarningsDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.RobnViewModel

enum class NavigationTab(val title: String, val icon: String) {
    TAP("Tap", "🪙"),
    MINE("Mine", "⛏️"),
    BOOST("Boost", "⚡"),
    TASKS("Tasks", "📋"),
    FRIENDS("Friends", "👥"),
    WALLET("Wallet", "💼")
}

@Composable
fun MainScreen(
    viewModel: RobnViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var currentTab by remember { mutableStateOf(NavigationTab.TAP) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Display snackbar message if any
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg),
        containerColor = DarkBg,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 70.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = DarkSurfaceVariant,
                    contentColor = TextWhite,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = TextWhite,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = DarkCardBorder,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                NavigationTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Text(
                                text = tab.icon,
                                fontSize = if (isSelected) 22.sp else 18.sp
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) GoldPrimary else TextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldPrimary,
                            selectedTextColor = GoldPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = GoldContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "TabCrossfade") { tab ->
                when (tab) {
                    NavigationTab.TAP -> TapScreen(
                        state = state,
                        onTap = { x, y, haptic -> viewModel.onTap(x, y, haptic) },
                        onActivateTurbo = { viewModel.activateTurboBoost() },
                        onActivateFullEnergy = { viewModel.activateFullEnergyBoost() },
                        onNavigateToBoost = { currentTab = NavigationTab.BOOST }
                    )
                    NavigationTab.MINE -> MineScreen(
                        state = state,
                        onBuyCard = { cardId -> viewModel.buyCard(cardId) },
                        onClaimCombo = { viewModel.claimDailyCombo() }
                    )
                    NavigationTab.BOOST -> BoostScreen(
                        state = state,
                        onActivateTurbo = { viewModel.activateTurboBoost() },
                        onActivateFullEnergy = { viewModel.activateFullEnergyBoost() },
                        onUpgradeMultiTap = { viewModel.upgradeMultiTap() },
                        onUpgradeEnergyLimit = { viewModel.upgradeEnergyLimit() },
                        onUpgradeRechargeSpeed = { viewModel.upgradeRechargeSpeed() },
                        onSubmitMorseLetter = { morse -> viewModel.submitMorseLetter(morse) }
                    )
                    NavigationTab.TASKS -> TasksScreen(
                        state = state,
                        onClaimDailyStreak = { viewModel.claimDailyStreak() },
                        onPerformTask = { task -> viewModel.performTaskAction(task) },
                        onClaimTaskReward = { taskId -> viewModel.claimTaskReward(taskId) }
                    )
                    NavigationTab.FRIENDS -> FriendsLeaderboardScreen(
                        state = state,
                        onInviteFriend = { viewModel.addInvitedFriend() },
                        onClaimCommission = { friendId -> viewModel.claimReferralCommission(friendId) }
                    )
                    NavigationTab.WALLET -> WalletScreen(
                        state = state,
                        onConnectWallet = { provider -> viewModel.connectWallet(provider) },
                        onDisconnectWallet = { viewModel.disconnectWallet() },
                        onClaimTestnet = { viewModel.claimTestnetAirdrop() }
                    )
                }
            }
        }
    }

    // Offline earnings celebratory dialog
    if (state.showOfflineDialog && state.offlineEarnedRbn > 0) {
        OfflineEarningsDialog(
            earnedAmount = state.offlineEarnedRbn,
            onClaim = { viewModel.dismissOfflineDialog() }
        )
    }
}
