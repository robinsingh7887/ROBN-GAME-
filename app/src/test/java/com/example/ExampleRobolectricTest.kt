package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Robn Game", appName)
  }

  @Test
  fun `user level increases according to balance threshold`() {
    val levelBronze = UserLevel.getLevelForBalance(2_500L)
    assertEquals(1, levelBronze.level)
    assertEquals("Bronze Tap", levelBronze.title)

    val levelGold = UserLevel.getLevelForBalance(250_000L)
    assertEquals(4, levelGold.level)
    assertEquals("Gold Trader", levelGold.title)

    val levelLord = UserLevel.getLevelForBalance(150_000_000L)
    assertEquals(10, levelLord.level)
    assertEquals("Lord of RBN", levelLord.title)
  }

  @Test
  fun `upgrade card profit and cost calculations scale properly`() {
    val card = UpgradeCard(
      id = "m_btc",
      name = "BTC Pairs",
      category = CardCategory.MARKETS,
      baseCost = 1_000L,
      baseProfitPerHour = 300L,
      level = 0
    )

    assertEquals(1_000L, card.currentCost())
    assertEquals(0L, card.currentProfitPerHour())
    assertEquals(300L, card.nextLevelProfitIncrease())

    val cardLvl1 = card.copy(level = 1)
    assertTrue(cardLvl1.currentCost() > 1_000L)
    assertEquals(300L, cardLvl1.currentProfitPerHour())
  }

  @Test
  fun `boost state costs scale with level`() {
    val boost = BoostState(multiTapLevel = 1, energyLimitLevel = 1, rechargeSpeedLevel = 1)
    assertEquals(1000L, boost.multiTapCost())
    assertEquals(2000L, boost.energyLimitCost())
    assertEquals(1500L, boost.rechargeSpeedCost())

    val upgradedBoost = BoostState(multiTapLevel = 3)
    assertEquals(4000L, upgradedBoost.multiTapCost())
  }
}
