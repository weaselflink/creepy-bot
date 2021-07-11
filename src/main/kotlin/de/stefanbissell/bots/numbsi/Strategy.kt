package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.UnitType
import com.github.ocraft.s2client.protocol.data.Units
import com.github.ocraft.s2client.protocol.data.Upgrade
import com.github.ocraft.s2client.protocol.data.Upgrades
import kotlin.math.ceil

class Strategy(
    private val gameMap: GameMap,
    private val buildOrder: BuildOrder,
    private val upgradeTacker: UpgradeTacker
) : BotComponent(11) {

    private val priorities = listOf(
        Units.ZERG_LAIR,
        Upgrades.ZERG_MELEE_WEAPONS_LEVEL1,
        Units.ZERG_SPIRE
    )

    override fun onStep(zergBot: ZergBot) {
        if (buildOrder.finished) {
            expandWhenReady(zergBot)
            droneUp(zergBot)
            trainTroops(zergBot)
            keepSupplied(zergBot)
            ensurePriorities(zergBot)
        }
    }

    private fun trainTroops(zergBot: ZergBot) {
        if (zergBot.canAfford(Units.ZERG_ZERGLING)) {
            zergBot.trainUnit(Units.ZERG_ZERGLING)
        }
    }

    private fun keepSupplied(zergBot: ZergBot) {
        if (needSupply(zergBot)) {
            val targetOverlordCount = ceil((zergBot.supplyCap * 0.2) / 8).toInt()
            if (zergBot.pendingCount(Units.ZERG_OVERLORD) < targetOverlordCount) {
                zergBot.trainUnit(Units.ZERG_OVERLORD)
            }
        }
    }

    private fun needSupply(zergBot: ZergBot): Boolean {
        if (zergBot.supplyCap >= 200) {
            return false
        }
        return zergBot.supplyLeft < (zergBot.supplyCap / 5)
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
        if (zergBot.bases.any { it.workersNeeded > 0 }) {
            zergBot.trainUnit(Units.ZERG_DRONE)
        }
    }

    private fun ensurePriorities(zergBot: ZergBot) {
        priorities
            .forEach {
                ensure(zergBot, it)
            }
    }

    private fun ensure(zergBot: ZergBot, what: Any) {
        if (what is UnitType) {
            ensureBuilding(zergBot, what)
        }
        if (what is Upgrade) {
            ensureUpgrade(zergBot, what)
        }
    }

    private fun ensureBuilding(zergBot: ZergBot, unitType: UnitType) {
        if (zergBot.totalCount(unitType) < 1) {
            zergBot.tryBuildStructure(gameMap, unitType)
        }
    }

    private fun ensureUpgrade(zergBot: ZergBot, upgrade: Upgrade) {
        if (!upgradeTacker.isCompletedOrPending(zergBot, upgrade)) {
            zergBot.tryResearchUpgrade(upgrade)
        }
    }
}
