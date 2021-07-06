package de.stefanbissell.bots.creepy

import com.github.ocraft.s2client.bot.S2Agent
import com.github.ocraft.s2client.protocol.data.Units
import com.github.ocraft.s2client.protocol.spatial.Point
import com.github.ocraft.s2client.protocol.spatial.Point2d
import com.github.ocraft.s2client.protocol.unit.Alliance
import java.lang.Float.max
import java.lang.Float.min

class GameMap(
    private val sc2Agent: S2Agent
) : BotComponent {

    private val startRaw by lazy {
        sc2Agent.observation().gameInfo.startRaw.get()
    }
    lateinit var expansions: List<Point>
    lateinit var ownStart: Point2d
    lateinit var enemyStart: Point2d

    private val width by lazy { startRaw.mapSize.x }
    private val height by lazy { startRaw.mapSize.x }
    val center: Point by lazy { Point.of(width / 2f, height / 2f) }

    override fun onGameStart() {
        expansions = sc2Agent.query()
            .calculateExpansionLocations(sc2Agent.observation())
        ownStart = sc2Agent.observation()
            .getUnits(Alliance.SELF) { it.unit().type == Units.ZERG_HATCHERY }
            .map { it.unit() }
            .first()
            .position
            .toPoint2d()
        enemyStart = sc2Agent.observation()
            .gameInfo.startRaw.get()
            .startLocations
            .first {
                it.distance(ownStart) > 9
            }
    }

    fun clampToMap(point: Point): Point =
        Point.of(
            min(max(0f, point.x), width.toFloat()),
            min(max(0f, point.y), height.toFloat())
        )
}
