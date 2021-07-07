package de.stefanbissell.bots.creepy

import com.github.ocraft.s2client.protocol.data.Abilities
import com.github.ocraft.s2client.protocol.data.Ability
import com.github.ocraft.s2client.protocol.data.UnitType
import com.github.ocraft.s2client.protocol.data.Units
import com.github.ocraft.s2client.protocol.data.Upgrade
import com.github.ocraft.s2client.protocol.data.Upgrades
import com.github.ocraft.s2client.protocol.spatial.Point
import com.github.ocraft.s2client.protocol.unit.Unit

open class ZergBot : CommonBot() {

    private val baseTypes = listOf(
        Units.ZERG_HATCHERY,
        Units.ZERG_HIVE,
        Units.ZERG_LAIR
    )

    private val workerTypes = listOf(
        Units.ZERG_DRONE
    )

    private val trainingAbilities = mapOf(
        Units.ZERG_HATCHERY to Abilities.BUILD_HATCHERY,
        Units.ZERG_SPAWNING_POOL to Abilities.BUILD_SPAWNING_POOL,
        Units.ZERG_EXTRACTOR to Abilities.BUILD_EXTRACTOR,
        Units.ZERG_EVOLUTION_CHAMBER to Abilities.BUILD_EVOLUTION_CHAMBER,
        Units.ZERG_QUEEN to Abilities.TRAIN_QUEEN,
        Units.ZERG_DRONE to Abilities.TRAIN_DRONE,
        Units.ZERG_OVERLORD to Abilities.TRAIN_OVERLORD,
        Units.ZERG_ZERGLING to Abilities.TRAIN_ZERGLING
    )

    private val upgrades = listOf(
        UpgradeData(
            Upgrades.ZERGLING_MOVEMENT_SPEED,
            Abilities.RESEARCH_ZERGLING_METABOLIC_BOOST,
            Units.ZERG_SPAWNING_POOL
        ),
        UpgradeData(
            Upgrades.ZERGLING_ATTACK_SPEED,
            Abilities.RESEARCH_ZERGLING_ADRENAL_GLANDS,
            Units.ZERG_SPAWNING_POOL
        ),
        UpgradeData(
            Upgrades.ZERG_GROUND_ARMORS_LEVEL1,
            Abilities.RESEARCH_ZERG_GROUND_ARMOR,
            Units.ZERG_EVOLUTION_CHAMBER
        ),
        UpgradeData(
            Upgrades.ZERG_MELEE_WEAPONS_LEVEL1,
            Abilities.RESEARCH_ZERG_MELEE_WEAPONS,
            Units.ZERG_EVOLUTION_CHAMBER
        )
    ).associateBy { it.upgrade }

    val workers
        get() = ownUnits
            .filter {
                it.type in workerTypes
            }

    private val idleLarva
        get() = ownUnits
            .ofType(Units.ZERG_LARVA)
            .idle

    val baseBuildings
        get() = ownUnits
            .filter {
                it.type in baseTypes
            }

    fun pendingCount(type: UnitType): Int {
        return trainingAbilities[type]
            ?.let { ability ->
                ownUnits
                    .count { unit ->
                        unit.orders.any {
                            it.ability.abilityId == ability.abilityId
                        }
                    }
                    .let {
                        if (type == Units.ZERG_ZERGLING) it * 2 else it
                    }
            }
            ?: 0
    }

    fun readyCount(type: UnitType): Int {
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
        trainingAbilities[Units.ZERG_QUEEN]
            ?.also { ability ->
                baseBuildings
                    .filter {
                        canCast(it, ability, false)
                    }
                    .randomOrNull()
                    ?.also {
                        actions()
                            .unitCommand(it, ability, false)
                    }
            }

    }

    private fun trainUnitFromLarva(type: UnitType) {
        idleLarva
            .randomOrNull()
            ?.also { larva ->
                trainingAbilities[type]
                    ?.takeIf {
                        canCast(larva, it, false)
                    }
                    ?.also {
                        actions()
                            .unitCommand(larva, it, false)
                    }
            }
    }

    fun tryBuildStructure(type: UnitType, position: Point) {
        if (!canAfford(type)) {
            return
        }
        val ability = trainingAbilities[type] ?: return
        val builder = workers
            .filter {
                canCast(it, ability)
            }
            .randomOrNull()
            ?: return
        actions()
            .unitCommand(
                builder,
                ability,
                position.toPoint2d(),
                false
            )
    }

    fun tryBuildStructure(type: UnitType, target: Unit) {
        if (!canAfford(type)) {
            return
        }
        val ability = trainingAbilities[type] ?: return
        val builder = workers
            .filter {
                canCast(it, ability)
            }
            .randomOrNull()
            ?: return
        actions()
            .unitCommand(
                builder,
                ability,
                target,
                false
            )
    }

    fun isPending(upgrade: Upgrade): Boolean {
        val upgradeData = upgrades[upgrade] ?: return false
        return ownUnits
            .ofType(upgradeData.unitType)
            .flatMap { it.orders }
            .any { it.ability == upgradeData.ability }
    }

    fun tryResearchUpgrade(upgrade: Upgrade) {
        val upgradeData = upgrades[upgrade] ?: return
        val building = ownUnits
            .ofType(upgradeData.unitType)
            .filter {
                canCast(it, upgradeData.ability, false)
            }
            .randomOrNull()
            ?: return
        actions()
            .unitCommand(
                building,
                upgradeData.ability,
                false
            )
    }

    private fun canAfford(unitType: UnitType) = canAfford(cost(unitType))

    private fun canAfford(cost: Cost?): Boolean {
        return cost != null &&
            cost.supply <= supplyLeft &&
            cost.minerals <= observation().minerals &&
            cost.vespene <= observation().vespene
    }

    private fun cost(unitType: UnitType) =
        observation().getUnitTypeData(false)[unitType]
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
)

data class UpgradeData(
    val upgrade: Upgrade,
    val ability: Ability,
    val unitType: UnitType
)
