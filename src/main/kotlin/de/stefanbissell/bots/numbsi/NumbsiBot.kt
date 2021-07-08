package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.bot.ClientEvents
import org.kodein.di.DI
import org.kodein.di.allInstances
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

class NumbsiBot : ZergBot() {

    private val di = DI {
        bind { instance(this@NumbsiBot) }
        bind { singleton { GameMap(instance()) } }
        bind { singleton { Bases(instance()) } }
        bind { singleton { FriendlyChat(instance()) } }
        bind { singleton { WorkerManager(instance(), instance()) } }
        bind { singleton { BuildOrder(instance(), instance(), instance()) } }
        bind { singleton { Attacker(instance(), instance()) } }
    }
    private val components by di.allInstances<BotComponent>()

    override fun onGameStart() {
        components.forEach {
            it.onGameStart()
        }
    }

    override fun onStep() {
        components.forEach {
            it.onStep()
        }

        debug().sendDebug()
    }
}

interface BotComponent : ClientEvents
