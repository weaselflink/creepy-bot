package de.stefanbissell.bots.creepy

import com.github.ocraft.s2client.protocol.action.ActionChat
import com.github.ocraft.s2client.protocol.data.Abilities
import com.github.ocraft.s2client.protocol.data.Units

class CreepyBot : ZergBot() {

    private val gameMap by lazy { GameMap(this) }
    private val buildOrder by lazy { BuildOrder(this, gameMap, bases) }
    private val components by lazy {
        listOf(
            gameMap,
            bases,
            buildOrder
        )
    }

    override fun onGameStart() {
        components.forEach {
            it.onGameStart()
        }

        actions()
            .sendChat("GL HF", ActionChat.Channel.BROADCAST)
    }

    override fun onStep() {
        components.forEach {
            it.onStep()
        }

        if (readyCount(Units.ZERG_ZERGLING) >= 12) {
            ownUnits
                .ofType(Units.ZERG_ZERGLING)
                .idle
                .forEach {
                    actions()
                        .unitCommand(it, Abilities.ATTACK, gameMap.enemyStart, false)
                }
        }
    }
}
