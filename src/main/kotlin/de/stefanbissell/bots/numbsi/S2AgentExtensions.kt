package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.debug.Color
import com.github.ocraft.s2client.protocol.spatial.Point
import com.github.ocraft.s2client.protocol.spatial.Point2d
import kotlin.random.Random

fun List<Point2d>.closestTo(point: Point2d) =
    minByOrNull { it.distance(point) }

fun Point.towards(to: Point2d, distance: Float): Point2d {
    val from = this.toPoint2d()
    val dir = to.sub(from)
    val dist = to.distance(from).toFloat()
    if (dist < 0.1) {
        return from
    }
    return from.add(dir.div(dist).mul(distance))
}

fun Point.distance(point: Point2d) =
    toPoint2d().distance(point)

fun Point2d.random(distance: Float): Point2d =
    add(getRandomScalar() * distance, getRandomScalar() * distance)

private fun getRandomScalar(): Float {
    return Random.nextFloat() * 2 - 1
}

fun debugText(
    zergBot: ZergBot,
    unit: BotUnit,
    text: String,
    color: Color = Color.WHITE
) {
    zergBot.debug()
        .debugTextOut(text, unit.position, color, 12)
}
