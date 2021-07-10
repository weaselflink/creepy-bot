package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.*
import com.github.ocraft.s2client.protocol.spatial.Point
import kotlin.random.Random

class BuildOrder(
    private val gameMap: GameMap,
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

    fun tryBuildStructure(zergBot: ZergBot, type: UnitType) =
        when (type) {
            Units.ZERG_EXTRACTOR -> {
                zergBot.bases
                    .currentBases
                    .flatMap {
                        it.emptyGeysers
                    }
                    .randomOrNull()
                    ?.also {
                        zergBot.tryBuildStructure(type, it)
                    }
            }
            Units.ZERG_HATCHERY -> {
                gameMap
                    .expansions
                    .filter { expansion ->
                        zergBot.baseBuildings.none { it.position.distance(expansion) < 4 }
                    }
                    .minByOrNull {
                        it.toPoint2d().distance(gameMap.ownStart)
                    }
                    ?.also {
                        zergBot.tryBuildStructure(type, it)
                    }
            }
            Units.ZERG_LAIR -> {
                zergBot
                    .baseBuildings
                    .ready
                    .filter {
                        zergBot.canCast(it, Abilities.MORPH_LAIR, false)
                    }
                    .closestTo(gameMap.ownStart)
                    ?.also {
                        zergBot.actions()
                            .unitCommand(it, Abilities.MORPH_LAIR, false)
                    }
            }
            else -> {
                zergBot.bases
                    .currentBases
                    .firstOrNull()
                    ?.building
                    ?.position
                    ?.towards(gameMap.center, 6f)
                    ?.add(Point.of(getRandomScalar(), getRandomScalar()).mul(4.0f))
                    ?.let {
                        gameMap.clampToMap(it)
                    }
                    ?.also {
                        zergBot.tryBuildStructure(type, it)
                    }
            }
        }

    private fun getRandomScalar(): Float {
        return Random.nextFloat() * 2 - 1
    }
}

sealed class BuildOrderStep {

    abstract fun tryExecute(zergBot: ZergBot, buildOrder: BuildOrder): Boolean
}

data class DroneUp(val needed: Int) : BuildOrderStep() {
    override fun tryExecute(zergBot: ZergBot, buildOrder: BuildOrder): Boolean {
        val count = zergBot.totalCount(Units.ZERG_DRONE)
        if (count < needed) {
            zergBot.trainUnit(Units.ZERG_DRONE)
            return false
        }
        return true
    }
}

data class TrainUnit(
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

data class BuildStructure(
    val type: UnitType,
    val needed: Int = 1
) : BuildOrderStep() {
    override fun tryExecute(zergBot: ZergBot, buildOrder: BuildOrder): Boolean {
        val count = zergBot.totalCount(type)
        if (count < needed) {
            buildOrder.tryBuildStructure(zergBot, type)
            return false
        }
        return true
    }
}

data class ResearchUpgrade(
    val upgradeTacker: UpgradeTacker,
    val upgrade: Upgrade
) : BuildOrderStep() {
    override fun tryExecute(zergBot: ZergBot, buildOrder: BuildOrder): Boolean {
        if (upgradeTacker.isCompleted(upgrade)) {
            return true
        }
        if (upgradeTacker.isPending(zergBot, upgrade)) {
            return true
        }
        zergBot.tryResearchUpgrade(upgrade)
        return false
    }
}

object End : BuildOrderStep() {
    override fun tryExecute(zergBot: ZergBot, buildOrder: BuildOrder): Boolean {
        buildOrder.finished = true
        return true
    }
}
