package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.Units
import com.github.ocraft.s2client.protocol.spatial.Point2d

class OverlordController(
    private val gameMap: GameMap
) : BotComponent() {

    private val fromEdge = 10f
    private lateinit var point: Point2d

    override fun onGameStart(zergBot: ZergBot) {
        val main = zergBot.baseBuildings.first().position.toPoint2d()
        val center = gameMap.center
        val x = if (main.x < center.x) {
            gameMap.playable.p0.x + fromEdge
        } else {
            gameMap.playable.p1.x - fromEdge
        }
        val y = if (main.y < center.y) {
            gameMap.playable.p0.y + fromEdge
        } else {
            gameMap.playable.p1.y - fromEdge
        }
        point = Point2d.of(x, y)
    }

    override fun onStep(zergBot: ZergBot) {
        zergBot
            .ownUnits
            .ofType(Units.ZERG_OVERLORD)
            .idle
            .forEach {
                if (it.distanceTo(point) > 6f) {
                    val point = point.random(4f)
                    it.move(point)
                }
            }
    }
}
