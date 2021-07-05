package de.stefanbissell.bots.creepy

import com.github.ocraft.s2client.protocol.action.ActionChat
import com.github.ocraft.s2client.protocol.data.Abilities
import com.github.ocraft.s2client.protocol.data.Units

class CreepyBot : ZergBot() {

    private val gameMap = GameMap(this)
    private val bases = Bases(this)
    private val buildOrder = BuildOrder(this, gameMap, bases)
    private val components = listOf(
        gameMap,
        bases,
        buildOrder
    )

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
                .filter {
                    it.type == Units.ZERG_ZERGLING &&
                        it.orders.isEmpty()
                }
                .forEach {
                    actions()
                        .unitCommand(it, Abilities.ATTACK, gameMap.enemyStart, false)
                }
        }
    }
}
