package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.Units
import com.github.ocraft.s2client.protocol.unit.Unit as S2Unit

class Bases(
    private val zergBot: ZergBot
) {

    val currentBases: List<Base> by lazy {
        initBases()
    }

    private fun initBases() =
        zergBot
            .baseBuildings
            .ready
            .map {
                Base(
                    zergBot = zergBot,
                    building = it
                )
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
