package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.*
import com.github.ocraft.s2client.protocol.unit.Tag

class BuildOrder(
    val gameMap: GameMap,
    upgradeTacker: UpgradeTacker
) : BotComponent(10) {

    var finished = false
    val knownPending = mutableSetOf<Tag>()

    private val order = listOf(
        Drone(),
        Drone(),
        Overlord(),
        BuildStructure(Units.ZERG_SPAWNING_POOL),
        Drone(),
        Drone(),
        Drone(),
        BuildStructure(Units.ZERG_EXTRACTOR),
        Drone(),
        Overlord(),
        TrainUnit(Units.ZERG_QUEEN, 1),
        Drone(),
        Drone(),
        Drone(),
        ResearchUpgrade(upgradeTacker, Upgrades.ZERGLING_MOVEMENT_SPEED),
        BuildStructure(Units.ZERG_HATCHERY, 2),
        BuildStructure(Units.ZERG_EVOLUTION_CHAMBER),
        Drone(),
        Drone(),
        Drone(),
        Drone(),
        Drone(),
        Drone(),
        Overlord(),
        ResearchUpgrade(upgradeTacker, Upgrades.ZERG_GROUND_ARMORS_LEVEL1),
        End
    )

    override fun onStep(zergBot: ZergBot) {
        if (!finished) {
            order
                .firstOrNull {
                    !it.executeNotDone(zergBot, this)
                }
        }
    }

    fun pendingLarva(zergBot: ZergBot, unitType: UnitType): BotUnit? {
        val ability = zergBot.trainingAbility(unitType)
        return zergBot
            .ownUnits
            .filter {
                it.tag !in knownPending
            }
            .firstOrNull { larva ->
                larva.orders
                    .any { it.ability == ability }
            }
    }
}

private sealed class BuildOrderStep {

    var done: Boolean = false

    fun executeNotDone(zergBot: ZergBot, buildOrder: BuildOrder) =
        if (done) {
            true
        } else {
            tryExecute(zergBot, buildOrder)
        }

    abstract fun tryExecute(zergBot: ZergBot, buildOrder: BuildOrder): Boolean
}

private class Drone : BuildOrderStep() {

    override fun tryExecute(zergBot: ZergBot, buildOrder: BuildOrder): Boolean {
        val pending = buildOrder.pendingLarva(zergBot, Units.ZERG_DRONE)
        if (pending != null) {
            buildOrder.knownPending += pending.tag
            done = true
            return true
        }
        zergBot.trainUnit(Units.ZERG_DRONE)
        return false
    }
}

private class Overlord : BuildOrderStep() {

    override fun tryExecute(zergBot: ZergBot, buildOrder: BuildOrder): Boolean {
        val pending = buildOrder.pendingLarva(zergBot, Units.ZERG_OVERLORD)
        if (pending != null) {
            buildOrder.knownPending += pending.tag
            done = true
            return true
        }
        zergBot.trainUnit(Units.ZERG_OVERLORD)
        return false
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
