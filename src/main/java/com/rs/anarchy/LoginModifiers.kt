package com.rs.anarchy

import com.rs.Settings
import com.rs.engine.command.Commands
import com.rs.engine.miniquest.Miniquest
import com.rs.engine.quest.Quest
import com.rs.game.World
import com.rs.game.content.achievements.Achievement
import com.rs.game.content.tutorialisland.TutorialIslandController
import com.rs.game.model.entity.Entity
import com.rs.game.model.entity.Teleport
import com.rs.game.model.entity.actions.Action
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.actions.PlayerAction
import com.rs.game.model.entity.player.managers.InterfaceManager
import com.rs.game.tasks.WorldTasks
import com.rs.lib.game.Animation
import com.rs.lib.game.Rights
import com.rs.lib.game.SpotAnim
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemAddedToInventory
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
            controllerManager.addCanAttackHook(::anarchyCanAttackCheck)
            controllerManager.addKeepFightingHook(::anarchyKeepFightingCheck)
            controllerManager.addCanHitHook(::anarchyCanHitCheck)
            controllerManager.addDeathHook(::anarchyPvpDeathCheck)
            controllerManager.addTeleportHook(::teleportCheck)
        }
    }

    onItemAddedToInventory(24444) { updateSkull(it.player) }

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

fun updateSkull(player: Player) {
    player.tasks.schedule {
        when(player.inventory.getNumberOf(24444)) {
            0 -> player.skullId = 0
            1 -> player.skullId = 6
            2 -> player.skullId = 5
            3 -> player.skullId = 4
            4 -> player.skullId = 3
            else -> player.skullId = 2
        }
        player.appearance.generateAppearanceData()
    }
}

fun anarchyKeepFightingCheck(player: Player, target: Entity): Boolean {
    if (target is NPC) return true
    if (!anarchyCanAttackCheck(player, target)) return false
    if (target is Player && !player.attackedBy(target.username)) {
        player.setWildernessSkull()
        updateSkull(player)
    }
    return true;
}

fun anarchyCanAttackCheck(player: Player, target: Entity): Boolean {
    return anarchyCanHitCheck(player, target)
}

fun anarchyCanHitCheck(player: Player, target: Entity): Boolean {
    if (target is Player && player.isCanPvp && !target.isCanPvp) {
        player.sendMessage("That player is not in the wilderness.")
        return false
    }
    //combat level checks here
    return true
}

fun teleportCheck(player: Player, teleport: Teleport): Boolean {
    if (!player.isCanPvp)
        return true
    player.actionManager.action = object : PlayerAction() {
        override fun start(player: Player): Boolean {
            player.sync(16385, 3017)
            player.actionManager.actionDelay = 18
            return true
        }

        override fun process(player: Player): Boolean {
            if (player.hasBeenHit(1000) || player.inCombat(1000)) {
                player.sendMessage("Your teleport has been interrupted.")
                player.anim(-1)
                return false
            }
            return true
        }

        override fun processWithDelay(player: Player): Int {
            player.tele(teleport.destination)
            if (teleport.end != null)
                teleport.end.run()
            player.controllerManager.onTeleported(teleport.type)
            player.anim(-1)
            return -1
        }

        override fun stop(player: Player) {
            player.anim(-1)
        }
    }
    return false
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
            5 -> {
                player.jingle(90)
                return@scheduleTimer false
            }
        }
        return@scheduleTimer true
    }
    return true
}