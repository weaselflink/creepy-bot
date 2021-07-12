package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.Units
import com.github.ocraft.s2client.protocol.observation.raw.Visibility

class Attacker(
    private val gameMap: GameMap
) : BotComponent() {

    private var enoughTroops = false

    override fun onStep(zergBot: ZergBot) {
        val threats = threats(zergBot)
        val attackers = zergBot
            .ownCombatUnits
            .toBotUnits(zergBot)
        if (threats.isNotEmpty()) {
            attackers
                .forEach {
                    it.attackBest(threats)
                }
            return
        }
        updateEnoughTroops(zergBot, attackers)
        if (enoughTroops) {
            attackers
                .orderAttack(zergBot)
        } else if (zergBot.ownCombatUnits.isNotEmpty()) {
            val rallyPoint = attackers
                .map { it.position.toPoint2d() }
                .reduce { acc, point -> acc.add(point) }
                .div(zergBot.ownCombatUnits.count().toFloat())
            attackers
                .forEach {
                    if (it.position.distance(rallyPoint) > 5) {
                        it.move(rallyPoint)
                    }
                }
        }
    }

    private fun List<BotUnit>.orderAttack(zergBot: ZergBot) {
        val enemies = zergBot.enemyUnits
            .toBotUnits(zergBot)
        if (zergBot.observation().getVisibility(gameMap.enemyStart) == Visibility.HIDDEN) {
            idle.forEach {
                it.attack(gameMap.enemyStart)
            }
            return
        }
        if (enemies.isNotEmpty()) {
            filter {
                it.wrapped.orders.firstOrNull()?.targetedWorldSpacePosition?.isPresent ?: true
            }.forEach {
                it.attackBest(enemies)
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
                    it.move(scoutingTargets.random().toPoint2d())
                }
        }
    }

    private fun updateEnoughTroops(zergBot: ZergBot, ownCombatUnits: List<BotUnit>) {
        @Suppress("USELESS_CAST")
        val troops = ownCombatUnits
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
            .map {
                it.toBotUnit(zergBot)
            }

    private fun BotUnit.attackBest(targets: List<BotUnit>) {
        targets
            .filter {
                canAttack(it)
            }
            .closestTo(this)
            ?.also {
                attack(it)
            }
    }
}
