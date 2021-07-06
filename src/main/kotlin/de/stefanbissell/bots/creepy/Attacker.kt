package de.stefanbissell.bots.creepy

import com.github.ocraft.s2client.protocol.data.Abilities
import com.github.ocraft.s2client.protocol.data.Units

class Attacker(
    private val zergBot: ZergBot,
    private val gameMap: GameMap
) : BotComponent {

    override fun onStep() {
        if (zergBot.readyCount(Units.ZERG_ZERGLING) >= 12) {
            zergBot.ownUnits
                .ofType(Units.ZERG_ZERGLING)
                .idle
                .forEach {
                    zergBot.actions()
                        .unitCommand(it, Abilities.ATTACK, gameMap.enemyStart, false)
                }
        }
    }
}
