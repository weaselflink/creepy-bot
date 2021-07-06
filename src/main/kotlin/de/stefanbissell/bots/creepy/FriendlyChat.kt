package de.stefanbissell.bots.creepy

import com.github.ocraft.s2client.bot.ClientEvents
import com.github.ocraft.s2client.protocol.action.ActionChat

class FriendlyChat(
    private val zergBot: ZergBot
) : BotComponent {

    override fun onGameStart() {
        zergBot.actions()
            .sendChat("GL HF", ActionChat.Channel.BROADCAST)
    }
}
