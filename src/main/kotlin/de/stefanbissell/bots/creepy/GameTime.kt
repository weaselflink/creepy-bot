package de.stefanbissell.bots.creepy

data class GameTime(
    val loop: Long
) {

    val exactMinutes: Double = (loop / 22.4) / 60.0
}
