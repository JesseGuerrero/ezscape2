package com.rs.game.content.quests.ghosts_ahoy

import com.rs.engine.quest.Quest
import com.rs.engine.quest.QuestHandler
import com.rs.engine.quest.QuestOutline
import com.rs.game.content.items.Dye
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.lib.Constants
import com.rs.lib.game.Rights
import com.rs.lib.game.Tile
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onLogin

private const val Ectophial = 4251
val HARICANTO_TILE = Tile.of(3803, 3530, 0)

@QuestHandler(
    quest = Quest.GHOSTS_AHOY,
    startText = "Speak to Velorina in Port Phasmatys.",
    itemsText = "Approximately 1000 coins, thread, silk, spade, oak longbow, nettle tea, bucket of milk, ghostspeak amulet, 3 colours of dye, ectotokens or charter ships to enter Port Phasmatys three times.",
    combatText = "You will need to defeat a level 42 giant lobster.",
    rewardsText = "2,400 Prayer XP<br>" +
            "Free passage into Port Phasmatys<br>" +
            "The Ectophial",
    completedStage = 10
)

class GhostsAhoy : QuestOutline() {

    companion object {
        const val STAGE_1_BEGIN_QUEST = 1
        const val STAGE_2_PLEAD_WITH_NECROVARUS = 2
        const val STAGE_3_NECROVARUS_REFUSES = 3
        const val STAGE_4_SEEK_OLD_WOMAN = 4
        const val STAGE_5_GATHER_ITEMS = 5
        const val STAGE_6_AMULET_ENCHANTED = 6
        const val STAGE_7_COMMAND_NECROVARUS = 7
        const val STAGE_8_TELL_VELORINA = 8
        const val STAGE_9_QUEST_COMPLETE = 9
    }

    override fun getJournalLines(player: Player, stage: Int): List<String> {
        return when (stage) {
            STAGE_1_BEGIN_QUEST -> listOf(
                "To start this quest, I can speak to Velorina in Port Phasmatys."
            )

            STAGE_2_PLEAD_WITH_NECROVARUS -> listOf(
                "Velorina told me of the trouble the ghosts in Port Phasmatys face.",
                "She wants me to plead with Necrovarus in the temple to let them pass on."
            )

            STAGE_3_NECROVARUS_REFUSES -> listOf(
                "My pleas to Necrovarus were unsuccessful.",
                "Velorina mentioned an old woman who fled the city before the tragedy—maybe she has a plan."
            )

            STAGE_4_SEEK_OLD_WOMAN -> listOf(
                "I found the old woman, who says she can enchant my Amulet of Ghostspeak to command Necrovarus,",
                "but I need to bring her three items: the Book of Haricanto, the Robes of Necrovarus, and a translation manual."
            )

            STAGE_5_GATHER_ITEMS -> listOf(
                "I must gather:",
                "- The Book of Haricanto",
                "- The Robes of Necrovarus",
                "- A translation manual",
                "…and return to the old woman so she can perform the enchantment."
            )

            STAGE_6_AMULET_ENCHANTED -> listOf(
                "I’ve given the old woman the required items.",
                "She has now enchanted my Amulet of Ghostspeak!"
            )

            STAGE_7_COMMAND_NECROVARUS -> listOf(
                "With my newly enchanted amulet, I commanded Necrovarus to lift his ban.",
                "The ghosts of Port Phasmatys are finally free to move on."
            )

            STAGE_8_TELL_VELORINA -> listOf(
                "I should return to Velorina and let her know that Necrovarus has lifted his ban."
            )

            STAGE_9_QUEST_COMPLETE -> listOf(
                "I told Velorina the good news!",
                "She rewarded me with an Ectophial, which teleports me to the temple.",
                "Quest complete!"
            )

            else -> listOf("Invalid quest stage. Please report this to an administrator.")
        }
    }

    override fun complete(player: Player) {
        player.setQuestStage(Quest.GHOSTS_AHOY, STAGE_9_QUEST_COMPLETE)
        player.inventory.addItem(Ectophial)
        player.skills.addXp(Skills.PRAYER, 2400.0)
        player.vars.saveVar(217, 5)
        sendQuestCompleteInterface(player, Ectophial)
    }

    override fun updateStage(player: Player, stage: Int) {
        if(player.hasRights(Rights.ADMIN))
        player.sendMessage("Current stage: $stage")
        if (stage == STAGE_5_GATHER_ITEMS) {
            player.vars.saveVarBit(217, stage)
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
            player.vars.saveVarBit(217, 5)
        else if (player.getQuestStage(Quest.GHOSTS_AHOY) >= GhostsAhoy.STAGE_5_GATHER_ITEMS)
            player.vars.saveVarBit(217, 5)
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
