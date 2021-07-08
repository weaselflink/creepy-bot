package de.stefanbissell.bots.numbsi

data class GameTime(
    val loop: Long
) {

    val exactMinutes: Double = (loop / 22.4) / 60.0
}
