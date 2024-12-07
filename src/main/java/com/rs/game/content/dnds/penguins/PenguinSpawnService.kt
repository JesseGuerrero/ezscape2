package com.rs.game.content.dnds.penguins

import com.rs.db.WorldDB
import com.rs.engine.pathfinder.Direction
import com.rs.game.World
import com.rs.game.content.dnds.penguins.PenguinServices.penguinSpawnService
import com.rs.game.map.ChunkManager
import com.rs.game.model.entity.npc.NPC
import com.rs.lib.game.Tile
import com.rs.lib.util.Logger
import com.rs.utils.spawns.NPCSpawn
import java.time.Month

const val PUMPKIN_ID = 14415
const val SNOWMAN_ID = 14766

class PenguinSpawnService () {
    val spawnedNPCs: MutableMap<String, NPC> = mutableMapOf()
    var regionIds: HashSet<Int> = hashSetOf()
    private val allSpawns = mutableMapOf<NPCSpawn, Int>()

    fun loadSpawns() {
        allSpawns.clear()
        val penguins = WorldDB.getPenguinHAS().getAllPenguins()
        penguins.forEach { penguin ->
            val spawn = NPCSpawn(penguin.npcId, penguin.location, penguin.wikiLocation)
            allSpawns[spawn] = penguin.week
            regionIds.add(penguin.location.regionId)
        }
        Logger.debug(PenguinSpawnService::class.java, "loadSpawns", "Loaded ${allSpawns.size} spawns from database.")
    }

    fun addSpawn(id: Int, tile: Tile, comment: String, week: Int): NPCSpawn {
        val spawn = NPCSpawn(id, tile, comment)
        allSpawns[spawn] = week
        regionIds.add(tile.regionId)
        return spawn
    }

    fun getAllSpawns(): Map<NPCSpawn, Int> = allSpawns

    fun getSpawnsForWeek(week: Int): List<NPCSpawn> {
        return getAllSpawns().filter { (_, spawnWeek) -> spawnWeek == week }.map { (spawn, _) -> spawn }
    }

    fun isSpawnEmpty(): Boolean = getAllSpawns().isEmpty()

    fun prepareNew(week: Int) {
        var previouslySpawnedPenguins = getAllSpawns()
            .filter { (_, spawnWeek) -> spawnWeek == (week - 1) }
            .map { (spawn, _) -> spawn }

        removeAllSpawns()

        for (player in World.players) { // Reset Quickchat varbit to 0 for all logged in players
            player.vars.saveVarBit(5276, 0)
        }

        val currentMonth = PenguinServices.penguinHideAndSeekManager.getCurrentMonth()
        val usedLocationHints = mutableSetOf<String>()

        val onePointPenguins = Penguins.getPenguinsByPoints(1)
            .filterNot { it.locationHint in usedLocationHints || previouslySpawnedPenguins.any { spawn -> spawn.tile == it.tile } }
            .take(5)
        onePointPenguins.forEach { usedLocationHints.add(it.locationHint) }
        val twoPointPenguins = Penguins.getPenguinsByPoints(2)
            .filterNot { it.locationHint in usedLocationHints || previouslySpawnedPenguins.any { spawn -> spawn.tile == it.tile } }
            .take(5)
        twoPointPenguins.forEach { usedLocationHints.add(it.locationHint) }

        val selectedPenguins = (onePointPenguins + twoPointPenguins).toMutableList()
        val uniquePenguins = selectedPenguins.distinctBy { it.locationHint }.toMutableList()

        if (uniquePenguins.size < 10) {
            fun addReplacementPenguins(points: Int, targetList: MutableList<Penguins>, requiredCount: Int) {
                val needed = requiredCount - targetList.count { it.points == points }
                if (needed > 0) {
                    Penguins.getPenguinsByPoints(points).filterNot { it.locationHint in usedLocationHints || it in targetList || previouslySpawnedPenguins.any { spawn -> spawn.tile == it.tile } }
                        .take(needed)
                        .forEach { penguin ->
                            if (penguin.locationHint !in usedLocationHints) {
                                targetList += penguin
                                usedLocationHints.add(penguin.locationHint)
                            }
                        }
                }
            }

            addReplacementPenguins(1, uniquePenguins, 5)
            addReplacementPenguins(2, uniquePenguins, 5)
        }

        uniquePenguins.forEach { penguin ->
            usedLocationHints.add(penguin.locationHint)
            val idToUse = when (currentMonth) {
                Month.OCTOBER -> PUMPKIN_ID
                Month.DECEMBER -> SNOWMAN_ID
                else -> penguin.npcId
            }
            val newPenguin = WorldDB.getPenguinHAS().createPenguin(idToUse, penguin.name, null, week, penguin.points, penguin.tile, penguin.wikiLocation, penguin.locationHint)
            val spawn = addSpawn(idToUse, newPenguin.location, penguin.wikiLocation, week)
            spawnPenguins(week, spawn)
        }
        Logger.debug(PenguinSpawnService::class.java, "prepareNew", "New penguins spawned for week $week. Current tracked size: ${spawnedNPCs.size}")
    }

    fun prepareExisting(currentWeek: Int) {
        val nonCurrentWeekSpawns = getAllSpawns()
        val alreadySpawnedTiles = spawnedNPCs.values.map { it.respawnTile }.toSet()

        nonCurrentWeekSpawns.forEach { spawn ->
            if (spawn.key.tile in alreadySpawnedTiles) {
                return@forEach
            }
            spawnPenguins(currentWeek, spawn.key)
        }
    }

    fun spawnPenguins(week: Int, spawn: NPCSpawn) {
        if (!regionIds.contains(spawn.tile.regionId))
            regionIds.add(spawn.tile.regionId)
        ChunkManager.permanentlyPreloadRegions(penguinSpawnService.regionIds)
        val npc = spawn.spawnAtCoords(spawn.tile, Direction.random())
        trackSpawnedNPC(week, npc.respawnTile, npc)
    }

    fun trackSpawnedNPC(week: Int, tile: Tile, npc: NPC) {
        val key = "$week:${tile.x}:${tile.y}:${tile.plane}"
        spawnedNPCs[key] = npc
        playSoundAndAnim(86, npc)
    }

    fun removeAllSpawns(): Boolean {
        val allSpawns = getAllSpawns() as? MutableMap<NPCSpawn, Int>

        val removed = allSpawns?.isNotEmpty() == true
        allSpawns?.clear()
        WorldDB.getPenguinHAS().clearAllPenguins()

        if (removed) {
            spawnedNPCs.values.forEach { npc ->
                playSoundAndAnim(1605, npc)
                npc.finish()
            }
            spawnedNPCs.clear()
            ChunkManager.removePermanentlyPreloadedRegions(regionIds)
            regionIds.clear()
            Logger.debug(PenguinSpawnService::class.java, "removeAllSpawns", "All old penguin spawns removed.")
        }

        return removed
    }

    private fun playSoundAndAnim(spotAnimId: Int, npc: NPC) {
        val soundId = 1930
        val actualSpotAnimId = spotAnimId
        npc.soundEffect(npc, soundId, true)
        World.sendSpotAnim(npc.tile, actualSpotAnimId)
    }
}
