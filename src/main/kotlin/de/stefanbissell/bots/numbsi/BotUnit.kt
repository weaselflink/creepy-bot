package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.UnitTypeData
import com.github.ocraft.s2client.protocol.data.Weapon
import com.github.ocraft.s2client.protocol.unit.Unit as S2Unit

class BotUnit(
    val zergBot: ZergBot,
    val unit: S2Unit,
    private val unitTypeData: UnitTypeData
) {

    private val isFlying by lazy {
        unit.flying.orElse(false)
    }

    fun canAttack(target: BotUnit) =
        (!target.isFlying && canAttackGround) || (target.isFlying && canAttackAir)

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
        this,
        zergBot.observation().getUnitTypeData(false)[this.type]!!
    )
