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
        BuildStructure(Units.ZERG_SPAWNING_POOL),
        Drone(),
        Overlord(),
        BuildStructure(Units.ZERG_EXTRACTOR),
        Drone(),
        Drone(),
        Drone(),
        Overlord(),
        Queen(),
        Zerglings(),
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
            removeFinished(zergBot)
            order
                .firstOrNull {
                    !it.executeNotDone(zergBot, this)
                }
        }
    }

    fun newPending(zergBot: ZergBot, unitType: UnitType): BotUnit? {
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

    private fun removeFinished(zergBot: ZergBot) {
        knownPending
            .removeIf {
                val unit = zergBot.unit(it)
                unit == null || unit.isIdle
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

private open class BuildUnit(val unitType: UnitType) : BuildOrderStep() {

    override fun tryExecute(zergBot: ZergBot, buildOrder: BuildOrder): Boolean {
        val pending = buildOrder.newPending(zergBot, unitType)
        if (pending != null) {
            buildOrder.knownPending += pending.tag
            done = true
            return true
        }
        zergBot.trainUnit(unitType)
        return false
    }
}

private class Drone : BuildUnit(Units.ZERG_DRONE)

private class Overlord : BuildUnit(Units.ZERG_OVERLORD)

private class Zerglings : BuildUnit(Units.ZERG_ZERGLING)

private class Queen : BuildUnit(Units.ZERG_QUEEN)

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
        done = true
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
