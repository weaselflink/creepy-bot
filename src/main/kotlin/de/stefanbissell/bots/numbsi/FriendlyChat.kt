package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.action.ActionChat

class FriendlyChat : BotComponent() {

    override fun onGameStart(zergBot: ZergBot) {
        zergBot.actions()
            .sendChat("GL HF", ActionChat.Channel.BROADCAST)
    }
}
