package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.bot.S2Agent
import com.github.ocraft.s2client.protocol.data.*
import com.github.ocraft.s2client.protocol.spatial.Point2d

open class ZergBot(
    agent: S2Agent
) : CommonBot(agent) {

    private val baseTypes = listOf(
        Units.ZERG_HATCHERY,
        Units.ZERG_HIVE,
        Units.ZERG_LAIR
    )

    private val combatTypes = listOf(
        Units.ZERG_ZERGLING,
        Units.ZERG_ROACH,
        Units.ZERG_HYDRALISK,
        Units.ZERG_MUTALISK
    )

    private val buildingAbilities = listOf(
        Abilities.BUILD_HATCHERY,
        Abilities.BUILD_SPAWNING_POOL,
        Abilities.BUILD_ROACH_WARREN,
        Abilities.BUILD_SPIRE,
        Abilities.BUILD_EXTRACTOR,
        Abilities.BUILD_EVOLUTION_CHAMBER
    )

    val bases by lazy { Bases(this) }

    val ownCombatUnits by lazy {
        ownUnits.ofTypes(combatTypes)
    }

    private val larva by lazy {
        ownUnits
            .ofType(Units.ZERG_LARVA)
    }

    private val idleLarva by lazy {
        larva.idle
    }

    val baseBuildings by lazy {
        ownUnits.ofTypes(baseTypes)
    }

    val queens by lazy {
        ownUnits.ofType(Units.ZERG_QUEEN)
    }

    val overlords by lazy {
        ownUnits.ofType(Units.ZERG_OVERLORD)
    }

    fun isBuilding(unit: BotUnit): Boolean {
        return unit
            .orders
            .map { it.ability }
            .any { it in buildingAbilities }
    }

    fun pendingCount(type: UnitType): Int {
        return trainingAbility(type)
            ?.let { ability ->
                ownUnits
                    .count { unit ->
                        unit.orders.any {
                            it.ability == ability
                        }
                    }
                    .let {
                        if (type == Units.ZERG_ZERGLING) it * 2 else it
                    }
            }
            ?: 0
    }

    private fun readyCount(type: UnitType): Int {
        return ownUnits.ofType(type).count()
    }

    fun totalCount(type: UnitType): Int {
        return readyCount(type) + pendingCount(type)
    }

    fun trainUnit(type: UnitType) {
        if (type == Units.ZERG_QUEEN) {
            trainQueen()
        } else {
            trainUnitFromLarva(type)
        }
    }

    private fun trainQueen() {
        trainingAbility(Units.ZERG_QUEEN)
            ?.also { ability ->
                val idleBases = baseBuildings
                    .filter {
                        it.canCast(ability)
                    }
                    .idle
                val idleBasesWithoutQueen = idleBases
                    .filter { idleBase ->
                        queens
                            .closerThan(idleBase, 9f)
                            .isEmpty()
                    }
                idleBasesWithoutQueen
                    .ifEmpty { idleBases }
                    .randomOrNull()
                    ?.also {
                        it.use(ability)
                    }
            }

    }

    private fun trainUnitFromLarva(type: UnitType) {
        idleLarva
            .randomOrNull()
            ?.also { larva ->
                trainingAbility(type)
                    ?.takeIf {
                        larva.canCast(it)
                    }
                    ?.also {
                        larva.use(it)
                    }
            }
    }

    private fun tryBuildStructure(type: UnitType, position: Point2d) {
        if (!canAfford(type)) {
            return
        }
        val ability = trainingAbility(type) ?: return
        val builder = workers
            .filter {
                it.canCast(ability)
            }
            .closestTo(position)
            ?: return
        builder.use(ability, position)
    }

    fun tryBuildStructure(gameMap: GameMap, type: UnitType) {
        when (type) {
            Units.ZERG_EXTRACTOR -> {
                bases
                    .currentBases
                    .flatMap {
                        it.emptyGeysers
                    }
                    .randomOrNull()
                    ?.also {
                        tryBuildStructure(type, it)
                    }
            }
            Units.ZERG_HATCHERY -> {
                emptyExpansions(gameMap)
                    .minByOrNull { (_, distance) ->
                        distance
                    }
                    ?.also { (expansion, _) ->
                        tryBuildStructure(Units.ZERG_HATCHERY, expansion)
                    }
            }
            Units.ZERG_LAIR -> {
                baseBuildings
                    .ready
                    .filter {
                        it.canCast(Abilities.MORPH_LAIR, false)
                    }
                    .closestTo(gameMap.ownStart)
                    ?.also {
                        it.use(Abilities.MORPH_LAIR)
                    }
            }
            Units.ZERG_HIVE -> {
                baseBuildings
                    .ready
                    .filter {
                        it.canCast(Abilities.MORPH_HIVE, false)
                    }
                    .closestTo(gameMap.ownStart)
                    ?.also {
                        it.use(Abilities.MORPH_HIVE)
                    }
            }
            else -> {
                bases
                    .map { it.building }
                    .closestTo(gameMap.ownStart)
                    ?.position
                    ?.towards(gameMap.center, 6f)
                    ?.random(4f)
                    ?.let {
                        gameMap.clampToMap(it)
                    }
                    ?.also {
                        tryBuildStructure(type, it)
                    }
            }
        }
    }

    fun tryResearchUpgrade(upgrade: Upgrade) {
        if (!canAfford(upgrade)) {
            return
        }
        val ability = researchAbility(upgrade) ?: return
        val building = ownStructures
            .idle
            .filter {
                it.canCast(ability)
            }
            .randomOrNull()
            ?: return
        building.use(ability)
    }

    fun canAfford(unitType: UnitType, count: Int = 1) =
        canAfford(cost(unitType)?.let { it * count })

    private fun emptyExpansions(gameMap: GameMap): Map<Point2d, Float> {
        val firstBase = gameMap
            .expansions
            .closestTo(gameMap.ownStart)
        return gameMap
            .expansionDistances[firstBase]!!
            .filter { (expansion, _) ->
                allStructures
                    .none {
                        it.position.distance(expansion) - it.radius < 2
                    }
            }
    }

    private fun canAfford(upgrade: Upgrade) =
        canAfford(cost(upgrade))

    private fun tryBuildStructure(type: UnitType, target: BotUnit) {
        if (!canAfford(type)) {
            return
        }
        val ability = trainingAbility(type) ?: return
        val builder = workers
            .filter {
                it.canCast(ability)
            }
            .randomOrNull()
            ?: return
        builder.use(ability, target)
    }

    private fun canAfford(cost: Cost?): Boolean {
        return cost != null &&
                cost.supply <= supplyLeft &&
                cost.minerals <= observation().minerals &&
                cost.vespene <= observation().vespene
    }

    private fun cost(upgrade: Upgrade) =
        observation()
            .getUpgradeData(false)[upgrade]
            ?.let {
                Cost(
                    supply = 0f,
                    minerals = it.mineralCost.orElse(0),
                    vespene = it.vespeneCost.orElse(0)
                )
            }

    private fun cost(unitType: UnitType) =
        observation()
            .getUnitTypeData(false)[unitType]
            ?.let {
                Cost(
                    supply = it.foodRequired.orElse(0f),
                    minerals = it.mineralCost.orElse(0),
                    vespene = it.vespeneCost.orElse(0)
                )
            }
}

data class Cost(
    val supply: Float,
    val minerals: Int,
    val vespene: Int
) {

    operator fun times(factor: Int): Cost =
        Cost(supply * factor, minerals * factor, vespene * factor)
}
