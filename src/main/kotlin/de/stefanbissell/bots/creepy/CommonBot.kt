package de.stefanbissell.bots.creepy

import com.github.ocraft.s2client.bot.S2Agent
import com.github.ocraft.s2client.bot.gateway.UnitInPool
import com.github.ocraft.s2client.protocol.data.Ability
import com.github.ocraft.s2client.protocol.data.UnitType
import com.github.ocraft.s2client.protocol.unit.Alliance
import com.github.ocraft.s2client.protocol.unit.Unit

open class CommonBot : S2Agent() {

    val supplyLeft
        get() = observation().foodCap - observation().foodUsed

    val ownUnits
        get() = observation()
            .getUnits(Alliance.SELF)
            .asUnits()

    fun canCast(
        unit: Unit,
        ability: Ability,
        ignoreResourceRequirements: Boolean = true
    ) =
        query()
            .getAbilitiesForUnit(unit, ignoreResourceRequirements)
            .abilities
            .map { it.ability }
            .contains(ability)

    private fun Iterable<UnitInPool>.asUnits() = map { it.unit() }
}

fun List<Unit>.ofType(type: UnitType) =
    filter { it.type == type }

fun List<Unit>.ofTypes(vararg type: UnitType) =
    filter { it.type in type }

val List<Unit>.idle
    get() = filter { it.orders.isEmpty() }
