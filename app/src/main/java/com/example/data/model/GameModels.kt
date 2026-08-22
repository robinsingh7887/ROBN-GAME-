package com.example.data.model

enum class CardCategory(val title: String) {
    MARKETS("Markets"),
    TECH("Tech & AI"),
    PR_LEGAL("PR & Team"),
    SPECIALS("Specials")
}

data class UserLevel(
    val level: Int,
    val title: String,
    val minBalance: Long,
    val maxBalance: Long,
    val colorHex: Long
) {
    companion object {
        val ALL_LEVELS = listOf(
            UserLevel(1, "Bronze Robin", 0L, 5_000L, 0xFFCD7F32),
            UserLevel(2, "Silver Miner", 5_000L, 25_000L, 0xFFC0C0C0),
            UserLevel(3, "Gold Trader", 25_000L, 100_000L, 0xFFFFD700),
            UserLevel(4, "Platinum Holder", 100_000L, 1_000_000L, 0xFF00E5FF),
            UserLevel(5, "Diamond Whale", 1_000_000L, 5_000_000L, 0xFFB9F2FF),
            UserLevel(6, "Master Validator", 5_000_000L, 20_000_000L, 0xFF7C4DFF),
            UserLevel(7, "Grandmaster Node", 20_000_000L, 50_000_000L, 0xFFFF4081),
            UserLevel(8, "Epic Guildmaster", 50_000_000L, 100_000_000L, 0xFF00E676),
            UserLevel(9, "Legendary Baron", 100_000_000L, 500_000_000L, 0xFFFF9100),
            UserLevel(10, "Lord of RBN", 500_000_000L, Long.MAX_VALUE, 0xFFFFD700)
        )

        fun getLevelForBalance(balance: Long): UserLevel {
            return ALL_LEVELS.lastOrNull { balance >= it.minBalance } ?: ALL_LEVELS.first()
        }
    }
}

data class UpgradeCard(
    val id: String,
    val name: String,
    val category: CardCategory,
    val baseCost: Long,
    val baseProfitPerHour: Long,
    val level: Int = 0,
    val description: String,
    val iconEmoji: String = "⚡",
    val requiredLevel: Int = 1
) {
    fun currentCost(): Long {
        if (level == 0) return baseCost
        var cost = baseCost.toDouble()
        for (i in 1..level) {
            cost *= 1.48
        }
        return cost.toLong().coerceAtLeast(100L)
    }

    fun currentProfitPerHour(): Long {
        if (level == 0) return 0L
        var profit = baseProfitPerHour.toDouble()
        for (i in 1 until level) {
            profit += baseProfitPerHour * 0.95
        }
        return profit.toLong()
    }

    fun nextLevelProfitIncrease(): Long {
        return (baseProfitPerHour * (1.0 + level * 0.1)).toLong().coerceAtLeast(baseProfitPerHour)
    }
}

data class DailyRewardDay(
    val day: Int,
    val rewardRbn: Long,
    val isClaimed: Boolean,
    val isCurrent: Boolean
)

enum class TaskType {
    DAILY_TAP,
    DAILY_MINE,
    DAILY_ENERGY,
    INVITE_FRIEND,
    SOCIAL_X,
    SOCIAL_TG,
    SOCIAL_YT,
    CONNECT_WALLET
}

data class TaskItem(
    val id: String,
    val title: String,
    val description: String,
    val rewardRbn: Long,
    val type: TaskType,
    val targetCount: Int = 1,
    val currentCount: Int = 0,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false,
    val iconEmoji: String = "🎯"
)

data class DailyComboState(
    val comboCardIds: List<String>,
    val foundCardIds: Set<String> = emptySet(),
    val isClaimed: Boolean = false,
    val rewardRbn: Long = 5_000_000L
)

data class DailyCipherState(
    val secretWord: String = "ROBN",
    val morseCode: String = ".-. --- -... -.",
    val solvedLetters: String = "",
    val isSolved: Boolean = false,
    val rewardRbn: Long = 1_000_000L
)

data class ReferralFriend(
    val id: String,
    val name: String,
    val username: String,
    val levelTitle: String,
    val isPremium: Boolean,
    val coinsGenerated: Long,
    val avatarColorHex: Long
)

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val balance: Long,
    val profitPerHour: Long,
    val levelTitle: String,
    val isCurrentUser: Boolean = false,
    val badge: String = "🪙"
)

data class BoostState(
    val fullEnergyAvailable: Int = 6,
    val fullEnergyMax: Int = 6,
    val turboAvailable: Int = 6,
    val turboMax: Int = 6,
    val isTurboActive: Boolean = false,
    val turboSecondsLeft: Int = 0,
    val multiTapLevel: Int = 1,
    val energyLimitLevel: Int = 1,
    val rechargeSpeedLevel: Int = 1
) {
    fun multiTapCost(): Long = (1_000L * Math.pow(2.0, (multiTapLevel - 1).toDouble())).toLong()
    fun energyLimitCost(): Long = (1_500L * Math.pow(2.0, (energyLimitLevel - 1).toDouble())).toLong()
    fun rechargeSpeedCost(): Long = (2_000L * Math.pow(2.0, (rechargeSpeedLevel - 1).toDouble())).toLong()
}

data class WalletState(
    val isConnected: Boolean = false,
    val address: String = "",
    val provider: String = "Tonkeeper",
    val network: String = "TON Mainnet",
    val testnetRbnBalance: Long = 0L,
    val airdropTier: String = "Diamond Tier",
    val estimatedTokens: Long = 0L,
    val isClaiming: Boolean = false,
    val txHash: String = ""
)

data class TapEffect(
    val id: Long,
    val amount: Long,
    val x: Float,
    val y: Float
)
