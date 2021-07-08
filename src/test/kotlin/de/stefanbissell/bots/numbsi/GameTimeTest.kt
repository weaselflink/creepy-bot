package de.stefanbissell.bots.numbsi

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class GameTimeTest {

    @Test
    fun `calculates exact seconds`() {
        expectThat(GameTime(67).exactSeconds)
            .isEqualTo(  2.9910, 0.0001)
    }

    @Test
    fun `calculates full seconds`() {
        expectThat(GameTime(67).fullSeconds)
            .isEqualTo(2)
        expectThat(GameTime(68).fullSeconds)
            .isEqualTo(3)
    }

    @Test
    fun `calculates exact minutes`() {
        expectThat(GameTime(150).exactMinutes)
            .isEqualTo(0.1116, 0.0001)
    }

    @Test
    fun `calculates full minutes`() {
        expectThat(GameTime(2687).fullMinutes)
            .isEqualTo(1)
        expectThat(GameTime(2688).fullMinutes)
            .isEqualTo(2)
    }
}
