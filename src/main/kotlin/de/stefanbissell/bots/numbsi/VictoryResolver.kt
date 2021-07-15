package de.stefanbissell.bots.numbsi

class VictoryResolver : BotComponent() {

    override fun onGameEnd(zergBot: ZergBot) {
        if (zergBot.ownStructures.isNotEmpty()) {
            println("I won!?!")
        } else {
            println("I lost!?!")
        }
    }
}
