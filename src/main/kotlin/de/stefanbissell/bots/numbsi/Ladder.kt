package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.bot.S2Coordinator
import com.github.ocraft.s2client.protocol.game.Difficulty
import com.github.ocraft.s2client.protocol.game.LocalMap
import com.github.ocraft.s2client.protocol.game.Race
import kotlin.io.path.Path

fun main(args: Array<String>) {
    val s2Coordinator = S2Coordinator
        .setup()
        .setTimeoutMS(300_000)
        .loadLadderSettings(args)
        .setParticipants(
            S2Coordinator.createParticipant(Race.ZERG, NumbsiBot())
        )
        .connectToLadder()
        .joinGame()

    while (s2Coordinator.update()) {
        // nothing
    }

    s2Coordinator.quit()
}
