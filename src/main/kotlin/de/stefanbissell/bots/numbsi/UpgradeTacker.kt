package de.stefanbissell.bots.numbsi

import com.github.ocraft.s2client.protocol.data.Upgrade

class UpgradeTacker : BotComponent() {

    private val completedUpgrades = mutableSetOf<Upgrade>()

    override fun onUpgradeCompleted(zergBot: ZergBot, upgrade: Upgrade) {
        completedUpgrades += upgrade
    }

    private fun isCompleted(upgrade: Upgrade) = upgrade in completedUpgrades

    fun isCompletedOrPending(zergBot: ZergBot, upgrade: Upgrade): Boolean {
        if (isCompleted(upgrade)) return true
        val ability = zergBot.researchAbility(upgrade)
        return zergBot
            .ownUnits
            .flatMap { it.orders }
            .any { it.ability == ability }
    }
}
