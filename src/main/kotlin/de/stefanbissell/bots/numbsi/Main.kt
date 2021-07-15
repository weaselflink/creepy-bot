package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.bot.S2Coordinator
import com.github.ocraft.s2client.protocol.game.Difficulty
import com.github.ocraft.s2client.protocol.game.LocalMap
import com.github.ocraft.s2client.protocol.game.Race
import kotlin.io.path.Path

fun main(args: Array<String>) {
    val maps = listOf(
        "2000AtmospheresAIE.SC2Map",
        "BlackburnAIE.SC2Map",
        "JagannathaAIE.SC2Map",
        "LightShadeAIE.SC2Map",
        "OxideAIE.SC2Map",
        "RomanticideAIE.SC2Map"
    )

    val s2Coordinator = S2Coordinator
        .setup()
        .setRealtime(false)
        .loadSettings(args)
        .setParticipants(
            S2Coordinator.createParticipant(Race.ZERG, NumbsiBot(true)),
            S2Coordinator.createComputer(Race.TERRAN, Difficulty.HARD)
        )
        .launchStarcraft()
        .startGame(LocalMap.of(Path(maps.random())))

    while (s2Coordinator.update()) {
        // nothing
    }

    s2Coordinator.quit()
}
