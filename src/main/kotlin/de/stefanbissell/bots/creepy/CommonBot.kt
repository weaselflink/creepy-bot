package de.stefanbissell.bots.creepy

import com.github.ocraft.s2client.bot.S2Agent
import com.github.ocraft.s2client.bot.gateway.UnitInPool
import com.github.ocraft.s2client.protocol.data.*
import com.github.ocraft.s2client.protocol.unit.Alliance
import com.github.ocraft.s2client.protocol.unit.Unit
import com.github.ocraft.s2client.protocol.unit.UnitOrder

open class CommonBot : S2Agent() {

    private val mineralBuffs = listOf(
        Buffs.CARRY_MINERAL_FIELD_MINERALS,
        Buffs.CARRY_HIGH_YIELD_MINERAL_FIELD_MINERALS
    )

    private val mineralFieldTypes = listOf(
        Units.NEUTRAL_MINERAL_FIELD, Units.NEUTRAL_MINERAL_FIELD750,
        Units.NEUTRAL_RICH_MINERAL_FIELD, Units.NEUTRAL_RICH_MINERAL_FIELD750,
        Units.NEUTRAL_PURIFIER_MINERAL_FIELD, Units.NEUTRAL_PURIFIER_MINERAL_FIELD750,
        Units.NEUTRAL_PURIFIER_RICH_MINERAL_FIELD, Units.NEUTRAL_PURIFIER_RICH_MINERAL_FIELD750,
        Units.NEUTRAL_LAB_MINERAL_FIELD, Units.NEUTRAL_LAB_MINERAL_FIELD750,
        Units.NEUTRAL_BATTLE_STATION_MINERAL_FIELD, Units.NEUTRAL_BATTLE_STATION_MINERAL_FIELD750
    )

    private val vespeneGeyserTypes = listOf(
        Units.NEUTRAL_VESPENE_GEYSER, Units.NEUTRAL_PROTOSS_VESPENE_GEYSER,
        Units.NEUTRAL_SPACE_PLATFORM_GEYSER, Units.NEUTRAL_PURIFIER_VESPENE_GEYSER,
        Units.NEUTRAL_SHAKURAS_VESPENE_GEYSER, Units.NEUTRAL_RICH_VESPENE_GEYSER
    )

    private val resourceTypes = mineralFieldTypes + vespeneGeyserTypes

    val supplyLeft
        get() = observation().foodCap - observation().foodUsed

    val resources
        get() = observation()
            .getUnits(Alliance.NEUTRAL)
            .asUnits()
            .ofTypes(resourceTypes)

    val mineralFields
        get() = observation()
            .getUnits(Alliance.NEUTRAL)
            .asUnits()
            .ofTypes(mineralFieldTypes)

    val vespeneGeysers
        get() = observation()
            .getUnits(Alliance.NEUTRAL)
            .asUnits()
            .ofTypes(vespeneGeyserTypes)

    val ownUnits
        get() = observation()
            .getUnits(Alliance.SELF)
            .asUnits()

    val gameTime
        get() = GameTime(observation().gameLoop)


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

    fun isHarvestingMinerals(unit: Unit): Boolean {
        val gatherOrder = unit.orders
            .firstOrNull { it.ability == Abilities.HARVEST_GATHER }
        if (gatherOrder != null) {
            return gatherOrder.targetUnit()?.type in mineralFieldTypes
        }
        val returnOrder = unit.orders
            .firstOrNull { it.ability == Abilities.HARVEST_RETURN }
        if (returnOrder != null) {
            return unit.buffs.intersect(mineralBuffs).isNotEmpty()
        }
        return false
    }

    private fun UnitOrder.targetUnit() =
        targetedUnitTag
            .map { it.value }
            .orElse(null)
            ?.let { id ->
                observation()
                    .units
                    .firstOrNull {
                        it.tag.value == id
                    }
                    ?.unit()
            }

    private fun Iterable<UnitInPool>.asUnits() = map { it.unit() }
}

fun List<Unit>.ofType(type: UnitType) =
    filter { it.type == type }

fun List<Unit>.ofTypes(vararg types: UnitType) =
    filter { it.type in types }

fun List<Unit>.ofTypes(types: List<UnitType>) =
    filter { it.type in types }

val List<Unit>.idle
    get() = filter { it.orders.isEmpty() }
