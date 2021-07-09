package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.spatial.Point
import com.github.ocraft.s2client.protocol.spatial.Point2d
import com.github.ocraft.s2client.protocol.unit.Unit as S2Unit

fun Point.towards(to: Point, distance: Float): Point {
    val dir = to.sub(this)
    val dist = to.distance(this).toFloat()
    if (dist < 0.1) {
        return this
    }
    return this.add(dir.div(dist).mul(distance))
}

fun List<S2Unit>.closestTo(unit: S2Unit) = closestTo(unit.position)

fun List<S2Unit>.closestTo(point: Point2d) = closestTo(Point.of(point.x, point.y))

fun List<S2Unit>.closestTo(point: Point) =
    minByOrNull { it.position.distance(point) }

fun List<S2Unit>.closerThan(unit: S2Unit, distance: Float) =
    filter { it.position.distance(unit.position) < distance }

fun Point.distance(point: Point2d) =
    distance(Point.of(point.x, point.y))
