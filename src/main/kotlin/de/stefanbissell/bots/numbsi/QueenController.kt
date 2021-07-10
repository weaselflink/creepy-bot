package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.Abilities
import com.github.ocraft.s2client.protocol.data.Buffs

class QueenController : BotComponent() {

    override fun onStep(zergBot: ZergBot) {
        tryInjectLarva(zergBot)
    }

    private fun tryInjectLarva(zergBot: ZergBot) {
        val readyBases = zergBot
            .baseBuildings
            .ready
        val (nearQueens, farQueens) = zergBot.queens
            .partition {
                val closest = readyBases.closestDistanceTo(it)
                closest != null && closest < 9
            }
        nearQueens
            .idle
            .mapNotNull { queen ->
                readyBases
                    .firstOrNull { it.position.distance(queen.position) < 9 }
                    ?.let {
                        queen to it
                    }
            }
            .filter { (queen, base) ->
                zergBot.canCast(queen, Abilities.EFFECT_INJECT_LARVA) &&
                        base.buffs.none { it == Buffs.QUEEN_SPAWN_LARVA_TIMER }
            }
            .minByOrNull { (queen, base) ->
                queen.distance(base)
            }
            ?.also { (queen, base) ->
                zergBot.actions()
                    .unitCommand(queen, Abilities.EFFECT_INJECT_LARVA, base, false)
                return
            }
        farQueens
            .idle
            .mapNotNull { queen ->
                readyBases
                    .closestTo(queen)
                    ?.let {
                        queen to it
                    }
            }
            .filter { (queen, base) ->
                zergBot.canCast(queen, Abilities.EFFECT_INJECT_LARVA) &&
                        base.buffs.none { it == Buffs.QUEEN_SPAWN_LARVA_TIMER }
            }
            .minByOrNull { (queen, base) ->
                queen.distance(base)
            }
            ?.also { (queen, base) ->
                zergBot.actions()
                    .unitCommand(queen, Abilities.EFFECT_INJECT_LARVA, base, false)
                return
            }
    }
}
