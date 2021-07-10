package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.Abilities
import com.github.ocraft.s2client.protocol.debug.Color
import com.github.ocraft.s2client.protocol.unit.Unit as S2Unit

class WorkerManager : BotComponent() {

    private val prioritizeGas = true
    private var lastRebalance = 0.0

    override fun onStep(zergBot: ZergBot) {
        sendIdleToWork(zergBot)
        debugWorkerJobs(zergBot)
        val seconds = zergBot.gameTime.exactSeconds
        if (seconds - lastRebalance > 1) {
            lastRebalance = seconds
            rebalanceWorkers(zergBot)
        }
    }

    private fun rebalanceWorkers(zergBot: ZergBot) {
        val basesWithSurplus = zergBot.bases.currentBases
            .filter {
                it.workerCount > it.optimalWorkerCount + 1
            }
        val basesWithNeed = zergBot.bases.currentBases
            .filter {
                it.workerCount < it.optimalWorkerCount
            }

        if (basesWithNeed.isNotEmpty() && basesWithSurplus.isNotEmpty()) {
            val worker = basesWithSurplus
                .random()
                .surplusWorker
            if (worker != null) {
                val extractors = basesWithNeed
                    .flatMap {
                        it.underSaturatedExtractors
                    }
                if (extractors.isNotEmpty()) {
                    extractors
                        .closestTo(worker)
                        ?.also {
                            zergBot.actions()
                                .unitCommand(worker, Abilities.HARVEST_GATHER_DRONE, it, false)
                        }
                } else {
                    basesWithNeed
                        .flatMap {
                            it.mineralFields
                        }
                        .closestTo(worker)
                        ?.also {
                            zergBot.actions()
                                .unitCommand(worker, Abilities.HARVEST_GATHER_DRONE, it, false)
                        }
                }
            }
        } else {
            zergBot.bases.currentBases
                .forEach { base ->
                    val underSaturatedExtractors = base.underSaturatedExtractors
                    val mineralWorkers = base.mineralWorkers
                    if (underSaturatedExtractors.isNotEmpty() && (prioritizeGas || mineralWorkers.size > base.mineralFields.size * 2)) {
                        mineralWorkers
                            .randomOrNull()
                            ?.also { worker ->
                                underSaturatedExtractors
                                    .closestTo(worker)
                                    ?.also {
                                        zergBot.actions()
                                            .unitCommand(worker, Abilities.HARVEST_GATHER_DRONE, it, false)
                                    }

                            }
                    }
                }
        }
    }

    private fun sendIdleToWork(zergBot: ZergBot) {
        zergBot.workers
            .idle
            .forEach {
                it.backToWork(zergBot)
            }
    }

    private fun debugWorkerJobs(zergBot: ZergBot) {
        zergBot.workers
            .forEach {
                when {
                    zergBot.isHarvestingMinerals(it) -> {
                        debugText(zergBot, it, "minerals")
                    }
                    zergBot.isHarvestingVespene(it) -> {
                        debugText(zergBot, it, "vespene")
                    }
                    zergBot.isBuilding(it) -> {
                        debugText(zergBot, it, "building", Color.GREEN)
                    }
                    else -> {
                        debugText(zergBot, it, "unknown", Color.RED)
                    }
                }
            }
    }

    private fun S2Unit.backToWork(zergBot: ZergBot) {
        val closestMinerals = closestMineralsNearBase(zergBot) ?: closestMinerals(zergBot)
        closestMinerals
            ?.also {
                zergBot.actions()
                    .unitCommand(this, Abilities.HARVEST_GATHER_DRONE, it, false)
            }
    }

    private fun S2Unit.closestMinerals(zergBot: ZergBot) =
        zergBot.mineralFields
            .closestTo(this)

    private fun S2Unit.closestMineralsNearBase(zergBot: ZergBot) =
        zergBot.bases.currentBases
            .flatMap {
                it.mineralFields
            }
            .closestTo(this)
}
