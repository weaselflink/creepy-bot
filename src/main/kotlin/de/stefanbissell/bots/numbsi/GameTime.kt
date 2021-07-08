package de.stefanbissell.bots.numbsi

data class GameTime(
    val loop: Long
) {

    val exactSeconds: Double = (loop / 22.4)
    val fullSeconds: Int = exactSeconds.toInt()
    val exactMinutes: Double = exactSeconds / 60.0
    val fullMinutes: Int = exactMinutes.toInt()
}
