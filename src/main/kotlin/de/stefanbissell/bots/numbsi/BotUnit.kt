package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.Abilities
import com.github.ocraft.s2client.protocol.data.UnitType
import com.github.ocraft.s2client.protocol.data.UnitTypeData
import com.github.ocraft.s2client.protocol.data.Weapon
import com.github.ocraft.s2client.protocol.spatial.Point
import com.github.ocraft.s2client.protocol.spatial.Point2d
import com.github.ocraft.s2client.protocol.unit.Unit as S2Unit

class BotUnit(
    val zergBot: ZergBot,
    val wrapped: S2Unit
) {

    private val unitTypeData: UnitTypeData by lazy {
        zergBot.observation().getUnitTypeData(false)[wrapped.type]!!
    }

    val type: UnitType
        get() = wrapped.type

    val position: Point
        get() = wrapped.position

    fun distanceTo(target: BotUnit) =
        position.toPoint2d().distance(target.position.toPoint2d())

    fun move(target: Point2d) {
        zergBot.actions()
            .unitCommand(wrapped, Abilities.MOVE, target, false)
    }

    fun attack(target: BotUnit) {
        zergBot.actions()
            .unitCommand(wrapped, Abilities.ATTACK, target.wrapped, false)
    }

    fun attack(target: Point2d) {
        zergBot.actions()
            .unitCommand(wrapped, Abilities.ATTACK, target, false)
    }

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

fun S2Unit.toBotUnit(zergBot: ZergBot) =
    BotUnit(
        zergBot,
        this
    )
