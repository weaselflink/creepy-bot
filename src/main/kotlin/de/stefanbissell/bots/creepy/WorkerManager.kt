package de.stefanbissell.bots.creepy

import com.github.ocraft.s2client.protocol.data.Abilities
import com.github.ocraft.s2client.protocol.data.Buffs
import com.github.ocraft.s2client.protocol.debug.Color
import com.github.ocraft.s2client.protocol.unit.Unit
import com.github.ocraft.s2client.protocol.unit.UnitOrder

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

        zergBot.workers
            .forEach { worker ->
                if (zergBot.isHarvestingMinerals(worker)) {
                    debugText(worker, "minerals")
                }
            }
    }

    private fun debugText(worker: Unit, text: String) {
        zergBot.debug()
            .debugTextOut(text, worker.position, Color.WHITE, 12)
    }

    private fun Unit.backToWork() {
        val closestMinerals = closestMineralsNearBase() ?: closestMinerals()
        closestMinerals
            ?.also {
                zergBot.actions()
                    .unitCommand(this, Abilities.HARVEST_GATHER_DRONE, it, false)
            }
    }

    private fun Unit.closestMinerals() =
        zergBot.mineralFields
            .minByOrNull {
                it.position.distance(position)
            }

    private fun Unit.closestMineralsNearBase() =
        bases.currentBases
            .flatMap {
                it.mineralFields
            }
            .minByOrNull {
                it.position.distance(position)
            }
}
