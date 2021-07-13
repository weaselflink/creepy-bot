package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.Units
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

class PrioritizerKtTest {

    private val units = listOf<BotUnit>(
        mockk {
            every { type } returns Units.ZERG_ZERGLING
        },
        mockk {
            every { type } returns Units.ZERG_ROACH
        }
    )


    @Test
    fun `prioritizing empty list yields empty list`() {
        val result = emptyList<BotUnit>()
            .prioritize(
                { true }
            )

        expectThat(result).isEmpty()
    }

    @Test
    fun `prioritizing without conditions yields original list`() {
        val result = units
            .prioritize()

        expectThat(result).isEqualTo(units)
    }

    @Test
    fun `matching single condition are returned`() {
        val result = units
            .prioritize(
                { it.type == Units.ZERG_ROACH }
            )

        expectThat(result)
            .containsExactly(units[1])
    }

    @Test
    fun `not matching single condition returns original list`() {
        val result = units
            .prioritize(
                { it.type == Units.ZERG_MUTALISK }
            )

        expectThat(result)
            .isEqualTo(units)
    }

    @Test
    fun `last condition does not match but first one does`() {
        val result = units
            .prioritize(
                { it.type == Units.ZERG_ZERGLING },
                { it.type == Units.ZERG_MUTALISK }
            )

        expectThat(result)
            .containsExactly(units[0])
    }

    @Test
    fun `both conditions match last one wins`() {
        val result = units
            .prioritize(
                { it.type == Units.ZERG_ZERGLING },
                { it.type == Units.ZERG_ROACH }
            )

        expectThat(result)
            .containsExactly(units[1])
    }
}
