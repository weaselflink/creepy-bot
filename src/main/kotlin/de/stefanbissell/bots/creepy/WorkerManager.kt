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
        val nextMinerals = bases.currentBases
            .flatMap {
                it.mineralFields
            }
            .minByOrNull {
                it.position.distance(position)
            }
            ?: zergBot.mineralFields
                .minByOrNull {
                    it.position.distance(position)
                }
        nextMinerals
            ?.also {
                zergBot.actions()
                    .unitCommand(this, Abilities.HARVEST_GATHER_DRONE, it, false)
            }
    }
}
