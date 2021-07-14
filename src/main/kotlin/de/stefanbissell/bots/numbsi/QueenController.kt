package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.Abilities
import com.github.ocraft.s2client.protocol.data.Buffs
import com.github.ocraft.s2client.protocol.unit.Tag

class QueenController : BotComponent() {

    private val assignedQueens = mutableMapOf<Tag, Tag>()

    override fun onStep(zergBot: ZergBot) {
        assignQueens(zergBot)
        tryInjectLarva(zergBot)
        debugQueens(zergBot)
    }

    private fun assignQueens(zergBot: ZergBot) {
        removeDead(zergBot)
        val unassignedQueens = zergBot
            .queens
            .filter {
                it.tag !in assignedQueens.values
            }
        val basesNeedingQueen = zergBot
            .baseBuildings
            .ready
            .filter {
                it.tag !in assignedQueens.keys
            }
        basesNeedingQueen
            .closestPair(unassignedQueens)
            ?.also { (base, queen) ->
                assignedQueens[base.tag] = queen.tag
            }
    }

    private fun removeDead(zergBot: ZergBot) {
        val bases = zergBot
            .baseBuildings
            .ready
        assignedQueens
            .entries
            .removeIf { (baseTag, queenTag) ->
                bases.none { it.tag == baseTag } ||
                        zergBot.queens.none { it.tag == queenTag }
            }
    }

    private fun tryInjectLarva(zergBot: ZergBot) {
        val bases = zergBot.baseBuildings.ready
        val queens = zergBot.queens
        assignedQueens
            .map { (baseTag, queenTag) ->
                val base = bases.first { it.tag == baseTag }
                val queen = queens.first { it.tag == queenTag }
                base to queen
            }
            .firstOrNull { (base, queen) ->
                base.wrapped.buffs.none { it == Buffs.QUEEN_SPAWN_LARVA_TIMER } &&
                        queen.canCast(Abilities.EFFECT_INJECT_LARVA)
            }
            ?.also { (base, queen) ->
                queen.use(Abilities.EFFECT_INJECT_LARVA, base)
            }
    }

    private fun debugQueens(zergBot: ZergBot) {
        val bases = zergBot.baseBuildings.ready
        val queens = zergBot.queens
        var index = 1
        assignedQueens
            .map { (baseTag, queenTag) ->
                val base = bases.first { it.tag == baseTag }
                val queen = queens.first { it.tag == queenTag }
                debugText(zergBot, base, "base $index")
                debugText(zergBot, queen, "queen $index")
                index++
                base to queen
            }
        queens
            .filter { it.tag !in assignedQueens.values }
            .forEach {
                debugText(zergBot, it, "free")
            }
    }
}
