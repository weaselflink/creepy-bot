package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.spatial.Point2d
import com.github.ocraft.s2client.protocol.unit.Tag

class OverlordController(
    private val gameMap: GameMap
) : BotComponent() {

    private val fromEdge = 10f
    private lateinit var point: Point2d
    private lateinit var nearestExpansions: List<Point2d>
    private val scouts = mutableMapOf<Point2d, Tag>()

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
        val firstBase = gameMap
            .expansions
            .closestTo(gameMap.ownStart)
        nearestExpansions = gameMap
            .expansionDistances[firstBase]!!
            .entries
            .sortedBy { it.value }
            .limit(4)
            .map { it.key }
    }

    override fun onStep(zergBot: ZergBot) {
        removeScouts(zergBot)

        nearestExpansions
            .filter { point ->
                !scouts.containsKey(point)
            }
            .firstOrNull { point ->
                zergBot
                    .ownStructures
                    .none {
                        it.position.distance(point) - it.radius < 2
                    }
            }
            ?.also { point ->
                zergBot
                    .overlords
                    .filter {
                        !scouts.containsValue(it.tag)
                    }
                    .closestTo(point)
                    ?.also {
                        it.move(point)
                        scouts[point] = it.tag
                    }
            }

        zergBot
            .overlords
            .filter {
                !scouts.containsValue(it.tag)
            }
            .idle
            .forEach {
                if (it.distanceTo(point) > 6f) {
                    val point = point.random(4f)
                    it.move(point)
                }
            }
    }

    private fun removeScouts(zergBot: ZergBot) {
        scouts.entries
            .removeIf { (point, tag) ->
                zergBot.unit(tag) == null ||
                    zergBot
                        .ownStructures
                        .any {
                            it.position.distance(point) - it.radius < 2
                        }
            }
    }
}
