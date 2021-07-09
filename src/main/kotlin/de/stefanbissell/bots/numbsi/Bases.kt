package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.Abilities
import com.github.ocraft.s2client.protocol.data.Buffs
import com.github.ocraft.s2client.protocol.data.Units
import com.github.ocraft.s2client.protocol.spatial.Point
import com.github.ocraft.s2client.protocol.unit.Unit as S2Unit

class Bases(
    private val zergBot: ZergBot
) : BotComponent {

    val currentBases: MutableList<Base> = mutableListOf()

    override fun onStep() {
        removeDestroyedBases()
        addNewBases()
        tryInjectLarva()
    }

    private fun addNewBases() {
        zergBot.baseBuildings
            .filter { building ->
                currentBases
                    .none { it.buildingId == building.tag.value }
            }
            .forEach {
                currentBases += Base(
                    zergBot = zergBot,
                    buildingId = it.tag.value,
                    position = it.position
                )
            }
    }

    private fun removeDestroyedBases() {
        currentBases.removeIf { base ->
            zergBot
                .ownUnits
                .none { it.tag.value == base.buildingId }
        }
    }

    private fun tryInjectLarva() {
        zergBot
            .ownUnits
            .ofType(Units.ZERG_QUEEN)
            .idle
            .mapNotNull { queen ->
                zergBot.baseBuildings
                    .firstOrNull { it.position.distance(queen.position) < 9 }
                    ?.let {
                        queen to it
                    }
            }
            .filter { (queen, base) ->
                zergBot.canCast(queen, Abilities.EFFECT_INJECT_LARVA) &&
                    base.buffs.none { it.buffId == Buffs.QUEEN_SPAWN_LARVA_TIMER.buffId }
            }
            .randomOrNull()
            ?.also { (queen, base) ->
                zergBot.actions()
                    .unitCommand(queen, Abilities.EFFECT_INJECT_LARVA, base, false)
            }
    }
}

class Base(
    private val zergBot: ZergBot,
    val buildingId: Long,
    val position: Point
) {

    val isReady
        get() = building?.isReady ?: false

    private val building
        get() = zergBot
            .ownUnits
            .firstOrNull {
                it.tag.value == buildingId
            }

    val mineralFields
        get() = building
            ?.let { b ->
                zergBot
                    .mineralFields
                    .filter {
                        it.position.distance(b.position) < 9f
                    }
            }
            ?: emptyList()

    private val geysers
        get() = building
            ?.let { b ->
                zergBot
                    .vespeneGeysers
                    .filter {
                        it.position.distance(b.position) < 9f
                    }
            }
            ?: emptyList()

    val emptyGeysers
        get() = geysers
            .filter { geyser ->
                zergBot.ownUnits
                    .ofTypes(
                        Units.ZERG_EXTRACTOR,
                        Units.ZERG_EXTRACTOR_RICH
                    )
                    .none {
                        it.position.distance(geyser.position) < 1
                    }
            }

    private val workingExtractors
        get() = building
            ?.let { b ->
                zergBot
                    .ownWorkingVespeneBuildings
                    .filter {
                        it.position.distance(b.position) < 9f
                    }
            }
            ?: emptyList()

    val underSaturatedExtractors
        get() = workingExtractors
            .filter {
                it.assignedHarvesters.orElse(0) < 3
            }

    private val workers
        get() = building
            ?.let { b ->
                zergBot
                    .workers
                    .filter {
                        it.position.distance(b.position) < 9f
                    }
                    .filter {
                        zergBot.isHarvestingMinerals(it) ||
                            zergBot.isHarvestingVespene(it)
                    }
            }
            ?: emptyList()

    val mineralWorkers
        get() = building
            ?.let { b ->
                zergBot
                    .workers
                    .filter {
                        it.position.distance(b.position) < 9f
                    }
                    .filter {
                        zergBot.isHarvestingMinerals(it)
                    }
            }
            ?: emptyList()

    val workerCount: Int
        get() = workers.size

    val optimalWorkerCount: Int
        get() = workingExtractors.size * 3 + mineralFields.size * 2

    val surplusWorker: S2Unit?
        get() = if (workerCount <= optimalWorkerCount) {
            null
        } else {
            workers.randomOrNull()
        }
}
