package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.spatial.Point
import com.github.ocraft.s2client.protocol.unit.Unit as S2Unit

fun Point.towards(to: Point, distance: Float): Point {
    val dir = to.sub(this)
    val dist = to.distance(this).toFloat()
    if (dist < 0.1) {
        return this
    }
    return this.add(dir.div(dist).mul(distance))
}

fun List<S2Unit>.closestTo(unit: S2Unit) =
    minByOrNull { it.position.distance(unit.position) }
