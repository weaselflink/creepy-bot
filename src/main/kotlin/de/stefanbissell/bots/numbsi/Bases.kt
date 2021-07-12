package de.stefanbissell.bots.numbsi

import kotlin.math.max
import com.github.ocraft.s2client.protocol.unit.Unit as S2Unit

class Bases(
    private val zergBot: ZergBot
) : AbstractCollection<Base>() {

    val currentBases: List<Base> = initBases()

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

    override fun iterator() = currentBases.iterator()

    override val size: Int
        get() = currentBases.size
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

    val emptyGeysers by lazy {
        zergBot
            .emptyGeysers
            .filter {
                it.position.distance(building.position) < 9f
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

    val workersNeeded: Int by lazy {
        max(0, optimalWorkerCount - workerCount)
    }

    val surplusWorker: S2Unit? by lazy {
        if (workerCount <= optimalWorkerCount) {
            null
        } else {
            workers.randomOrNull()
        }
    }
}
