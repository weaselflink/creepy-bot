package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.Units
import com.github.ocraft.s2client.protocol.game.raw.StartRaw
import com.github.ocraft.s2client.protocol.spatial.Point2d
import com.github.ocraft.s2client.protocol.unit.Alliance
import java.lang.Float.max
import java.lang.Float.min

class GameMap : BotComponent(0) {

    private lateinit var startRaw: StartRaw
    lateinit var expansions: List<Point2d>
    lateinit var ownStart: Point2d
    lateinit var enemyStart: Point2d

    private val width by lazy { startRaw.mapSize.x }
    private val height by lazy { startRaw.mapSize.x }
    val center: Point2d by lazy { Point2d.of(width / 2f, height / 2f) }

    override fun onGameStart(zergBot: ZergBot) {
        startRaw = zergBot.observation().gameInfo.startRaw.get()
        expansions = zergBot.query()
            .calculateExpansionLocations(zergBot.observation())
            .map { it.toPoint2d() }
        ownStart = zergBot.observation()
            .getUnits(Alliance.SELF) { it.unit().type == Units.ZERG_HATCHERY }
            .map { it.unit() }
            .first()
            .position
            .toPoint2d()
        enemyStart = zergBot.observation()
            .gameInfo.startRaw.get()
            .startLocations
            .first {
                it.distance(ownStart) > 9
            }
    }

    fun clampToMap(point: Point2d): Point2d =
        Point2d.of(
            min(max(0f, point.x), width.toFloat()),
            min(max(0f, point.y), height.toFloat())
        )
}
