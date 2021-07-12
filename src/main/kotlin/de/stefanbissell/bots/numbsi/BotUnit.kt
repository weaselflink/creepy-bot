package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.Abilities
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

    val position: Point
        get() = wrapped.position

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

    private val isFlying by lazy {
        wrapped.flying.orElse(false)
    }

    private val canAttackAir by lazy {
        unitTypeData.weapons.any {
            it.targetType == Weapon.TargetType.ANY || it.targetType == Weapon.TargetType.AIR
        }
    }

    private val canAttackGround by lazy {
        unitTypeData.weapons.any {
            it.targetType == Weapon.TargetType.ANY || it.targetType == Weapon.TargetType.GROUND
        }
    }
}

fun S2Unit.toBotUnit(zergBot: ZergBot) =
    BotUnit(
        zergBot,
        this
    )
