package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserGameStateEntity
import com.example.data.model.*
import com.example.data.repository.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

data class RobnUiState(
    val balance: Long = 2_500L,
    val currentEnergy: Long = 1000L,
    val maxEnergy: Long = 1000L,
    val profitPerHour: Long = 0L,
    val totalTaps: Long = 0L,
    val level: UserLevel = UserLevel.getLevelForBalance(2_500L),
    val cards: List<UpgradeCard> = emptyList(),
    val tasks: List<TaskItem> = emptyList(),
    val streakRewards: List<DailyRewardDay> = emptyList(),
    val currentStreakDay: Int = 1,
    val canClaimDailyStreak: Boolean = true,
    val nextStreakTimeSeconds: Long = 0L,
    val boostState: BoostState = BoostState(),
    val dailyCombo: DailyComboState = DailyComboState(emptyList()),
    val dailyCipher: DailyCipherState = DailyCipherState(),
    val referralFriends: List<ReferralFriend> = emptyList(),
    val referralCode: String = "RBN-789X",
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val walletState: WalletState = WalletState(),
    val offlineEarnedRbn: Long = 0L,
    val showOfflineDialog: Boolean = false,
    val tapEffects: List<TapEffect> = emptyList(),
    val snackbarMessage: String? = null
)

class RobnViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(application)

    private val _uiState = MutableStateFlow(RobnUiState())
    val uiState: StateFlow<RobnUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null
    private var turboTimerJob: Job? = null
    private var tapEffectCounter = 0L

    init {
        loadSavedGame()
        startPeriodicTicker()
    }

    private fun loadSavedGame() {
        viewModelScope.launch {
            val savedState = repository.getInitialState()
            val cardsList = repository.getCards()
            val tasksList = repository.getTasks()
            val streakList = repository.defaultStreakRewards.map {
                it.copy(
                    isClaimed = it.day < savedState.dailyStreakDay || (it.day == savedState.dailyStreakDay && !canClaimStreakCheck(savedState.lastDailyClaimTimestamp)),
                    isCurrent = it.day == savedState.dailyStreakDay
                )
            }

            // Calculate profit per hour from cards
            val computedProfitPerHour = cardsList.sumOf { it.currentProfitPerHour() }
            val computedMaxEnergy = 1000L + (savedState.energyLimitLevel - 1) * 500L

            // Calculate offline earnings
            val now = System.currentTimeMillis()
            val elapsedSeconds = ((now - savedState.lastActiveTimestamp) / 1000L).coerceAtLeast(0L)
            val maxOfflineSeconds = 3L * 3600L // 3 hours limit
            val effectiveSeconds = elapsedSeconds.coerceAtMost(maxOfflineSeconds)
            val offlineProfit = if (computedProfitPerHour > 0 && effectiveSeconds > 30) {
                ((computedProfitPerHour.toDouble() / 3600.0) * effectiveSeconds).toLong()
            } else 0L

            // Calculate energy regenerated during offline time
            val rechargePerSec = 3L + (savedState.rechargeSpeedLevel - 1)
            val energyGained = effectiveSeconds * rechargePerSec
            val newEnergy = (savedState.currentEnergy + energyGained).coerceAtMost(computedMaxEnergy)

            val newBalance = savedState.balance + offlineProfit
            val newLevel = UserLevel.getLevelForBalance(newBalance)

            // Daily combo state
            val comboFound = savedState.comboFoundCardsJson.split(",").filter { it.isNotBlank() }.toSet()

            val boostState = BoostState(
                fullEnergyAvailable = savedState.fullEnergyBoostsLeft,
                turboAvailable = savedState.turboBoostsLeft,
                multiTapLevel = savedState.multiTapLevel,
                energyLimitLevel = savedState.energyLimitLevel,
                rechargeSpeedLevel = savedState.rechargeSpeedLevel
            )

            val wallet = WalletState(
                isConnected = savedState.walletConnected,
                address = savedState.walletAddress.ifBlank { "UQDx...9fA2" },
                provider = savedState.walletProvider.ifBlank { "Tonkeeper" },
                testnetRbnBalance = savedState.testnetClaimedTokens,
                estimatedTokens = (newBalance * 0.15 + computedProfitPerHour * 1.2).toLong().coerceAtLeast(10_000L)
            )

            val leaderboardList = generateLeaderboard(savedState.balance, computedProfitPerHour, newLevel.title)

            _uiState.update {
                it.copy(
                    balance = newBalance,
                    currentEnergy = newEnergy,
                    maxEnergy = computedMaxEnergy,
                    profitPerHour = computedProfitPerHour,
                    totalTaps = savedState.totalTaps,
                    level = newLevel,
                    cards = cardsList,
                    tasks = tasksList,
                    streakRewards = streakList,
                    currentStreakDay = savedState.dailyStreakDay,
                    canClaimDailyStreak = canClaimStreakCheck(savedState.lastDailyClaimTimestamp),
                    boostState = boostState,
                    dailyCombo = DailyComboState(
                        comboCardIds = repository.todayComboIds,
                        foundCardIds = comboFound,
                        isClaimed = savedState.comboClaimed
                    ),
                    dailyCipher = DailyCipherState(
                        isSolved = savedState.cipherSolved,
                        solvedLetters = if (savedState.cipherSolved) "ROBN" else ""
                    ),
                    referralFriends = repository.defaultFriends,
                    referralCode = savedState.referralCode,
                    leaderboard = leaderboardList,
                    walletState = wallet,
                    offlineEarnedRbn = offlineProfit,
                    showOfflineDialog = offlineProfit > 0
                )
            }
        }
    }

    private fun canClaimStreakCheck(lastClaimTimestamp: Long): Boolean {
        if (lastClaimTimestamp == 0L) return true
        val oneDayMillis = 24 * 60 * 60 * 1000L
        return (System.currentTimeMillis() - lastClaimTimestamp) >= oneDayMillis
    }

    private fun startPeriodicTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(1000L)
                tickOneSecond()
            }
        }
    }

    private fun tickOneSecond() {
        _uiState.update { state ->
            // Add profit per hour slice (profit/3600 per second)
            val profitPerSec = if (state.profitPerHour > 0) state.profitPerHour / 3600.0 else 0.0
            // Fractional accumulator or rounding
            val newBalance = (state.balance + (if (profitPerSec >= 1.0) profitPerSec.toLong() else if (Random.nextDouble() < profitPerSec) 1L else 0L))

            // Energy recharge
            val rechargePerSec = 3L + (state.boostState.rechargeSpeedLevel - 1)
            val updatedEnergy = (state.currentEnergy + rechargePerSec).coerceAtMost(state.maxEnergy)

            // Update level if needed
            val updatedLevel = if (newBalance >= state.level.maxBalance) {
                UserLevel.getLevelForBalance(newBalance)
            } else state.level

            state.copy(
                balance = newBalance,
                currentEnergy = updatedEnergy,
                level = updatedLevel
            )
        }
    }

    fun onTap(x: Float = 0f, y: Float = 0f, onHaptic: () -> Unit = {}) {
        val state = _uiState.value
        val boost = state.boostState
        val tapMultiplier = if (boost.isTurboActive) 5 else 1
        val tapCost = boost.multiTapLevel
        val coinsEarned = (boost.multiTapLevel * tapMultiplier).toLong()

        if (state.currentEnergy < tapCost) {
            _uiState.update { it.copy(snackbarMessage = "⚡ Energy depleted! Wait for recharge or use Full Energy Boost.") }
            return
        }

        onHaptic()

        val newEnergy = state.currentEnergy - tapCost
        val newBalance = state.balance + coinsEarned
        val newTotalTaps = state.totalTaps + 1
        val newLevel = UserLevel.getLevelForBalance(newBalance)

        // Spawn floating tap effect
        val effectId = ++tapEffectCounter
        val newEffect = TapEffect(id = effectId, amount = coinsEarned, x = x, y = y)

        _uiState.update { current ->
            val updatedEffects = (current.tapEffects + newEffect).takeLast(10)
            current.copy(
                balance = newBalance,
                currentEnergy = newEnergy,
                totalTaps = newTotalTaps,
                level = newLevel,
                tapEffects = updatedEffects
            )
        }

        // Check task progress for tapping
        updateTaskProgress(TaskType.DAILY_TAP, 1)

        // Persist periodically or on exit
        saveCurrentGameState()

        // Clean up tap effect after 800ms
        viewModelScope.launch {
            delay(800L)
            _uiState.update { cur ->
                cur.copy(tapEffects = cur.tapEffects.filter { it.id != effectId })
            }
        }
    }

    fun buyCard(cardId: String) {
        val state = _uiState.value
        val card = state.cards.find { it.id == cardId } ?: return
        val cost = card.currentCost()

        if (state.balance < cost) {
            _uiState.update { it.copy(snackbarMessage = "❌ Not enough RBN to upgrade ${card.name}!") }
            return
        }

        val newLevel = card.level + 1
        val newCards = state.cards.map {
            if (it.id == cardId) it.copy(level = newLevel) else it
        }
        val newBalance = state.balance - cost
        val newProfitPerHour = newCards.sumOf { it.currentProfitPerHour() }
        val userLevel = UserLevel.getLevelForBalance(newBalance)

        // Check daily combo
        val updatedFoundCards = if (state.dailyCombo.comboCardIds.contains(cardId)) {
            state.dailyCombo.foundCardIds + cardId
        } else {
            state.dailyCombo.foundCardIds
        }

        val newComboState = state.dailyCombo.copy(foundCardIds = updatedFoundCards)

        _uiState.update {
            it.copy(
                balance = newBalance,
                cards = newCards,
                profitPerHour = newProfitPerHour,
                level = userLevel,
                dailyCombo = newComboState,
                snackbarMessage = "🎉 Upgraded ${card.name} to Lvl $newLevel! (+${card.nextLevelProfitIncrease()}/h)"
            )
        }

        viewModelScope.launch {
            repository.saveCardLevel(cardId, newLevel)
            saveCurrentGameState()
        }

        updateTaskProgress(TaskType.DAILY_MINE, 1)
    }

    fun activateTurboBoost() {
        val state = _uiState.value
        if (state.boostState.turboAvailable <= 0) {
            _uiState.update { it.copy(snackbarMessage = "❌ No Turbo Boosts left for today!") }
            return
        }
        if (state.boostState.isTurboActive) {
            _uiState.update { it.copy(snackbarMessage = "⚡ Turbo Boost is already active!") }
            return
        }

        val newTurboLeft = state.boostState.turboAvailable - 1
        _uiState.update {
            it.copy(
                boostState = it.boostState.copy(
                    turboAvailable = newTurboLeft,
                    isTurboActive = true,
                    turboSecondsLeft = 20
                ),
                snackbarMessage = "🔥 TURBO BOOST 5x ACTIVATED FOR 20s!"
            )
        }

        turboTimerJob?.cancel()
        turboTimerJob = viewModelScope.launch {
            for (sec in 20 downTo 1) {
                delay(1000L)
                _uiState.update {
                    it.copy(boostState = it.boostState.copy(turboSecondsLeft = sec - 1))
                }
            }
            _uiState.update {
                it.copy(
                    boostState = it.boostState.copy(isTurboActive = false, turboSecondsLeft = 0),
                    snackbarMessage = "Turbo Boost ended."
                )
            }
        }
        saveCurrentGameState()
    }

    fun activateFullEnergyBoost() {
        val state = _uiState.value
        if (state.boostState.fullEnergyAvailable <= 0) {
            _uiState.update { it.copy(snackbarMessage = "❌ No Full Energy Boosts left today!") }
            return
        }

        val newFullEnergy = state.boostState.fullEnergyAvailable - 1
        _uiState.update {
            it.copy(
                currentEnergy = it.maxEnergy,
                boostState = it.boostState.copy(fullEnergyAvailable = newFullEnergy),
                snackbarMessage = "⚡ ENERGY FULLY RESTORED TO ${it.maxEnergy}!"
            )
        }
        saveCurrentGameState()
    }

    fun upgradeMultiTap() {
        val state = _uiState.value
        val cost = state.boostState.multiTapCost()
        if (state.balance < cost) {
            _uiState.update { it.copy(snackbarMessage = "❌ Need ${formatNumber(cost)} RBN for Multitap upgrade") }
            return
        }

        val newLevel = state.boostState.multiTapLevel + 1
        val newBalance = state.balance - cost
        _uiState.update {
            it.copy(
                balance = newBalance,
                boostState = it.boostState.copy(multiTapLevel = newLevel),
                snackbarMessage = "⚡ Multitap upgraded to Level $newLevel! (+${newLevel} coins/tap)"
            )
        }
        saveCurrentGameState()
    }

    fun upgradeEnergyLimit() {
        val state = _uiState.value
        val cost = state.boostState.energyLimitCost()
        if (state.balance < cost) {
            _uiState.update { it.copy(snackbarMessage = "❌ Need ${formatNumber(cost)} RBN for Energy Limit upgrade") }
            return
        }

        val newLevel = state.boostState.energyLimitLevel + 1
        val newMaxEnergy = 1000L + (newLevel - 1) * 500L
        val newBalance = state.balance - cost
        _uiState.update {
            it.copy(
                balance = newBalance,
                maxEnergy = newMaxEnergy,
                currentEnergy = (it.currentEnergy + 500L).coerceAtMost(newMaxEnergy),
                boostState = it.boostState.copy(energyLimitLevel = newLevel),
                snackbarMessage = "🔋 Max Energy upgraded to $newMaxEnergy!"
            )
        }
        saveCurrentGameState()
    }

    fun upgradeRechargeSpeed() {
        val state = _uiState.value
        val cost = state.boostState.rechargeSpeedCost()
        if (state.balance < cost) {
            _uiState.update { it.copy(snackbarMessage = "❌ Need ${formatNumber(cost)} RBN for Recharge Speed upgrade") }
            return
        }

        val newLevel = state.boostState.rechargeSpeedLevel + 1
        val newBalance = state.balance - cost
        _uiState.update {
            it.copy(
                balance = newBalance,
                boostState = it.boostState.copy(rechargeSpeedLevel = newLevel),
                snackbarMessage = "⚡ Recharge speed upgraded to Level $newLevel! (+${3 + newLevel - 1} energy/sec)"
            )
        }
        saveCurrentGameState()
    }

    fun claimDailyStreak() {
        val state = _uiState.value
        if (!state.canClaimDailyStreak) {
            _uiState.update { it.copy(snackbarMessage = "⏳ Daily reward already claimed today! Check back tomorrow.") }
            return
        }

        val rewardItem = state.streakRewards.find { it.day == state.currentStreakDay } ?: return
        val rewardAmount = rewardItem.rewardRbn
        val newBalance = state.balance + rewardAmount
        val nextDay = if (state.currentStreakDay >= 10) 1 else state.currentStreakDay + 1

        val updatedRewards = state.streakRewards.map {
            if (it.day == state.currentStreakDay) it.copy(isClaimed = true, isCurrent = false)
            else if (it.day == nextDay) it.copy(isCurrent = true)
            else it
        }

        _uiState.update {
            it.copy(
                balance = newBalance,
                streakRewards = updatedRewards,
                currentStreakDay = nextDay,
                canClaimDailyStreak = false,
                snackbarMessage = "🎁 CLAIMED ${formatNumber(rewardAmount)} RBN DAILY STREAK BONUS!"
            )
        }
        saveCurrentGameState()
    }

    fun claimTaskReward(taskId: String) {
        val state = _uiState.value
        val task = state.tasks.find { it.id == taskId } ?: return
        if (!task.isCompleted || task.isClaimed) return

        val newTasks = state.tasks.map {
            if (it.id == taskId) it.copy(isClaimed = true) else it
        }
        val newBalance = state.balance + task.rewardRbn

        _uiState.update {
            it.copy(
                balance = newBalance,
                tasks = newTasks,
                snackbarMessage = "✅ Claimed ${formatNumber(task.rewardRbn)} RBN from ${task.title}!"
            )
        }

        viewModelScope.launch {
            repository.saveTask(task.copy(isClaimed = true))
            saveCurrentGameState()
        }
    }

    fun performTaskAction(task: TaskItem) {
        if (task.isCompleted) {
            if (!task.isClaimed) claimTaskReward(task.id)
            return
        }

        // For social tasks, mark as completed upon interaction
        val updatedTask = task.copy(currentCount = task.targetCount, isCompleted = true)
        val newTasks = _uiState.value.tasks.map {
            if (it.id == task.id) updatedTask else it
        }
        _uiState.update {
            it.copy(
                tasks = newTasks,
                snackbarMessage = "🎉 Verified ${task.title}! Claim your reward now."
            )
        }

        viewModelScope.launch {
            repository.saveTask(updatedTask)
        }
    }

    private fun updateTaskProgress(type: TaskType, amount: Int) {
        val currentTasks = _uiState.value.tasks
        val updated = currentTasks.map { task ->
            if (task.type == type && !task.isCompleted) {
                val newCount = task.currentCount + amount
                val completed = newCount >= task.targetCount
                val newTask = task.copy(currentCount = newCount, isCompleted = completed)
                viewModelScope.launch { repository.saveTask(newTask) }
                newTask
            } else task
        }
        _uiState.update { it.copy(tasks = updated) }
    }

    fun claimDailyCombo() {
        val state = _uiState.value
        if (state.dailyCombo.foundCardIds.size < 3) {
            _uiState.update { it.copy(snackbarMessage = "🔍 Find all 3 daily combo cards in Mine tab to unlock 5,000,000 RBN!") }
            return
        }
        if (state.dailyCombo.isClaimed) {
            _uiState.update { it.copy(snackbarMessage = "✅ Daily Combo already claimed today!") }
            return
        }

        val reward = state.dailyCombo.rewardRbn
        val newBalance = state.balance + reward
        val newComboState = state.dailyCombo.copy(isClaimed = true)

        _uiState.update {
            it.copy(
                balance = newBalance,
                dailyCombo = newComboState,
                snackbarMessage = "🏆 MEGA JACKPOT! CLAIMED ${formatNumber(reward)} RBN DAILY COMBO!"
            )
        }
        saveCurrentGameState()
    }

    fun submitMorseLetter(morse: String) {
        val state = _uiState.value
        if (state.dailyCipher.isSolved) return

        val morseMap = mapOf(
            ".-." to 'R',
            "---" to 'O',
            "-..." to 'B',
            "-." to 'N'
        )

        val decodedChar = morseMap[morse]
        if (decodedChar == null) {
            _uiState.update { it.copy(snackbarMessage = "❌ Incorrect Morse pattern '$morse'. Try again!") }
            return
        }

        val targetWord = state.dailyCipher.secretWord
        val currentSolved = state.dailyCipher.solvedLetters
        val nextExpectedChar = targetWord.getOrNull(currentSolved.length)

        if (decodedChar == nextExpectedChar) {
            val newSolved = currentSolved + decodedChar
            val isNowSolved = newSolved == targetWord
            val newBalance = if (isNowSolved) state.balance + state.dailyCipher.rewardRbn else state.balance

            _uiState.update {
                it.copy(
                    balance = newBalance,
                    dailyCipher = it.dailyCipher.copy(
                        solvedLetters = newSolved,
                        isSolved = isNowSolved
                    ),
                    snackbarMessage = if (isNowSolved) "🎉 CIPHER CRACKED! You unlocked ${formatNumber(state.dailyCipher.rewardRbn)} RBN!"
                    else "✅ Found letter: $decodedChar ($newSolved/$targetWord)"
                )
            }
            saveCurrentGameState()
        } else {
            _uiState.update { it.copy(snackbarMessage = "❌ Incorrect letter '$decodedChar'. Word is $targetWord. Resetting!") }
        }
    }

    fun connectWallet(providerName: String, customAddress: String? = null) {
        val address = customAddress ?: "UQD" + (1000..9999).random() + "..." + (1000..9999).random() + "A2"
        val state = _uiState.value
        val airdropTier = when {
            state.balance > 10_000_000 -> "Lord of RBN (Top 1%)"
            state.balance > 1_000_000 -> "Diamond Whale (Top 5%)"
            state.balance > 100_000 -> "Gold Staker (Top 15%)"
            else -> "Early Contributor"
        }
        val estimatedAirdrop = (state.balance * 0.18 + state.profitPerHour * 1.5).toLong().coerceAtLeast(25_000L)

        val updatedWallet = WalletState(
            isConnected = true,
            address = address,
            provider = providerName,
            network = if (providerName.contains("MetaMask", ignoreCase = true)) "Ethereum Mainnet" else "TON Network",
            airdropTier = airdropTier,
            estimatedTokens = estimatedAirdrop
        )

        _uiState.update {
            it.copy(
                walletState = updatedWallet,
                snackbarMessage = "🔗 $providerName connected successfully ($address)!"
            )
        }

        updateTaskProgress(TaskType.CONNECT_WALLET, 1)
        saveCurrentGameState()
    }

    fun disconnectWallet() {
        _uiState.update {
            it.copy(
                walletState = WalletState(isConnected = false),
                snackbarMessage = "Wallet disconnected."
            )
        }
        saveCurrentGameState()
    }

    fun claimTestnetAirdrop() {
        val state = _uiState.value
        if (!state.walletState.isConnected) {
            _uiState.update { it.copy(snackbarMessage = "❌ Please connect your wallet first!") }
            return
        }

        val claimTokens = state.walletState.estimatedTokens
        val fakeHash = "0x" + List(16) { "0123456789abcdef".random() }.joinToString("")

        _uiState.update {
            it.copy(
                walletState = it.walletState.copy(
                    testnetRbnBalance = it.walletState.testnetRbnBalance + claimTokens,
                    txHash = fakeHash
                ),
                snackbarMessage = "🚀 Minted ${formatNumber(claimTokens)} RBN testnet tokens to wallet! Tx: $fakeHash"
            )
        }
        saveCurrentGameState()
    }

    fun claimReferralCommission(friendId: String) {
        val state = _uiState.value
        val friend = state.referralFriends.find { it.id == friendId } ?: return
        if (friend.coinsGenerated <= 0) return

        val claimAmount = friend.coinsGenerated
        val newFriends = state.referralFriends.map {
            if (it.id == friendId) it.copy(coinsGenerated = 0) else it
        }
        val newBalance = state.balance + claimAmount

        _uiState.update {
            it.copy(
                balance = newBalance,
                referralFriends = newFriends,
                snackbarMessage = "💰 Claimed ${formatNumber(claimAmount)} RBN commission from ${friend.name}!"
            )
        }
        saveCurrentGameState()
    }

    fun addInvitedFriend() {
        val randomNames = listOf("Crypto Viking", "Robin Hood", "Ton Master", "Solana Dev", "Moon Walker")
        val chosen = randomNames.random()
        val bonus = 25_000L
        val newFriend = ReferralFriend(
            id = "f_${System.currentTimeMillis()}",
            name = chosen,
            username = "@" + chosen.lowercase().replace(" ", "_"),
            levelTitle = "Gold Trader",
            isPremium = true,
            coinsGenerated = 15_000L,
            avatarColorHex = 0xFFFFD700
        )
        val newBalance = _uiState.value.balance + bonus
        val newFriends = _uiState.value.referralFriends + newFriend

        _uiState.update {
            it.copy(
                balance = newBalance,
                referralFriends = newFriends,
                snackbarMessage = "🤝 Friend $chosen joined via your referral! (+${formatNumber(bonus)} RBN bonus)"
            )
        }
        updateTaskProgress(TaskType.INVITE_FRIEND, 1)
        saveCurrentGameState()
    }

    fun dismissOfflineDialog() {
        _uiState.update { it.copy(showOfflineDialog = false, offlineEarnedRbn = 0L) }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun saveCurrentGameState() {
        val state = _uiState.value
        viewModelScope.launch {
            val entity = UserGameStateEntity(
                balance = state.balance,
                currentEnergy = state.currentEnergy,
                maxEnergy = state.maxEnergy,
                profitPerHour = state.profitPerHour,
                totalTaps = state.totalTaps,
                multiTapLevel = state.boostState.multiTapLevel,
                energyLimitLevel = state.boostState.energyLimitLevel,
                rechargeSpeedLevel = state.boostState.rechargeSpeedLevel,
                fullEnergyBoostsLeft = state.boostState.fullEnergyAvailable,
                turboBoostsLeft = state.boostState.turboAvailable,
                lastActiveTimestamp = System.currentTimeMillis(),
                dailyStreakDay = state.currentStreakDay,
                lastDailyClaimTimestamp = if (!state.canClaimDailyStreak) System.currentTimeMillis() else 0L,
                referralCode = state.referralCode,
                cipherSolved = state.dailyCipher.isSolved,
                comboClaimed = state.dailyCombo.isClaimed,
                comboFoundCardsJson = state.dailyCombo.foundCardIds.joinToString(","),
                walletAddress = state.walletState.address,
                walletProvider = state.walletState.provider,
                walletConnected = state.walletState.isConnected,
                testnetClaimedTokens = state.walletState.testnetRbnBalance
            )
            repository.saveUserState(entity)
        }
    }

    private fun generateLeaderboard(userBalance: Long, userProfit: Long, userTitle: String): List<LeaderboardEntry> {
        val topList = mutableListOf(
            LeaderboardEntry(1, "💎 Satoshi_N", 842_500_000L, 4_500_000L, "Lord of RBN", badge = "👑"),
            LeaderboardEntry(2, "⚡ Pavel_Durov", 620_100_000L, 3_800_000L, "Lord of RBN", badge = "🥈"),
            LeaderboardEntry(3, "🚀 Elon_Mars", 450_900_000L, 2_950_000L, "Legendary Baron", badge = "🥉"),
            LeaderboardEntry(4, "🦅 Robin_Founder", 310_000_000L, 2_100_000L, "Legendary Baron"),
            LeaderboardEntry(5, "🔥 TON_Whale_99", 195_400_000L, 1_450_000L, "Epic Guildmaster"),
            LeaderboardEntry(6, "🌐 Vitalik_Eth", 120_800_000L, 950_000L, "Epic Guildmaster"),
            LeaderboardEntry(7, "🤖 AI_Arbitrageur", 85_600_000L, 720_000L, "Grandmaster Node"),
            LeaderboardEntry(8, "🎮 Crypto_Ninja", 45_200_000L, 480_000L, "Master Validator"),
            LeaderboardEntry(9, "⚡ Cyber_Ghost", 18_900_000L, 210_000L, "Diamond Whale"),
            LeaderboardEntry(10, "💰 Gold_Digger", 9_400_000L, 95_000L, "Platinum Holder")
        )

        // Add user rank at #11 or based on score
        topList.add(
            LeaderboardEntry(
                rank = 11,
                name = "You (Robn CEO)",
                balance = userBalance,
                profitPerHour = userProfit,
                levelTitle = userTitle,
                isCurrentUser = true,
                badge = "⭐"
            )
        )
        return topList
    }

    fun formatNumber(number: Long): String {
        return when {
            number >= 1_000_000_000 -> String.format("%.2fB", number / 1_000_000_000.0)
            number >= 1_000_000 -> String.format("%.2fM", number / 1_000_000.0)
            number >= 1_000 -> String.format("%.1fK", number / 1_000.0)
            else -> number.toString()
        }
    }
}
