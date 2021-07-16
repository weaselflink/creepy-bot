package de.stefanbissell.bots.numbsi

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly

class BotUnitTest {

    @Test
    fun `idle filters units`() {
        val units = listOf<BotUnit>(
            mockk { every { isIdle } returns true },
            mockk { every { isIdle } returns false },
            mockk { every { isIdle } returns true }
        )

        expectThat(units.idle)
            .containsExactly(units[0], units[2])
    }
}
