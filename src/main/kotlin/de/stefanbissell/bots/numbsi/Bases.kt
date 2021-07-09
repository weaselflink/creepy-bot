package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.Abilities
import com.github.ocraft.s2client.protocol.data.Buffs
import com.github.ocraft.s2client.protocol.data.Units
import com.github.ocraft.s2client.protocol.unit.Unit as S2Unit

class Bases : BotComponent(1) {

    var currentBases: List<Base> = listOf()

    override fun onStep(zergBot: ZergBot) {
        initBases(zergBot)
        tryInjectLarva(zergBot)
    }

    private fun initBases(zergBot: ZergBot) {
        currentBases = zergBot
            .baseBuildings
            .ready
            .map {
                Base(
                    zergBot = zergBot,
                    building = it
                )
            }
    }

    private fun tryInjectLarva(zergBot: ZergBot) {
        zergBot
            .ownQueens
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
    val building: S2Unit
) {

    val mineralFields by lazy {
        zergBot
            .mineralFields
            .filter {
                it.position.distance(building.position) < 9f
            }
    }

    private val geysers by lazy {
        zergBot
            .vespeneGeysers
            .filter {
                it.position.distance(building.position) < 9f
            }
    }

    val emptyGeysers by lazy {
        geysers
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
    }

    private val workingExtractors by lazy {
        zergBot
            .ownWorkingVespeneBuildings
            .filter {
                it.position.distance(building.position) < 9f
            }
    }

    val underSaturatedExtractors by lazy {
        workingExtractors
            .filter {
                it.assignedHarvesters.orElse(0) < 3
            }
    }

    private val workers by lazy {
        zergBot
            .workers
            .filter {
                it.position.distance(building.position) < 9f
            }
            .filter {
                zergBot.isHarvestingMinerals(it) ||
                        zergBot.isHarvestingVespene(it)
            }
    }

    val mineralWorkers by lazy {
        zergBot
            .workers
            .filter {
                it.position.distance(building.position) < 9f
            }
            .filter {
                zergBot.isHarvestingMinerals(it)
            }
    }

    val workerCount: Int by lazy { workers.size }

    val optimalWorkerCount: Int by lazy {
        workingExtractors.size * 3 + mineralFields.size * 2
    }

    val surplusWorker: S2Unit? by lazy {
        if (workerCount <= optimalWorkerCount) {
            null
        } else {
            workers.randomOrNull()
        }
    }
}
