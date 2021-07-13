package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.debug.Color
import com.github.ocraft.s2client.protocol.spatial.Point
import com.github.ocraft.s2client.protocol.spatial.Point2d

fun Point.towards(to: Point, distance: Float): Point {
    val from = this
    val dir = to.sub(from)
    val dist = to.distance(from).toFloat()
    if (dist < 0.1) {
        return from
    }
    return this.add(dir.div(dist).mul(distance))
}

fun Point.distance(point: Point2d) =
    toPoint2d().distance(point)

fun debugText(
    zergBot: ZergBot,
    unit: BotUnit,
    text: String,
    color: Color = Color.WHITE
) {
    zergBot.debug()
        .debugTextOut(text, unit.position, color, 12)
}
