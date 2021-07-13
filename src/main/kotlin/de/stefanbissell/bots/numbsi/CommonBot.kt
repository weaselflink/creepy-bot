package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.bot.ClientEvents
import com.github.ocraft.s2client.bot.S2Agent
import com.github.ocraft.s2client.bot.gateway.*
import com.github.ocraft.s2client.protocol.data.*
import com.github.ocraft.s2client.protocol.unit.Alliance
import com.github.ocraft.s2client.protocol.unit.Unit
import com.github.ocraft.s2client.protocol.unit.UnitOrder

open class CommonBot(
    private val agent: S2Agent
) : ClientEvents {

    fun observation(): ObservationInterface = agent.observation()

    fun actions(): ActionInterface = agent.actions()

    fun query(): QueryInterface = agent.query()

    fun debug(): DebugInterface = agent.debug()

    private val mineralBuffs = listOf(
        Buffs.CARRY_MINERAL_FIELD_MINERALS,
        Buffs.CARRY_HIGH_YIELD_MINERAL_FIELD_MINERALS
    )

    private val vespeneBuffs = listOf(
        Buffs.CARRY_HARVESTABLE_VESPENE_GEYSER_GAS,
        Buffs.CARRY_HARVESTABLE_VESPENE_GEYSER_GAS_PROTOSS,
        Buffs.CARRY_HARVESTABLE_VESPENE_GEYSER_GAS_ZERG
    )

    private val mineralFieldTypes = listOf(
        Units.NEUTRAL_MINERAL_FIELD, Units.NEUTRAL_MINERAL_FIELD750,
        Units.NEUTRAL_RICH_MINERAL_FIELD, Units.NEUTRAL_RICH_MINERAL_FIELD750,
        Units.NEUTRAL_PURIFIER_MINERAL_FIELD, Units.NEUTRAL_PURIFIER_MINERAL_FIELD750,
        Units.NEUTRAL_PURIFIER_RICH_MINERAL_FIELD, Units.NEUTRAL_PURIFIER_RICH_MINERAL_FIELD750,
        Units.NEUTRAL_LAB_MINERAL_FIELD, Units.NEUTRAL_LAB_MINERAL_FIELD750,
        Units.NEUTRAL_BATTLE_STATION_MINERAL_FIELD, Units.NEUTRAL_BATTLE_STATION_MINERAL_FIELD750
    )

    private val workerTypes = listOf(
        Units.ZERG_DRONE,
        Units.ZERG_DRONE_BURROWED,
        Units.PROTOSS_PROBE,
        Units.TERRAN_SCV
    )

    private val vespeneBuildingTypes = listOf(
        Units.ZERG_EXTRACTOR, Units.ZERG_EXTRACTOR_RICH,
        Units.TERRAN_REFINERY, Units.TERRAN_REFINERY_RICH,
        Units.PROTOSS_ASSIMILATOR, Units.PROTOSS_ASSIMILATOR_RICH
    )

    private val vespeneGeyserTypes = listOf(
        Units.NEUTRAL_VESPENE_GEYSER, Units.NEUTRAL_PROTOSS_VESPENE_GEYSER,
        Units.NEUTRAL_SPACE_PLATFORM_GEYSER, Units.NEUTRAL_PURIFIER_VESPENE_GEYSER,
        Units.NEUTRAL_SHAKURAS_VESPENE_GEYSER, Units.NEUTRAL_RICH_VESPENE_GEYSER
    )

    private val resourceTypes = mineralFieldTypes + vespeneGeyserTypes

    val supplyCap by lazy {
        observation().foodCap
    }

    val supplyLeft by lazy {
        observation().foodCap - observation().foodUsed
    }

    private val neutralUnits by lazy {
        observation()
            .getUnits(Alliance.NEUTRAL)
            .asUnits()
    }

    private val allUnits by lazy {
        observation()
            .units
            .asUnits()
    }

    val workers by lazy {
        ownUnits.ofTypes(workerTypes)
    }

    val resources by lazy {
        neutralUnits
            .ofTypes(resourceTypes)
    }

    val mineralFields by lazy {
        neutralUnits
            .ofTypes(mineralFieldTypes)
    }

    private val vespeneGeysers by lazy {
        neutralUnits
            .ofTypes(vespeneGeyserTypes)
    }

    val emptyGeysers by lazy {
        vespeneGeysers
            .filter { geyser ->
                allUnits
                    .ofTypes(vespeneBuildingTypes)
                    .none { it.position.distance(geyser.position) < 1 }
            }
    }

    val ownUnits by lazy {
        observation()
            .getUnits(Alliance.SELF)
            .asUnits()
    }

    val enemyUnits by lazy {
        observation()
            .getUnits(Alliance.ENEMY)
            .asUnits()
    }

    val ownWorkingVespeneBuildings by lazy {
        ownUnits
            .ofTypes(vespeneBuildingTypes)
            .ready
            .filter {
                it.vespeneContents.orElse(0) > 0
            }
    }

    val gameTime = GameTime(observation().gameLoop)

    fun trainingAbility(unityType: UnitType) =
        observation()
            .getUnitTypeData(false)[unityType]
            ?.ability
            ?.orElse(null)

    fun canCast(
        unit: Unit,
        ability: Ability,
        ignoreResourceRequirements: Boolean = false
    ) =
        query()
            .getAbilitiesForUnit(unit, ignoreResourceRequirements)
            .abilities
            .map { it.ability }
            .contains(ability)

    fun isHarvestingMinerals(unit: Unit) =
        isHarvesting(unit, mineralFieldTypes, mineralBuffs)

    fun isHarvestingVespene(unit: Unit) =
        isHarvesting(unit, vespeneBuildingTypes, vespeneBuffs)

    private fun isHarvesting(unit: Unit, targets: List<UnitType>, buffs: List<Buffs>): Boolean {
        val gatherOrder = unit.orderOfType(Abilities.HARVEST_GATHER)
        if (gatherOrder != null) {
            return gatherOrder.targetUnit()?.type in targets
        }
        val returnOrder = unit.orderOfType(Abilities.HARVEST_RETURN)
        if (returnOrder != null) {
            return unit.buffs.intersect(buffs).isNotEmpty()
        }
        return false
    }

    private fun Unit.orderOfType(type: Ability) =
        orders.firstOrNull { it.ability == type }

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

fun List<Unit>.ofTypes(types: List<UnitType>) =
    filter { it.type in types }

val List<Unit>.idle
    get() = filter { it.orders.isEmpty() }

val List<Unit>.ready
    get() = filter { it.isReady }

val List<Unit>.inProgress
    get() = filter { !it.isReady }

val Unit.isReady
    get() = buildProgress == 1f
