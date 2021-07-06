package de.stefanbissell.bots.creepy

import com.github.ocraft.s2client.protocol.data.Abilities
import com.github.ocraft.s2client.protocol.unit.Unit

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
