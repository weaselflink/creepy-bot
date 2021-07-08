package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.Abilities
import com.github.ocraft.s2client.protocol.data.Units

class Attacker(
    private val zergBot: ZergBot,
    private val gameMap: GameMap
) : BotComponent {

    override fun onStep() {
        if (enoughTroops()) {
            zergBot.ownUnits
                .ofType(Units.ZERG_ZERGLING)
                .idle
                .forEach {
                    zergBot.actions()
                        .unitCommand(it, Abilities.ATTACK, gameMap.enemyStart, false)
                }
        }
    }

    private fun enoughTroops(): Boolean {
        val troops = zergBot.readyCount(Units.ZERG_ZERGLING)
        return troops >= 40 || troops >= (zergBot.gameTime.exactMinutes * 5)
    }
}
