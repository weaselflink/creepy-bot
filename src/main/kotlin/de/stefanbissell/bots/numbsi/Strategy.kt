package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.Units

class Strategy(
    private val buildOrder: BuildOrder
) : BotComponent(11) {

    override fun onStep(zergBot: ZergBot) {
        if (buildOrder.finished) {
            trainTroops(zergBot)
            keepSupplied(zergBot)
        }
    }

    private fun trainTroops(zergBot: ZergBot) {
        if (zergBot.canAfford(Units.ZERG_ZERGLING)) {
            zergBot.trainUnit(Units.ZERG_ZERGLING)
        }
    }

    private fun keepSupplied(zergBot: ZergBot) {
        if (needSupply(zergBot) && zergBot.pendingCount(Units.ZERG_OVERLORD) < 1) {
            zergBot.trainUnit(Units.ZERG_OVERLORD)
        }
    }

    private fun needSupply(zergBot: ZergBot): Boolean {
        if (zergBot.supplyCap >= 200) {
            return false
        }
        return zergBot.supplyLeft < (zergBot.supplyCap / 10)
    }
}
