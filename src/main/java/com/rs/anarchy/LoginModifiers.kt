package com.rs.anarchy

import com.rs.Settings
import com.rs.engine.command.Commands
import com.rs.engine.miniquest.Miniquest
import com.rs.engine.quest.Quest
import com.rs.game.World
import com.rs.game.content.achievements.Achievement
import com.rs.game.content.tutorialisland.TutorialIslandController
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.managers.InterfaceManager
import com.rs.game.tasks.WorldTasks
import com.rs.lib.game.Rights
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onLogin
import java.util.*

@ServerStartupEvent
fun mapLoginModifiers() {
    onLogin {
        it.player.apply {
            checkZone(this, this.chunkId, true)
            if (controllerManager.isIn(TutorialIslandController::class.java)) {
                setIronMan(false)
                isChosenAccountType = true
                World.sendWorldMessage("<img=5><col=FF0000>" + displayName + " has just joined " + Settings.getConfig().serverName + "!</col>", false)
                tele(Tile.of(Settings.getConfig().playerStartTile))
                controllerManager.forceStop()
                interfaceManager.flashTabOff()
                interfaceManager.sendSubDefaults(*InterfaceManager.Sub.ALL_GAME_TABS)
                giveStarter()
                interfaceManager.sendAchievementComplete(Achievement.THE_JOURNEY_BEGINS_3521)
                appearance.generateAppearanceData()
            }
            controllerManager.addDeathHook(::anarchyPvpDeathCheck)
        }
    }

    Commands.add(Rights.PLAYER, "completequest [questName]", "Completes the specified quest.") { p, args ->
        for (quest in Quest.entries)
            if (quest.name.lowercase(Locale.getDefault()).contains(args[0]!!)) {
                p.questManager.completeQuest(quest)
                p.sendMessage("Completed quest: " + quest.name)
                return@add
            }
        for (quest in Miniquest.entries)
            if (quest.name.lowercase(Locale.getDefault()).contains(args[0]!!)) {
                p.miniquestManager.complete(quest)
                p.sendMessage("Completed miniquest: " + quest.name)
                return@add
            }
    }

    Commands.add(Rights.PLAYER, "item,spawn [itemId (amount)]", "Spawns an item with specified id and amount.") { p, args ->
        val itemId = args[0].toInt()
        if (arrayOf(5733, 25349, 25357).contains(itemId)) {
            p.sendMessage("You can't spawn that item.")
            return@add
        }
        p.inventory.addItem(args[0].toInt(), if (args.size >= 2) args[1].toInt() else 1)
        p.stopAll()
    }
}

fun anarchyPvpDeathCheck(player: Player): Boolean {
    val killer = player.mostDamageReceivedSourcePlayer
    if (killer == null || killer == player)
        return false
    player.lock(8)
    player.stopAll()
    WorldTasks.scheduleTimer { loop ->
        when (loop) {
            0 -> player.anim(836)
            1 -> player.sendMessage("Oh dear, you have died.")
            4 -> {
                killer.removeDamage(player)
                killer.increaseKillCount(player)
                player.sendPVPItemsOnDeath(killer)
                player.equipment.init()
                player.inventory.init()
                player.reset()
                player.tele(Settings.getConfig().playerRespawnTile)
                player.anim(-1)
            }
            4 -> {
                player.jingle(90)
                return@scheduleTimer false
            }
        }
        return@scheduleTimer true
    }
    return true
}