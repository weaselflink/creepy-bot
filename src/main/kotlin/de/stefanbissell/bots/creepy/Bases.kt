package de.stefanbissell.bots.creepy

import com.github.ocraft.s2client.bot.ClientEvents
import com.github.ocraft.s2client.protocol.data.Units
import com.github.ocraft.s2client.protocol.spatial.Point

class Bases(
    private val zergBot: ZergBot
) : ClientEvents {

    private val baseTypes = listOf(
        Units.ZERG_HATCHERY,
        Units.ZERG_HIVE,
        Units.ZERG_LAIR
    )

    val currentBases: MutableList<Base> = mutableListOf()

    override fun onStep() {
        currentBases.removeIf { base ->
            zergBot.observation().units.none { it.tag.value == base.building }
        }
        val baseBuildings = zergBot.observation().units
            .filter {
                it.unit().type in baseTypes
            }
            .associateBy { it.tag.value }
        baseBuildings.values
            .forEach {
                currentBases += Base(
                    building = it.tag.value,
                    position = it.unit().position
                )
            }

    }
}

class Base(
    val building: Long,
    val position: Point
)
