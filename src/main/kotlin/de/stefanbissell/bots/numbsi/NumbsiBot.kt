package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.bot.S2Agent
import com.github.ocraft.s2client.protocol.data.Upgrade
import org.kodein.di.*

class NumbsiBot(
    private val showDebug: Boolean = false
) : S2Agent() {

    private val di = DI {
        bind { singleton { GameMap() } }
        bind { singleton { QueenController() } }
        bind { singleton { UpgradeTacker() } }
        bind { singleton { FriendlyChat() } }
        bind { singleton { WorkerManager() } }
        bind { singleton { BuildOrder(instance(), instance()) } }
        bind { singleton { Strategy(instance(), instance(), instance()) } }
        bind { singleton { Attacker(instance()) } }
    }
    private val components by di.allInstances<BotComponent>()

    override fun onGameStart() {
        super.onGameStart()
        val zergBot = ZergBot(this)
        components
            .sortedBy { it.priority }
            .forEach {
                it.onGameStart(zergBot)
            }
    }

    override fun onStep() {
        super.onStep()
        val zergBot = ZergBot(this)
        components
            .sortedBy { it.priority }
            .forEach {
                it.onStep(zergBot)
            }

        if (showDebug) {
            debug().sendDebug()
        }
    }

    override fun onUpgradeCompleted(upgrade: Upgrade) {
        super.onUpgradeCompleted(upgrade)
        val zergBot = ZergBot(this)
        components
            .sortedBy { it.priority }
            .forEach {
                it.onUpgradeCompleted(zergBot, upgrade)
            }
    }
}

abstract class BotComponent(
    val priority: Int = Int.MAX_VALUE
) {

    open fun onGameStart(zergBot: ZergBot) {}

    open fun onGameEnd(zergBot: ZergBot) {}

    open fun onStep(zergBot: ZergBot) {}

    open fun onUpgradeCompleted(zergBot: ZergBot, upgrade: Upgrade) {}
}
