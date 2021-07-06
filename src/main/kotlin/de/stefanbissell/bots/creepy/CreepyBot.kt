package de.stefanbissell.bots.creepy

class CreepyBot : ZergBot() {

    private val gameMap by lazy { GameMap(this) }
    private val friendlyChat by lazy { FriendlyChat(this) }
    private val buildOrder by lazy { BuildOrder(this, gameMap, bases) }
    private val attacker by lazy { Attacker(this, gameMap) }
    private val components by lazy {
        listOf(
            gameMap,
            friendlyChat,
            bases,
            buildOrder,
            attacker
        )
    }

    override fun onGameStart() {
        components.forEach {
            it.onGameStart()
        }
    }

    override fun onStep() {
        components.forEach {
            it.onStep()
        }
    }
}
