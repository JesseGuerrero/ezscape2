package com.rs.anarchy

import com.rs.cache.loaders.NPCDefinitions
import com.rs.game.World
import com.rs.game.content.skills.magic.LodestoneAction.Lodestone
import com.rs.game.model.entity.Entity
import com.rs.game.model.entity.pathing.Direction
import com.rs.game.model.entity.player.Player
import com.rs.lib.game.SpotAnim
import com.rs.lib.game.Tile
import com.rs.lib.util.Utils
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onChunkEnter
import com.rs.tools.MapSearcher
import com.rs.utils.spawns.NPCSpawns
import it.unimi.dsi.fastutil.ints.IntOpenHashSet

val safeChunks = IntOpenHashSet()

@ServerStartupEvent
fun mapChunkChanges() {
    MapSearcher.getObjects {
        arrayOf(
            "Bank booth",
            "Bank",
            "Bank chest",
            "Bank table",
            "Counter",
            "Shantay chest",
            "Darkmeyer Treasury"
        ).contains(it.name) && (it.containsOption("Bank") || it.containsOption("Use"))
    }.forEach {
        safeChunks.addAll(World.getChunkRadius(it.tile.chunkId, 1))
    }

    NPCSpawns.getAllSpawns().forEach {
        if (it.defs.hasOption("Bank"))
            safeChunks.addAll(World.getChunkRadius(it.tile.chunkId, 1))
    }

    Lodestone.entries.forEach { safeChunks.addAll(World.getChunkRadius(it.tile.chunkId, 1)) }

    onChunkEnter { e ->
        if (e.player == null || !e.player.hasStarted())
            return@onChunkEnter
        checkZone(e.player, e.chunkId)
    }
}

fun checkZone(player: Player, chunkId: Int, login: Boolean = false) {
    if (player.isHasNearbyInstancedChunks) {
        player.isCanPvp = false
        updateUI(player)
        return
    }

    //Safe chunks
    if (safeChunks.contains(chunkId)) {
        if (player.hasSkull()) {
            player.tele(Tile.of(player.lastTile))
            player.temporaryMoveType = if (Utils.getDistance(player.tile, player.lastTile) > 4) Entity.MoveType.TELE else Entity.MoveType.WALK
            World.sendSpotAnim(Tile.of(player.tile), SpotAnim(654, 50, 0, Direction.getDirectionTo(player, player.lastTile)?.id ?: 0))
            player.sendMessage("A magical force prevents you from entering a safe zone with your skull.")
            return
        }
        if (login) {
            player.isCanPvp = false
            updateUI(player)
            return
        }
        if (player.isCanPvp && !player.tasks.hasTask("anarchySafeTimer")) {
            if (!player.inCombat(5000)) {
                player.isCanPvp = false
                updateUI(player)
                return
            }
            player.tasks.scheduleTimer("anarchySafeTimer") { tick ->
                if (tick >= 20) {
                    player.isCanPvp = false
                    updateUI(player)
                    return@scheduleTimer false
                }
                updateUI(player, tick)
                return@scheduleTimer true
            }
        }
        return
    }

    //Dangerous chunks
    if (!player.isCanPvp || player.tasks.hasTask("anarchySafeTimer")) {
        if (login) {
            player.isCanPvp = false
            player.tasks.scheduleTimer("anarchyDangerTimer") { tick ->
                if (tick >= 6) {
                    player.isCanPvp = true
                    updateUI(player)
                    return@scheduleTimer false
                }
                player.spotAnim(2000)
                updateUI(player, tick)
                return@scheduleTimer true
            }
            return
        }
        player.tasks.remove("anarchySafeTimer")
        player.isCanPvp = true
        updateUI(player)
    }
}

fun updateUI(player: Player, timer: Int = -1) {
    if (timer > -1) {
        player.packets.setIFHidden(745, 3, true)
        player.packets.setIFHidden(745, 6, true)
        player.packets.setIFHidden(745, 4, false)
        player.packets.setIFHidden(745, 5, false)
        player.packets.setIFText(745, 5, "${((if (player.tasks.hasTask("anarchySafeTimer")) 20 else 6))-timer}")
        return
    }
    player.packets.setIFHidden(745, 4, true)
    player.packets.setIFHidden(745, 5, true)
    player.packets.setIFHidden(745, 3, player.isCanPvp)
    player.packets.setIFHidden(745, 6, !player.isCanPvp)
    updateSkull(player)
}