package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.Units

class Strategy(
    private val gameMap: GameMap,
    private val buildOrder: BuildOrder
) : BotComponent(11) {

    override fun onStep(zergBot: ZergBot) {
        if (buildOrder.finished) {
            expandWhenReady(zergBot)
            droneUp(zergBot)
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

    private fun expandWhenReady(zergBot: ZergBot) {
        if (zergBot.observation().minerals > 400 &&
            !expansionInProgress(zergBot) &&
            goodSaturation(zergBot)
        ) {
            expand(zergBot)
        }
    }

    private fun goodSaturation(zergBot: ZergBot) =
        zergBot.bases
            .sumOf { it.workersNeeded } < 8

    private fun expansionInProgress(zergBot: ZergBot) =
        zergBot.pendingCount(Units.ZERG_HATCHERY) > 0 ||
                zergBot.baseBuildings.inProgress.count() > 0

    private fun expand(zergBot: ZergBot) {
        gameMap
            .expansions
            .filter { expansion ->
                zergBot.baseBuildings.none { it.position.distance(expansion) < 4 }
            }
            .minByOrNull {
                it.toPoint2d().distance(gameMap.ownStart)
            }
            ?.also {
                zergBot.tryBuildStructure(Units.ZERG_HATCHERY, it)
            }
    }

    private fun droneUp(zergBot: ZergBot) {
        // TODO
    }
}
