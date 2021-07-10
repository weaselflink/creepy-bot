package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.Abilities
import com.github.ocraft.s2client.protocol.data.Buffs

class QueenController : BotComponent() {

    override fun onStep(zergBot: ZergBot) {
        tryInjectLarva(zergBot)
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
