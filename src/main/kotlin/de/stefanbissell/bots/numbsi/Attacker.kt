package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.Abilities
import com.github.ocraft.s2client.protocol.observation.raw.Visibility
import com.github.ocraft.s2client.protocol.unit.CloakState
import com.github.ocraft.s2client.protocol.unit.Unit as S2Unit

class Attacker(
    private val gameMap: GameMap
) : BotComponent() {

    private var enoughTroops = false

    override fun onStep(zergBot: ZergBot) {
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
                        zergBot.actions()
                            .unitCommand(it, Abilities.MOVE, rallyPoint, false)
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
                zergBot.actions()
                    .unitCommand(it, Abilities.ATTACK, gameMap.enemyStart, false)
            }
            return
        }
        if (enemies.isNotEmpty()) {
            filter {
                it.orders.firstOrNull()?.targetedWorldSpacePosition?.isPresent ?: true
            }.forEach {
                zergBot.actions()
                    .unitCommand(it, Abilities.ATTACK, enemies.closestTo(it), false)
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
                    zergBot.actions()
                        .unitCommand(it, Abilities.MOVE, scoutingTargets.random().toPoint2d(), false)
                }
        }
    }

    private fun updateEnoughTroops(zergBot: ZergBot) {
        val troops = zergBot
            .ownCombatUnits
            .count()
        enoughTroops = if (enoughTroops) {
            troops >= 30 || troops >= (zergBot.gameTime.exactMinutes * 3)
        } else {
            troops >= 40 || troops >= (zergBot.gameTime.exactMinutes * 5)
        }
    }
}
