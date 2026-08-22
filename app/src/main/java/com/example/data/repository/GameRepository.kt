package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.CardUpgradeEntity
import com.example.data.local.TaskProgressEntity
import com.example.data.local.UserGameStateEntity
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val dao = database.gameDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    // Base card definitions
    val defaultCards = listOf(
        // MARKETS
        UpgradeCard("m_ton_pool", "RBN / TON Pool", CardCategory.MARKETS, 400L, 250L, 0, "Provide liquidity to earn RBN swap fees.", "💧"),
        UpgradeCard("m_dex_list", "DEX Listing", CardCategory.MARKETS, 1_200L, 600L, 0, "List RBN on DeDust and STON.fi.", "📈"),
        UpgradeCard("m_ai_arbitrage", "AI Arbitrage Bot", CardCategory.MARKETS, 5_000L, 1_800L, 0, "High-frequency cross-exchange arbitrage.", "🤖"),
        UpgradeCard("m_whale_staking", "Whale Staking Vault", CardCategory.MARKETS, 20_000L, 5_500L, 0, "Institutional high-yield locked vault.", "🐋"),
        UpgradeCard("m_cross_chain", "Cross-Chain Bridge", CardCategory.MARKETS, 80_000L, 18_000L, 0, "Bridge tokens to Solana & Ethereum.", "🌉"),
        UpgradeCard("m_derivatives", "Futures & Perpetuals", CardCategory.MARKETS, 300_000L, 65_000L, 0, "Launch 50x leverage trading markets.", "⚡"),

        // TECH & AI
        UpgradeCard("t_gpu_cluster", "GPU Mining Rig", CardCategory.TECH, 800L, 450L, 0, "Hydro-cooled RTX 4090 mining setup.", "🖥️"),
        UpgradeCard("t_quantum_node", "Quantum Node", CardCategory.TECH, 3_000L, 1_200L, 0, "Zero-latency blockchain validator.", "⚛️"),
        UpgradeCard("t_sat_uplink", "Satellite Uplink", CardCategory.TECH, 15_000L, 4_200L, 0, "Decentralized orbital relay network.", "🛰️"),
        UpgradeCard("t_l2_zk", "ZK-Rollup L2", CardCategory.TECH, 50_000L, 12_500L, 0, "Ultra-fast zero-knowledge scaling.", "🛡️"),
        UpgradeCard("t_neural_ai", "Neural Oracle AI", CardCategory.TECH, 200_000L, 45_000L, 0, "Predictive on-chain intelligence.", "🧠"),
        UpgradeCard("t_supercomputer", "Robn Supercluster", CardCategory.TECH, 1_000_000L, 220_000L, 0, "Autonomous AI compute data center.", "🚀"),

        // PR & LEGAL
        UpgradeCard("p_tg_community", "Telegram Channel", CardCategory.PR_LEGAL, 500L, 300L, 0, "Engage 1,000,000+ active tap players.", "✈️"),
        UpgradeCard("p_influencer", "Crypto Influencer Promo", CardCategory.PR_LEGAL, 2_500L, 950L, 0, "Top viral crypto TikTok & YouTube campaigns.", "📢"),
        UpgradeCard("p_dao_shield", "DAO Legal Shield", CardCategory.PR_LEGAL, 10_000L, 3_000L, 0, "Compliance licenses across 180 countries.", "⚖️"),
        UpgradeCard("p_dubai_summit", "Dubai Crypto Summit", CardCategory.PR_LEGAL, 60_000L, 15_000L, 0, "Host VIP yacht afterparty & keynote.", "🏙️"),
        UpgradeCard("p_celeb_endorse", "Celebrity Ambassador", CardCategory.PR_LEGAL, 250_000L, 55_000L, 0, "Global sports star wears RBN merch.", "⭐"),
        UpgradeCard("p_nasdaq_billboard", "Times Square Billboard", CardCategory.PR_LEGAL, 1_500_000L, 350_000L, 0, "Massive 3D glowing animated RBN billboard.", "🗽"),

        // SPECIALS
        UpgradeCard("s_satoshi_box", "Satoshi Mystery Box", CardCategory.SPECIALS, 10_000L, 4_000L, 0, "Ancient cypherpunk artifact with high yield.", "🎁"),
        UpgradeCard("s_golden_pass", "Golden Robn NFT Pass", CardCategory.SPECIALS, 100_000L, 30_000L, 0, "Guaranteed multiplier for upcoming Airdrop.", "🎫"),
        UpgradeCard("s_cyber_robin", "Cybernetic Robin AI", CardCategory.SPECIALS, 500_000L, 120_000L, 0, "Autonomous AI pet generating infinite passive RBN tokens.", "🦅"),
        UpgradeCard("s_genesis_block", "Genesis Inscription", CardCategory.SPECIALS, 5_000_000L, 1_200_000L, 0, "Permanently inscribed block in RBN history.", "👑")
    )

    val defaultTasks = listOf(
        TaskItem("t_tap_100", "Tap Master", "Tap the RBN coin 200 times", 5_000L, TaskType.DAILY_TAP, 200, 0, iconEmoji = "👆"),
        TaskItem("t_buy_card", "Investor", "Purchase or upgrade 3 mining cards", 12_000L, TaskType.DAILY_MINE, 3, 0, iconEmoji = "⛏️"),
        TaskItem("t_use_energy", "Full Throttle", "Reach 500 Energy consumption", 8_000L, TaskType.DAILY_ENERGY, 500, 0, iconEmoji = "⚡"),
        TaskItem("t_invite", "Spread the Word", "Invite a new friend to Robn Game", 30_000L, TaskType.INVITE_FRIEND, 1, 0, iconEmoji = "🤝"),
        TaskItem("t_follow_x", "Follow on X", "Follow @RobnGame on X / Twitter", 50_000L, TaskType.SOCIAL_X, 1, 0, iconEmoji = "🐦"),
        TaskItem("t_join_tg", "Join Telegram", "Join Official Robn Telegram Announcement Channel", 50_000L, TaskType.SOCIAL_TG, 1, 0, iconEmoji = "✈️"),
        TaskItem("t_sub_yt", "Subscribe YouTube", "Subscribe to Robn Official Channel", 75_000L, TaskType.SOCIAL_YT, 1, 0, iconEmoji = "📺"),
        TaskItem("t_wallet", "Connect Wallet", "Connect your TON / Web3 Wallet for Airdrop", 100_000L, TaskType.CONNECT_WALLET, 1, 0, iconEmoji = "👛")
    )

    val defaultStreakRewards = listOf(
        DailyRewardDay(1, 1_000L, isClaimed = false, isCurrent = true),
        DailyRewardDay(2, 3_000L, isClaimed = false, isCurrent = false),
        DailyRewardDay(3, 10_000L, isClaimed = false, isCurrent = false),
        DailyRewardDay(4, 30_000L, isClaimed = false, isCurrent = false),
        DailyRewardDay(5, 75_000L, isClaimed = false, isCurrent = false),
        DailyRewardDay(6, 150_000L, isClaimed = false, isCurrent = false),
        DailyRewardDay(7, 350_000L, isClaimed = false, isCurrent = false),
        DailyRewardDay(8, 750_000L, isClaimed = false, isCurrent = false),
        DailyRewardDay(9, 1_500_000L, isClaimed = false, isCurrent = false),
        DailyRewardDay(10, 5_000_000L, isClaimed = false, isCurrent = false)
    )

    val defaultFriends = listOf(
        ReferralFriend("f1", "Alex Volkov", "@alex_v", "Diamond Whale", true, 340_000L, 0xFF00E5FF),
        ReferralFriend("f2", "Sarah Connor", "@crypto_sarah", "Platinum Holder", true, 185_000L, 0xFFFF4081),
        ReferralFriend("f3", "David Kim", "@dkim_ton", "Gold Trader", false, 95_000L, 0xFFFFD700),
        ReferralFriend("f4", "Elena Rostova", "@elena_rbn", "Silver Miner", false, 42_000L, 0xFF00E676)
    )

    val todayComboIds = listOf("t_gpu_cluster", "m_dex_list", "p_dubai_summit")

    suspend fun getInitialState(): UserGameStateEntity = withContext(Dispatchers.IO) {
        val existing = dao.getUserState()
        if (existing == null) {
            val fresh = UserGameStateEntity()
            dao.insertOrUpdateUserState(fresh)
            fresh
        } else {
            existing
        }
    }

    suspend fun saveUserState(state: UserGameStateEntity) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateUserState(state)
    }

    suspend fun getCards(): List<UpgradeCard> = withContext(Dispatchers.IO) {
        val saved = dao.getAllCardUpgrades().associateBy { it.cardId }
        defaultCards.map { card ->
            val level = saved[card.id]?.level ?: 0
            card.copy(level = level)
        }
    }

    suspend fun saveCardLevel(cardId: String, level: Int) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateCard(CardUpgradeEntity(cardId, level))
    }

    suspend fun getTasks(): List<TaskItem> = withContext(Dispatchers.IO) {
        val saved = dao.getAllTaskProgress().associateBy { it.taskId }
        defaultTasks.map { task ->
            val p = saved[task.id]
            if (p != null) {
                task.copy(
                    currentCount = p.currentCount,
                    isCompleted = p.isCompleted,
                    isClaimed = p.isClaimed
                )
            } else {
                task
            }
        }
    }

    suspend fun saveTask(task: TaskItem) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateTask(
            TaskProgressEntity(
                taskId = task.id,
                currentCount = task.currentCount,
                isCompleted = task.isCompleted,
                isClaimed = task.isClaimed
            )
        )
    }
}
