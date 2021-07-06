package de.stefanbissell.bots.creepy

import com.github.ocraft.s2client.protocol.data.Abilities
import com.github.ocraft.s2client.protocol.data.Buffs
import com.github.ocraft.s2client.protocol.data.Units
import com.github.ocraft.s2client.protocol.spatial.Point

class Bases(
    private val zergBot: ZergBot
) : BotComponent {

    private val baseTypes = listOf(
        Units.ZERG_HATCHERY,
        Units.ZERG_HIVE,
        Units.ZERG_LAIR
    )

    val currentBases: MutableList<Base> = mutableListOf()

    val baseBuildings
        get() = zergBot
            .ownUnits
            .filter {
                it.type in baseTypes
            }

    override fun onStep() {
        currentBases.removeIf { base ->
            zergBot.observation()
                .units
                .none { it.tag.value == base.buildingId }
        }
        baseBuildings
            .filter { building ->
                currentBases
                    .none { it.buildingId == building.tag.value }
            }
            .forEach {
                currentBases += Base(
                    zergBot = zergBot,
                    buildingId = it.tag.value,
                    position = it.position
                )
            }
        tryInjectLarva()
    }

    private fun tryInjectLarva() {
        zergBot.ownUnits
            .ofType(Units.ZERG_QUEEN)
            .idle
            .mapNotNull { queen ->
                baseBuildings
                    .firstOrNull { it.position.distance(queen.position) < 9 }
                    ?.let {
                        queen to it
                    }
            }
            .filter { (queen, base) ->
                zergBot.canCast(queen, Abilities.EFFECT_INJECT_LARVA) &&
                    base.buffs.none { it.buffId == Buffs.QUEEN_SPAWN_LARVA_TIMER.buffId }
            }
            .randomOrNull()
            ?.also { (queen, base) ->
                zergBot.actions()
                    .unitCommand(queen, Abilities.EFFECT_INJECT_LARVA, base, false)
            }
    }
}

class Base(
    private val zergBot: ZergBot,
    val buildingId: Long,
    val position: Point
) {

    private val building
        get() = zergBot
            .ownUnits
            .firstOrNull {
                it.tag.value == buildingId
            }

    val mineralFields
        get() = building
            ?.let { b ->
                zergBot
                    .mineralFields
                    .filter {
                        it.position.distance(b.position) < 9f
                    }
            }
            ?: emptyList()
}
