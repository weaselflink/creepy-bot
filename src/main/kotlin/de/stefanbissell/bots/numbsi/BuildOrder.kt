package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.Abilities
import com.github.ocraft.s2client.protocol.data.UnitType
import com.github.ocraft.s2client.protocol.data.Units
import com.github.ocraft.s2client.protocol.data.Upgrade
import com.github.ocraft.s2client.protocol.data.Upgrades
import com.github.ocraft.s2client.protocol.spatial.Point
import kotlin.random.Random

class BuildOrder(
    val zergBot: ZergBot,
    private val gameMap: GameMap,
    private val bases: Bases
) : BotComponent {

    private val order = listOf(
        DroneUp(14),
        TrainUnit(Units.ZERG_OVERLORD, 2),
        BuildStructure(Units.ZERG_SPAWNING_POOL),
        DroneUp(16),
        BuildStructure(Units.ZERG_EXTRACTOR),
        TrainUnit(Units.ZERG_OVERLORD, 3),
        TrainUnit(Units.ZERG_QUEEN, 1),
        DroneUp(19),
        ResearchUpgrade(Upgrades.ZERGLING_MOVEMENT_SPEED),
        BuildStructure(Units.ZERG_HATCHERY, 2),
        BuildStructure(Units.ZERG_EVOLUTION_CHAMBER),
        DroneUp(25),
        KeepTraining(listOf(Units.ZERG_ZERGLING)),
        KeepSupplied(),
        ResearchUpgrade(Upgrades.ZERG_GROUND_ARMORS_LEVEL1),
        NonWaiting(ResearchUpgrade(Upgrades.ZERG_MELEE_WEAPONS_LEVEL1)),
        NonWaiting(BuildStructure(Units.ZERG_LAIR, 1)),
        Conditional(TrainUnit(Units.ZERG_QUEEN, 2)) {
            it.zergBot.baseBuildings.ready.count() >= 2
        },
        Conditional(TrainUnit(Units.ZERG_QUEEN, 3)) {
            it.zergBot.baseBuildings.ready.count() >= 3
        }
    )

    override fun onStep() {
        order
            .firstOrNull {
                !it.tryExecute(this)
            }
    }

    fun tryBuildStructure(type: UnitType) =
        when (type) {
            Units.ZERG_EXTRACTOR -> {
                bases
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
                bases
                    .currentBases
                    .firstOrNull()
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

    abstract fun tryExecute(buildOrder: BuildOrder): Boolean
}

data class DroneUp(val needed: Int) : BuildOrderStep() {
    override fun tryExecute(buildOrder: BuildOrder): Boolean {
        val count = buildOrder.zergBot.totalCount(Units.ZERG_DRONE)
        if (count < needed) {
            buildOrder.zergBot.trainUnit(Units.ZERG_DRONE)
            return false
        }
        return true
    }
}

data class TrainUnit(
    val type: UnitType,
    val needed: Int
) : BuildOrderStep() {
    override fun tryExecute(buildOrder: BuildOrder): Boolean {
        val count = buildOrder.zergBot.totalCount(type)
        if (count < needed) {
            buildOrder.zergBot.trainUnit(type)
            return false
        }
        return true
    }
}

data class BuildStructure(
    val type: UnitType,
    val needed: Int = 1
) : BuildOrderStep() {
    override fun tryExecute(buildOrder: BuildOrder): Boolean {
        val count = buildOrder.zergBot.totalCount(type)
        if (count < needed) {
            buildOrder.tryBuildStructure(type)
            return false
        }
        return true
    }
}

data class KeepTraining(
    val types: List<UnitType>
) : BuildOrderStep() {
    override fun tryExecute(buildOrder: BuildOrder): Boolean {
        types
            .filter {
                buildOrder.zergBot.canAfford(it)
            }
            .randomOrNull()
            ?.also {
                buildOrder.zergBot.trainUnit(it)
            }
        return true
    }
}

data class ResearchUpgrade(
    val upgrade: Upgrade
) : BuildOrderStep() {
    override fun tryExecute(buildOrder: BuildOrder): Boolean {
        if (buildOrder.zergBot.isCompleted(upgrade)) {
            return true
        }
        if (buildOrder.zergBot.isPending(upgrade)) {
            return true
        }
        buildOrder.zergBot.tryResearchUpgrade(upgrade)
        return false
    }
}

data class KeepSupplied(val minOverlords: Int = 3) : BuildOrderStep() {
    override fun tryExecute(buildOrder: BuildOrder): Boolean {
        if (buildOrder.zergBot.totalCount(Units.ZERG_OVERLORD) >= minOverlords &&
            buildOrder.zergBot.supplyLeft < 4 &&
            buildOrder.zergBot.pendingCount(Units.ZERG_OVERLORD) == 0
        ) {
            buildOrder.zergBot.trainUnit(Units.ZERG_OVERLORD)
        }
        return true
    }
}

data class Conditional(
    val step: BuildOrderStep,
    val condition: (BuildOrder) -> Boolean
) : BuildOrderStep() {
    override fun tryExecute(buildOrder: BuildOrder): Boolean {
        if (!condition(buildOrder)) {
            return false
        }
        return step.tryExecute(buildOrder)
    }
}

data class NonWaiting(
    val step: BuildOrderStep
) : BuildOrderStep() {
    override fun tryExecute(buildOrder: BuildOrder): Boolean {
        step.tryExecute(buildOrder)
        return true
    }
}
