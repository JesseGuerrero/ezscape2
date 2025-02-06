package com.rs.game.content.quests.ghosts_ahoy

import com.rs.engine.quest.Quest
import com.rs.engine.quest.QuestHandler
import com.rs.engine.quest.QuestOutline
import com.rs.game.content.items.Dye
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.lib.game.Tile
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onLogin

private const val Ectophial = 4251
val HARICANTO_TILE = Tile.of(3803, 3530, 0)

@QuestHandler(
    quest = Quest.GHOSTS_AHOY,
    startText = "Speak to Velorina in Port Phasmatys.",
    itemsText = "Approximately 1000 coins, thread, silk, spade, oak shieldbow, nettle tea, bucket of milk, ghostspeak amulet, 3 colours of dye, ectotokens or charter ships to enter Port Phasmatys three times.",
    combatText = "You will need to defeat a level 42 giant lobster.",
    rewardsText = "2,400 Prayer XP<br>" +
            "Free passage into Port Phasmatys<br>" +
            "The Ectophial",
    completedStage = 10
)

class GhostsAhoy : QuestOutline() {
    override fun getJournalLines(player: Player, stage: Int) = when (stage) {
        0 -> listOf("To start this quest, I can speak speak to Velorina in Port Phasmatys.")
        1 -> listOf("I have spoken with Velorina who has told me the sad history of the ghosts of Port Phasmatys. She has asked me to plead with Necrovarus in the Phasmatayan Temple to let any ghost who so wishes pass over into the next world.")
        2 -> listOf("I Pleaded with Necrovarus to no avail.")
        3 -> listOf("Velorina was crestfallen at Necrovarus' refusal to lift his ban, and she told me of a woman who fled Port Phasmatys before the townsfolk died, and to seek her out, as she may know of a way around Necrovarus' stubbornness.")
        4 -> listOf("I found the old woman, who told me of an enchantment she can perform on the Amulet of Ghostspeak, which will then let me command Necrovarus to do my bidding.")
        5 -> listOf("I need to bring the old woman the following items<br>Book of Haricanto<br>the Robes of Necrovarus<br>and something to translate the Book of Haricanto.")
        6 -> listOf("I brought the old woman the Book of Haricanto, the Robes of Necrovarus, and a translation manual.")
        7 -> listOf("The old woman used the items I brought her to perform the enchantment on the Amulet of Ghostspeak.")
        8 -> listOf("I have commanded Necrovarus to remove his ban.")
        9 -> listOf("I have told Velorina that Necrovarus has been commanded to remove his ban, and to allow any ghost who so desires to pass over into the next plane of existence. Velorina gave me the Ectophial in return, which I can use to teleport to the Temple of Phasmatys.")
        10 -> listOf("QUEST COMPLETE!")
        else -> listOf("Invalid quest stage. Report this to an administrator.")
    }

    override fun complete(player: Player) {
        sendQuestCompleteInterface(player, Ectophial)
        player.skills.addXp(Skills.PRAYER, 2400.0)
        player.vars.saveVar(217, 5)
    }

    override fun updateStage(player: Player, stage: Int) {
        if (stage == 5) {
            player.vars.setVarBit(217, 5) //Make Ak-Haranu visible
            if(player.questManager.getAttribs(Quest.GHOSTS_AHOY).getO<String>("sailColour1") == null) {
                val sailColour1 = Dye.entries[Utils.random(Dye.entries.size)]
                player.questManager.getAttribs(Quest.GHOSTS_AHOY).setO<String>("sailColour1", sailColour1.name)
                val sailColour2 = Dye.entries[Utils.random(Dye.entries.size)]
                player.questManager.getAttribs(Quest.GHOSTS_AHOY).setO<String>("sailColour2", sailColour2.name)
                val sailColour3 = Dye.entries[Utils.random(Dye.entries.size)]
                player.questManager.getAttribs(Quest.GHOSTS_AHOY).setO<String>("sailColour3", sailColour3.name)
                player.questManager.getAttribs(Quest.GHOSTS_AHOY).setO<String>("sailColour1Player", "white")
                player.questManager.getAttribs(Quest.GHOSTS_AHOY).setO<String>("sailColour2Player", "white")
                player.questManager.getAttribs(Quest.GHOSTS_AHOY).setO<String>("sailColour3Player", "white")
            }
        }
    }
}

@ServerStartupEvent
fun mapGAVarbits(){
    onLogin { (player) ->
        if (player.isQuestComplete(Quest.GHOSTS_AHOY))
            player.vars.setVarBit(217, 5)
        else if (player.getQuestStage(Quest.GHOSTS_AHOY) >= 5)
            player.vars.setVarBit(217, 5)
    }
}

object SailColorManager {
    fun getSailColor(player: Player, key: String): String {
        return player.questManager.getAttribs(Quest.GHOSTS_AHOY).getO<String>(key) ?: "white"
    }

    fun setSailColor(player: Player, key: String, value: String) {
        player.questManager.getAttribs(Quest.GHOSTS_AHOY).setO<String>(key, value)
    }

    fun getAllSailColors(player: Player): Map<String, String> {
        return mapOf(
            "sailColour1" to getSailColor(player, "sailColour1"),
            "sailColour1Player" to getSailColor(player, "sailColour1Player"),
            "sailColour2" to getSailColor(player, "sailColour2"),
            "sailColour2Player" to getSailColor(player, "sailColour2Player"),
            "sailColour3" to getSailColor(player, "sailColour3"),
            "sailColour3Player" to getSailColor(player, "sailColour3Player")
        )
    }
}

fun completeGhostsAhoy(player: Player) {
    player.packets.setIFGraphic(1244, 18, Ectophial)
    player.questManager.completeQuest(Quest.GHOSTS_AHOY)
}
