package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.Units
import com.github.ocraft.s2client.protocol.debug.Color
import com.github.ocraft.s2client.protocol.observation.raw.Visibility

class Attacker(
    private val gameMap: GameMap
) : BotComponent() {

    private var enoughTroops = false

    override fun onStep(zergBot: ZergBot) {
        val threats = threats(zergBot)
        val attackers = zergBot
            .ownCombatUnits
        if (threats.isNotEmpty()) {
            attackers.attackBest(threats)
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
                        it.debugLine(rallyPoint, Color.YELLOW)
                    }
                }
        }
    }

    private fun List<BotUnit>.orderAttack(zergBot: ZergBot) {
        if (zergBot.enemyUnits.isNotEmpty()) {
            attackBest(zergBot.enemyUnits)
            return
        }
        if (zergBot.observation().getVisibility(gameMap.enemyStart) == Visibility.HIDDEN) {
            forEach {
                it.move(gameMap.enemyStart)
                it.debugLine(gameMap.enemyStart, Color.YELLOW)
            }
            return
        }
        val scoutingTargets = gameMap.expansions
            .filter {
                zergBot.observation().getVisibility(it) != Visibility.VISIBLE
            }
        if (scoutingTargets.isNotEmpty()) {
            idle
                .forEach {
                    val target = scoutingTargets.random()
                    it.move(target)
                    it.debugLine(target, Color.YELLOW)
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
            troops >= 30 || troops >= (zergBot.gameTime.exactMinutes * 2)
        } else {
            troops >= 40 || troops >= (zergBot.gameTime.exactMinutes * 3)
        }
    }

    private fun threats(zergBot: ZergBot) =
        zergBot.enemyUnits
            .filter {
                it.weapons.isNotEmpty()
            }
            .filter {
                val distance = zergBot
                    .baseBuildings
                    .closestDistanceTo(it)
                    ?: 1000.0
                distance < 15
            }

    private fun List<BotUnit>.attackBest(targets: List<BotUnit>) {
        forEach {
            it.attackBest(targets)
        }
    }

    private fun BotUnit.attackBest(targets: List<BotUnit>) {
        targets
            .filter {
                canAttack(it)
            }
            .prioritize(
                { it.canAttack(this) },
                { it.inRange(this) }
            )
            .closestTo(this)
            ?.also {
                attack(it)
                debugLine(it, Color.YELLOW)
            }
    }
}
