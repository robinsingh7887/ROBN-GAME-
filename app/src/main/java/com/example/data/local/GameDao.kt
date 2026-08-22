package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM user_game_state WHERE id = 1")
    fun getUserStateFlow(): Flow<UserGameStateEntity?>

    @Query("SELECT * FROM user_game_state WHERE id = 1")
    suspend fun getUserState(): UserGameStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserState(state: UserGameStateEntity)

    @Query("SELECT * FROM card_upgrades")
    fun getAllCardUpgradesFlow(): Flow<List<CardUpgradeEntity>>

    @Query("SELECT * FROM card_upgrades")
    suspend fun getAllCardUpgrades(): List<CardUpgradeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCard(card: CardUpgradeEntity)

    @Query("SELECT * FROM task_progress")
    fun getAllTaskProgressFlow(): Flow<List<TaskProgressEntity>>

    @Query("SELECT * FROM task_progress")
    suspend fun getAllTaskProgress(): List<TaskProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTask(task: TaskProgressEntity)
}
