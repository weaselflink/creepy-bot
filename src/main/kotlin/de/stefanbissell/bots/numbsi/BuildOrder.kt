package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.*

class BuildOrder(
    val gameMap: GameMap,
    upgradeTacker: UpgradeTacker
) : BotComponent(10) {

    var finished = false

    private val order = listOf(
        DroneUp(14),
        TrainUnit(Units.ZERG_OVERLORD, 2),
        BuildStructure(Units.ZERG_SPAWNING_POOL),
        DroneUp(16),
        BuildStructure(Units.ZERG_EXTRACTOR),
        TrainUnit(Units.ZERG_OVERLORD, 3),
        TrainUnit(Units.ZERG_QUEEN, 1),
        DroneUp(19),
        ResearchUpgrade(upgradeTacker, Upgrades.ZERGLING_MOVEMENT_SPEED),
        BuildStructure(Units.ZERG_HATCHERY, 2),
        BuildStructure(Units.ZERG_EVOLUTION_CHAMBER),
        DroneUp(24),
        TrainUnit(Units.ZERG_OVERLORD, 4),
        ResearchUpgrade(upgradeTacker, Upgrades.ZERG_GROUND_ARMORS_LEVEL1),
        End
    )

    override fun onStep(zergBot: ZergBot) {
        if (!finished) {
            order
                .firstOrNull {
                    !it.tryExecute(zergBot, this)
                }
        }
    }
}

private sealed class BuildOrderStep {

    abstract fun tryExecute(zergBot: ZergBot, buildOrder: BuildOrder): Boolean
}

private data class DroneUp(val needed: Int) : BuildOrderStep() {
    override fun tryExecute(zergBot: ZergBot, buildOrder: BuildOrder): Boolean {
        val count = zergBot.totalCount(Units.ZERG_DRONE)
        if (count < needed) {
            zergBot.trainUnit(Units.ZERG_DRONE)
            return false
        }
        return true
    }
}

private data class TrainUnit(
    val type: UnitType,
    val needed: Int
) : BuildOrderStep() {
    override fun tryExecute(zergBot: ZergBot, buildOrder: BuildOrder): Boolean {
        val count = zergBot.totalCount(type)
        if (count < needed) {
            zergBot.trainUnit(type)
            return false
        }
        return true
    }
}

private data class BuildStructure(
    val type: UnitType,
    val needed: Int = 1
) : BuildOrderStep() {
    override fun tryExecute(zergBot: ZergBot, buildOrder: BuildOrder): Boolean {
        val count = zergBot.totalCount(type)
        if (count < needed) {
            zergBot.tryBuildStructure(buildOrder.gameMap, type)
            return false
        }
        return true
    }
}

private data class ResearchUpgrade(
    val upgradeTacker: UpgradeTacker,
    val upgrade: Upgrade
) : BuildOrderStep() {
    override fun tryExecute(zergBot: ZergBot, buildOrder: BuildOrder): Boolean {
        if (upgradeTacker.isCompletedOrPending(zergBot, upgrade)) {
            return true
        }
        zergBot.tryResearchUpgrade(upgrade)
        return false
    }
}

private object End : BuildOrderStep() {
    override fun tryExecute(zergBot: ZergBot, buildOrder: BuildOrder): Boolean {
        buildOrder.finished = true
        return true
    }
}
