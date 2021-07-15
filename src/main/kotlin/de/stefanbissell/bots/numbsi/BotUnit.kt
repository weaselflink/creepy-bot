package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.bot.gateway.UnitInPool
import com.github.ocraft.s2client.protocol.data.*
import com.github.ocraft.s2client.protocol.spatial.Point
import com.github.ocraft.s2client.protocol.spatial.Point2d
import com.github.ocraft.s2client.protocol.unit.Alliance
import com.github.ocraft.s2client.protocol.unit.Tag
import com.github.ocraft.s2client.protocol.unit.UnitOrder
import com.github.ocraft.s2client.protocol.unit.Unit as S2Unit

class BotUnit(
    private val bot: CommonBot,
    val wrapped: S2Unit
) {

    private val unitTypeData: UnitTypeData by lazy {
        bot.observation().getUnitTypeData(false)[wrapped.type]!!
    }

    val tag: Tag
        get() = wrapped.tag

    val buffs: Set<Buff>
        get() = wrapped.buffs

    val weapons: Set<Weapon>
        get() = unitTypeData.weapons

    val alliance: Alliance
        get() = wrapped.alliance

    val isIdle by lazy {
        orders.isEmpty()
    }

    val isReady
        get() = wrapped.buildProgress == 1f

    val isStructure by lazy {
        unitTypeData.attributes
            .contains(UnitAttribute.STRUCTURE)
    }

    val type: UnitType
        get() = wrapped.type

    val position: Point
        get() = wrapped.position

    val orders: List<UnitOrder>
        get() = wrapped.orders

    val vespeneContents: Int
        get() = wrapped.vespeneContents.orElse(0)

    private fun distanceTo(target: BotUnit) =
        position.toPoint2d().distance(target.position.toPoint2d())

    fun use(ability: Ability) {
        bot.actions()
            .unitCommand(wrapped, ability, false)
    }

    fun use(ability: Ability, target: Point2d) {
        bot.actions()
            .unitCommand(wrapped, ability, target, false)
    }

    fun use(ability: Ability, target: BotUnit) {
        bot.actions()
            .unitCommand(wrapped, ability, target.wrapped, false)
    }

    fun move(target: Point2d) {
        use(Abilities.MOVE, target)
    }

    fun attack(target: BotUnit) {
        use(Abilities.ATTACK, target)
    }

    fun attack(target: Point2d) {
        use(Abilities.ATTACK, target)
    }

    fun canCast(
        ability: Ability,
        ignoreResourceRequirements: Boolean = false
    ) =
        bot.query()
            .getAbilitiesForUnit(wrapped, ignoreResourceRequirements)
            .abilities
            .map { it.ability }
            .contains(ability)

    fun canAttack(target: BotUnit) =
        (!target.isFlying && canAttackGround) || (target.isFlying && canAttackAir)

    fun inRange(target: BotUnit): Boolean =
        distanceTo(target)
            .let { distance ->
                unitTypeData.weapons
                    .filter { it.canAttack(target) }
                    .any { it.range >= distance }
            }

    private val isFlying by lazy {
        wrapped.flying.orElse(false)
    }

    private val canAttackAir by lazy {
        unitTypeData.weapons.any {
            it.canAttackAir
        }
    }

    private val canAttackGround by lazy {
        unitTypeData.weapons.any {
            it.canAttackGround
        }
    }

    private fun Weapon.canAttack(target: BotUnit) =
        (!target.isFlying && canAttackGround) || (target.isFlying && canAttackAir)

    private val Weapon.canAttackAir
        get() = targetType == Weapon.TargetType.ANY || targetType == Weapon.TargetType.AIR

    private val Weapon.canAttackGround
        get() = targetType == Weapon.TargetType.ANY || targetType == Weapon.TargetType.GROUND
}

fun S2Unit.toBotUnit(bot: CommonBot) = BotUnit(bot, this)

fun Iterable<UnitInPool>.toBotUnits(bot: CommonBot) =
    map { it.unit().toBotUnit(bot) }

fun Iterable<S2Unit>.asBotUnits(bot: CommonBot) =
    map { it.toBotUnit(bot) }

fun List<BotUnit>.ofType(type: UnitType) =
    filter { it.type == type }

fun List<BotUnit>.ofTypes(types: List<UnitType>) =
    filter { it.type in types }

val List<BotUnit>.ready
    get() = filter { it.isReady }

val List<BotUnit>.inProgress
    get() = filter { !it.isReady }

val List<BotUnit>.idle
    get() = filter {
        it.wrapped.orders.isEmpty()
    }

fun List<BotUnit>.closestTo(unit: BotUnit) =
    minByOrNull { it.position.distance(unit.position) }

fun List<BotUnit>.closestTo(point: Point2d) =
    minByOrNull { it.position.distance(point) }

fun List<BotUnit>.closerThan(unit: BotUnit, distance: Float) =
    filter { it.position.toPoint2d().distance(unit.position.toPoint2d()) < distance }

fun List<BotUnit>.closestDistanceTo(point: Point2d): Double? =
    minOfOrNull { it.position.distance(point) }

fun List<BotUnit>.closestDistanceTo(point: Point) = closestDistanceTo(point.toPoint2d())

fun List<BotUnit>.closestDistanceTo(unit: BotUnit) = closestDistanceTo(unit.position)

fun List<BotUnit>.closestPair(other: List<BotUnit>): Pair<BotUnit, BotUnit>? =
    if (this.isEmpty() || other.isEmpty()) {
        null
    } else {
        val firstUnit = minByOrNull {
            other.closestDistanceTo(it)!!
        }!!
        val secondUnit = other.closestTo(firstUnit)!!
        firstUnit to secondUnit
    }
