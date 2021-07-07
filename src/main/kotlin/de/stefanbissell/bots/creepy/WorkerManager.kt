package de.stefanbissell.bots.creepy

import com.github.ocraft.s2client.protocol.data.Abilities
import com.github.ocraft.s2client.protocol.debug.Color
import com.github.ocraft.s2client.protocol.unit.Unit as S2Unit

class WorkerManager(
    private val zergBot: ZergBot,
    private val bases: Bases
) : BotComponent {

    override fun onStep() {
        zergBot.workers
            .idle
            .forEach {
                it.backToWork()
            }

        val mineralWorkers = zergBot.workers
            .filter {
                zergBot.isHarvestingMinerals(it)
            }
            .onEach {
                debugText(it, "minerals")
            }
        zergBot.workers
            .filter {
                zergBot.isHarvestingVespene(it)
            }
            .onEach {
                debugText(it, "vespene")
            }

        val underSaturatedExtractors = zergBot.ownVespeneBuildings
            .filter {
                it.assignedHarvesters.orElse(0) < 3
            }
        if (mineralWorkers.size > 16 && underSaturatedExtractors.isNotEmpty()) {
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

    private fun debugText(worker: S2Unit, text: String) {
        zergBot.debug()
            .debugTextOut(text, worker.position, Color.WHITE, 12)
    }

    private fun S2Unit.backToWork() {
        val closestMinerals = closestMineralsNearBase() ?: closestMinerals()
        closestMinerals
            ?.also {
                zergBot.actions()
                    .unitCommand(this, Abilities.HARVEST_GATHER_DRONE, it, false)
            }
    }

    private fun S2Unit.closestMinerals() =
        zergBot.mineralFields
            .closestTo(this)

    private fun S2Unit.closestMineralsNearBase() =
        bases.currentBases
            .flatMap {
                it.mineralFields
            }
            .closestTo(this)
}
