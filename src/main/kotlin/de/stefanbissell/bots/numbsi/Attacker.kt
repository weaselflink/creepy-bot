package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.Units
import com.github.ocraft.s2client.protocol.observation.raw.Visibility
import com.github.ocraft.s2client.protocol.unit.CloakState
import com.github.ocraft.s2client.protocol.unit.Unit as S2Unit

class Attacker(
    private val gameMap: GameMap
) : BotComponent() {

    private var enoughTroops = false

    override fun onStep(zergBot: ZergBot) {
        val threats = threats(zergBot)
        if (threats.isNotEmpty()) {
            zergBot
                .ownCombatUnits
                .forEach { unit ->
                    attackBest(zergBot, unit, threats)
                }
            return
        }
        updateEnoughTroops(zergBot)
        if (enoughTroops) {
            zergBot
                .ownCombatUnits
                .orderAttack(zergBot)
        } else if (zergBot.ownCombatUnits.isNotEmpty()) {
            val rallyPoint = zergBot
                .ownCombatUnits
                .map { it.position.toPoint2d() }
                .reduce { acc, point -> acc.add(point) }
                .div(zergBot.ownCombatUnits.count().toFloat())
            zergBot
                .ownCombatUnits
                .forEach {
                    if (it.position.distance(rallyPoint) > 5) {
                        zergBot.move(it, rallyPoint)
                    }
                }
        }
    }

    private fun List<S2Unit>.orderAttack(zergBot: ZergBot) {
        val enemies = zergBot.enemyUnits
            .filter {
                !it.flying.orElse(true) &&
                        it.cloakState.orElse(CloakState.CLOAKED_UNKNOWN) != CloakState.CLOAKED
            }
        if (zergBot.observation().getVisibility(gameMap.enemyStart) == Visibility.HIDDEN) {
            idle.forEach {
                zergBot.attack(it, gameMap.enemyStart)
            }
            return
        }
        if (enemies.isNotEmpty()) {
            filter {
                it.orders.firstOrNull()?.targetedWorldSpacePosition?.isPresent ?: true
            }.forEach {
                attackBest(zergBot, it, enemies)
            }
            return
        }
        val scoutingTargets = gameMap.expansions
            .filter {
                zergBot.observation().getVisibility(it.toPoint2d()) != Visibility.VISIBLE
            }
        if (scoutingTargets.isNotEmpty()) {
            idle
                .forEach {
                    zergBot.move(it, scoutingTargets.random().toPoint2d())
                }
        }
    }

    private fun updateEnoughTroops(zergBot: ZergBot) {
        @Suppress("USELESS_CAST")
        val troops = zergBot
            .ownCombatUnits
            .sumOf {
                when (it.type) {
                    Units.ZERG_MUTALISK -> 3
                    Units.ZERG_ROACH -> 2
                    Units.ZERG_HYDRALISK -> 2
                    else -> 1
                } as Int
            }
        enoughTroops = if (enoughTroops) {
            troops >= 30 || troops >= (zergBot.gameTime.exactMinutes * 3)
        } else {
            troops >= 40 || troops >= (zergBot.gameTime.exactMinutes * 5)
        }
    }

    private fun threats(zergBot: ZergBot) =
        zergBot.enemyUnits
            .mapNotNull { unit ->
                zergBot.observation().getUnitTypeData(false)[unit.type]
                    ?.let { unit to it }
            }
            .mapNotNull { (unit, data) ->
                if (data.weapons.isNotEmpty()) unit else null
            }
            .filter { unit ->
                val distance = zergBot
                    .baseBuildings
                    .closestDistanceTo(unit)
                    ?: 1000.0
                distance < 15
            }

    private fun attackBest(zergBot: ZergBot, unit: S2Unit, targets: List<S2Unit>) {
        val attacker = unit.toBotUnit(zergBot)
        targets
            .map {
                it.toBotUnit(zergBot)
            }
            .filter {
                attacker.canAttack(it)
            }
            .closestTo(attacker)
            ?.also {
                zergBot.attack(unit, it)
            }
    }
}
