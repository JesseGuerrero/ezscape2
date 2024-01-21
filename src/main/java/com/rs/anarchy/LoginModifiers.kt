package com.rs.anarchy

import com.rs.Settings
import com.rs.engine.command.Commands
import com.rs.engine.miniquest.Miniquest
import com.rs.engine.quest.Quest
import com.rs.game.World
import com.rs.game.content.Effect
import com.rs.game.content.ItemConstants
import com.rs.game.content.ItemConstants.ItemDegrade
import com.rs.game.content.Potions
import com.rs.game.content.Toolbelt
import com.rs.game.content.Toolbelt.Tools
import com.rs.game.content.achievements.Achievement
import com.rs.game.content.skills.cooking.Foods
import com.rs.game.content.skills.magic.LodestoneAction.Lodestone
import com.rs.game.content.tutorialisland.TutorialIslandController
import com.rs.game.model.entity.Entity
import com.rs.game.model.entity.Teleport
import com.rs.game.model.entity.npc.NPC
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.Skills
import com.rs.game.model.entity.player.actions.PlayerAction
import com.rs.game.model.entity.player.managers.InterfaceManager.Sub
import com.rs.game.tasks.WorldTasks
import com.rs.lib.game.Item
import com.rs.lib.game.Rights
import com.rs.lib.game.Tile
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemAddedToInventory
import com.rs.plugin.kts.onLogin
import com.rs.plugin.kts.onXpDrop
import com.rs.utils.Ticks
import com.rs.utils.drop.Drop
import com.rs.utils.drop.DropSet
import com.rs.utils.drop.DropTable
import java.text.ParseException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.sqrt

@ServerStartupEvent
fun mapLoginModifiers() {
    onLogin {
        it.player.packets.sendDrawOrder(true)
        it.player.apply {
            try {
                sendMessage(
                    "Latest commit: ${
                        try {
                            ZonedDateTime.parse(
                                Settings.COMMIT_HISTORY[2].substring(8).trim(),
                                DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy Z")
                            ).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                        } catch (e: ParseException) {
                            "Error parsing date: ${e.message}"
                        }
                    } - ${Settings.COMMIT_HISTORY[3].substring(9).trim()}"
                )
            } catch(e: Throwable) {
                sendMessage("Error loading recent version")
            }
            checkZone(this, this.chunkId, true)
            if (controllerManager.isIn(TutorialIslandController::class.java)) {
                setIronMan(false)
                isChosenAccountType = true
                World.sendWorldMessage("<img=5><col=FF0000>" + displayName + " has just joined " + Settings.getConfig().serverName + "!</col>", false)
                tele(Tile.of(Settings.getConfig().playerStartTile))
                controllerManager.forceStop()
                interfaceManager.flashTabOff()
                interfaceManager.sendSubDefaults(*Sub.ALL_GAME_TABS)
                giveStarter()
                interfaceManager.sendAchievementComplete(Achievement.THE_JOURNEY_BEGINS_3521)
                appearance.generateAppearanceData()

                for (stone in Lodestone.entries) {
                    if (listOf(Lodestone.BANDIT_CAMP, Lodestone.LUNAR_ISLE).contains(stone)) continue
                    unlockLodestone(stone, null)
                }

                for (tool in Tools.entries) {
                    if (toolbelt[tool] != null) continue
                    toolbelt[tool] = 1
                }
                addToolbelt(1265)
                Toolbelt.refreshToolbelt(this)
            }
            nsv.setL("lastRandom", World.getServerTicks() + Ticks.fromHours(300))

            controllerManager.addCanAttackHook(::anarchyCanAttackCheck)
            controllerManager.addKeepFightingHook(::anarchyKeepFightingCheck)
            controllerManager.addCanHitHook(::anarchyCanHitCheck)
            controllerManager.addDeathHook(::anarchyPvpDeathCheck)
            controllerManager.addTeleportHook(::teleportCheck)
        }
    }

    val combatSkills = arrayOf(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENSE, Skills.MAGIC, Skills.RANGE, Skills.HITPOINTS)

    onXpDrop { e ->
        if (combatSkills.contains(e.skillId)) {
            val currLevel = e.player.skills.getLevelForXp(e.skillId)
            e.multiplier = when {
                currLevel < 50 -> 5.0
                currLevel < 99 -> 5.0 - (currLevel - 49) * 0.05
                else -> 1.0
            }
        }
    }

    onItemAddedToInventory(24444) {
        if (it.player.hasSkull())
            it.player.addEffect(Effect.SKULL, Ticks.fromMinutes(15).toLong())
        updateSkull(it.player)
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

fun updateSkull(player: Player) {
    player.tasks.scheduleTimer("anarchySkullUpdateCheck") { tick ->
        when(player.inventory.getNumberOf(24444)) {
            0 -> {
                player.skullId = 0
                return@scheduleTimer false
            }
            1 -> player.skullId = 6
            2 -> player.skullId = 5
            3 -> player.skullId = 4
            4 -> player.skullId = 3
            else -> player.skullId = 2
        }
        player.appearance.generateAppearanceData()
        if (tick % 5 == 0)
            player.packets.sendRunScript(2434, player.getEffectTicks(Effect.SKULL).toInt())
        return@scheduleTimer true
    }
}

fun anarchyKeepFightingCheck(player: Player, target: Entity): Boolean {
    if (target is NPC) return true
    if (!anarchyCanAttackCheck(player, target)) return false
    if (target is Player && !player.attackedBy(target.username) && !player.hasEffect(Effect.SKULL)) {
        player.addEffect(Effect.SKULL, Ticks.fromMinutes(5).toLong())
        player.skullId = 0
        updateSkull(player)
    }
    return true;
}

fun anarchyCanAttackCheck(player: Player, target: Entity): Boolean {
    return anarchyCanHitCheck(player, target)
}

fun anarchyCanHitCheck(player: Player, target: Entity): Boolean {
    if (target is Player && player.isCanPvp && !target.isCanPvp) {
        player.sendMessage("That player is not in an attackable target.")
        return false
    }
    //combat level checks here
    return true
}

fun teleportCheck(player: Player, teleport: Teleport): Boolean {
    if (player.hasSkull()) {
        player.sendMessage("You are unable to teleport with your skull!")
        return false
    }
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
            player.controllerManager.onTeleported(teleport.type)
            Teleport.checkDestinationControllers(player, teleport.destination)
            if (teleport.end != null)
                teleport.end.run()
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
    if (!player.hasSkull() && (killer == null || killer == player))
        return false
    player.lock(8)
    player.stopAll()
    WorldTasks.scheduleTimer { loop ->
        when (loop) {
            0 -> {
                player.anim(836)
                if (player.familiar != null)
                    player.familiar.sendDeath(killer)
            }
            1 -> player.sendMessage("Oh dear, you have died.")
            4 -> {
                if (killer != null) {
                    killer.removeDamage(player)
                    killer.increaseKillCount(player)
                    rollPvpDropTable(player, killer)
                } else
                    player.inventory.deleteItem(24444, 50)
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

fun rollPvpDropTable(player: Player, killer: Player) {
    fun getPlayerItems(container: Array<Item>, range: IntRange): List<Item> =
        range.mapNotNull { container[it] }.filter { it.id != -1 && it.amount != -1 }.map { Item(it.id, it.amount, it.metaData) }

    fun handleNonTradeableItem(item: Item, killer: Player, player: Player): Item {
        val deg = ItemDegrade.entries.find { it.degradedId == item.id || it.itemId == item.id }
        return when {
            deg != null && deg.brokenId != -1 -> {
                val broken = Item(deg.brokenId, item.amount)
                if (!ItemConstants.isTradeable(broken) && killer !== player) Item(995, item.definitions.getValue()) else broken
            }
            else -> Item(995, item.definitions.getValue())
        }
    }

    if (killer.username != player.username && killer.isIronMan) return

    val containedItems = CopyOnWriteArrayList<Item>().apply {
        addAll(getPlayerItems(player.equipment.itemsCopy, 0..13))
        addAll(getPlayerItems(player.inventory.items.array(), 0..27))
    }

    if (containedItems.isEmpty()) return

    val keptAmount = (if (player.hasSkull()) 0 else 3) + if (player.prayer.isProtectingItem) 1 else 0
    val keptItems = generateSequence { containedItems.maxByOrNull { it.definitions.getValue() } }.take(keptAmount).onEach { containedItems.remove(it) }.toList()
    keptItems.filter { it.id != 1 }.forEach { player.inventory.addItem(it) }

    val droppedItems = containedItems.map { item ->
        when {
            ItemConstants.isTradeable(item) || item.id == 24444 -> item
            else -> handleNonTradeableItem(item, killer, player)
        }
    }

    val lostItems = droppedItems.filterNot { Foods.isConsumable(it) || Potions.Potion.POTS.containsKey(it.id) || it.id == 24444 }

    generatePKDrop(lostItems.sumOf { it.definitions.value }).forEach {
        World.addGroundItem(it, player.lastTile, killer, true, 60)
    }
}

fun generatePKDrop(pkedPlayerDropValue: Int): List<Item> {
    val drops: MutableList<Item> = ArrayList()

    val g = Utils.clampD(sqrt(sqrt(pkedPlayerDropValue.toDouble())), 1.0, 20.0)
    var r = 60000.0 / g

    Utils.add(
        drops, DropTable.calculateDrops(
            DropSet(
                //1/R chance each to obtain an Ancient, Seren, Armadyl, Zamorak, Saradomin or Bandos statuette, or a random brawling glove. The rate for brawling gloves is 2/15 for Smithing and Hunter gloves, and 1/15 for all others
                DropTable(1.0, r, Drop(14876)),
                DropTable(1.0, r, Drop(14877)),
                DropTable(1.0, r, Drop(14878)),
                DropTable(1.0, r, Drop(14879)),
                DropTable(1.0, r, Drop(14880)),
                DropTable(1.0, r, Drop(14881)),
                DropTable(1.0, r,
                    Drop(13845),
                    Drop(13846),
                    Drop(13847),
                    Drop(13848),
                    Drop(13849),
                    Drop(13850),
                    Drop(13851),
                    Drop(13852),
                    Drop(13853),
                    Drop(13854),
                    Drop(13855),
                    Drop(13856),
                    Drop(13857),
                    Drop(13855),
                    Drop(13853)
                ),

                //2/R chance each to obtain a Ruby chalice, Guthixian brazier, Armadyl totem, Zamorak medallion, Saradomin carving, Bandos scrimshaw or a corrupt dragon item
                DropTable(2.0, r, Drop(14882)),
                DropTable(2.0, r, Drop(14883)),
                DropTable(2.0, r, Drop(14884)),
                DropTable(2.0, r, Drop(14885)),
                DropTable(2.0, r, Drop(14886)),
                DropTable(2.0, r, Drop(14887)),
                DropTable(2.0, r,
                    Drop(13958),
                    Drop(13961),
                    Drop(13964),
                    Drop(13967),
                    Drop(13970),
                    Drop(13973),
                    Drop(13976),
                    Drop(13979),
                    Drop(13982),
                    Drop(13985),
                    Drop(13988)
                ),

                //3/R chance each to obtain a Saradomin amphora, Ancient psaltery bridge, Bronzed dragon claw, Third age carafe or broken statue headdress
                DropTable(3.0, r, Drop(14888)),
                DropTable(3.0, r, Drop(14889)),
                DropTable(3.0, r, Drop(14890)),
                DropTable(3.0, r, Drop(14891)),
                DropTable(3.0, r, Drop(14892)),

                //10/R chance each to obtain a piece of Ancient Warriors' equipment or its corrupt version
                DropTable(10.0, r,
                    Drop(13858),
                    Drop(13861),
                    Drop(13864),
                    Drop(13867),
                    Drop(13870),
                    Drop(13873),
                    Drop(13876),
                    Drop(13879, 15, 50),
                    Drop(13883, 15, 50),
                    Drop(13884),
                    Drop(13887),
                    Drop(13890),
                    Drop(13893),
                    Drop(13896),
                    Drop(13899),
                    Drop(13902),
                    Drop(13905)
                ),
                DropTable(10.0, r,
                    Drop(13908),
                    Drop(13911),
                    Drop(13914),
                    Drop(13917),
                    Drop(13920),
                    Drop(13923),
                    Drop(13926),
                    Drop(13929),
                    Drop(13932),
                    Drop(13935),
                    Drop(13938),
                    Drop(13941),
                    Drop(13944),
                    Drop(13947),
                    Drop(13950),
                    Drop(13953, 15, 50),
                    Drop(13957, 15, 50)
                ),

                //5/R chance to get an ultra rare lucky
                DropTable(5.0, r, *(23679..23700).map { Drop(it) }.toTypedArray())
            )
        )
    )

    //If no unique is dropped, then a final roll for coins appear, the cap roughly equal to 50 times G.
    if (drops.isEmpty()) drops.add(Item(995, (200.0 * g).toInt()))
    return drops
}