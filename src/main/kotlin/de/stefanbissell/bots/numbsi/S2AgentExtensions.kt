package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.debug.Color
import com.github.ocraft.s2client.protocol.spatial.Point
import com.github.ocraft.s2client.protocol.spatial.Point2d
import com.github.ocraft.s2client.protocol.unit.Unit as S2Unit

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

fun S2Unit.distance(other: S2Unit) =
    position.distance(other.position.toPoint2d())

fun Point2d.toPoint(): Point = Point.of(x, y)

fun List<S2Unit>.closerThan(unit: S2Unit, distance: Float) =
    filter { it.position.toPoint2d().distance(unit.position.toPoint2d()) < distance }

fun List<S2Unit>.closestDistanceTo(point: Point) = closestDistanceTo(point.toPoint2d())

fun List<S2Unit>.closestDistanceTo(unit: S2Unit) = closestDistanceTo(unit.position)

fun List<S2Unit>.closestDistanceTo(point: Point2d): Double? =
    minOfOrNull { it.position.distance(point) }

fun List<BotUnit>.closestTo(unit: BotUnit) =
    map { it.unit }.closestTo(unit.unit)

fun List<S2Unit>.closestTo(unit: S2Unit) = closestTo(unit.position)

fun List<S2Unit>.closestTo(point: Point) = closestTo(point.toPoint2d())

fun List<S2Unit>.closestTo(point: Point2d): S2Unit? =
    minByOrNull { it.position.distance(point) }

fun List<S2Unit>.closestPair(other: List<S2Unit>): Pair<S2Unit, S2Unit>? =
    if (this.isEmpty() || other.isEmpty()) {
        null
    } else {
        val firstUnit = minByOrNull {
            other.closestDistanceTo(it)!!
        }!!
        val secondUnit = other.closestTo(firstUnit)!!
        firstUnit to secondUnit
    }

fun debugText(
    zergBot: ZergBot,
    unit: S2Unit,
    text: String,
    color: Color = Color.WHITE
) {
    zergBot.debug()
        .debugTextOut(text, unit.position, color, 12)
}
