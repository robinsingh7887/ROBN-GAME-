package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_game_state")
data class UserGameStateEntity(
    @PrimaryKey val id: Int = 1,
    val balance: Long = 2500L,
    val currentEnergy: Long = 1000L,
    val maxEnergy: Long = 1000L,
    val profitPerHour: Long = 0L,
    val totalTaps: Long = 0L,
    val multiTapLevel: Int = 1,
    val energyLimitLevel: Int = 1,
    val rechargeSpeedLevel: Int = 1,
    val fullEnergyBoostsLeft: Int = 6,
    val turboBoostsLeft: Int = 6,
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val dailyStreakDay: Int = 1,
    val lastDailyClaimTimestamp: Long = 0L,
    val referralCode: String = "RBN-789X",
    val invitedCount: Int = 3,
    val cipherSolved: Boolean = false,
    val comboClaimed: Boolean = false,
    val comboFoundCardsJson: String = "",
    val walletAddress: String = "",
    val walletProvider: String = "",
    val walletConnected: Boolean = false,
    val testnetClaimedTokens: Long = 0L
)

@Entity(tableName = "card_upgrades")
data class CardUpgradeEntity(
    @PrimaryKey val cardId: String,
    val level: Int
)

@Entity(tableName = "task_progress")
data class TaskProgressEntity(
    @PrimaryKey val taskId: String,
    val currentCount: Int,
    val isCompleted: Boolean,
    val isClaimed: Boolean
)
