package de.stefanbissell.bots.numbsi

data class GameTime(
    val loop: Long
) {

    val exactSeconds: Double = (loop / 22.4)
    val exactMinutes: Double = exactSeconds / 60.0
}
