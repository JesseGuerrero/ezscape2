package com.rs.game.content.items

import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onItemOnItem

enum class Dye(val id: Int) {
    RED(1763),
    YELLOW(1765),
    BLUE(1767),
    ORANGE(1769),
    PURPLE(1773),
    GREEN(1771);

    companion object {
        fun fromId(id: Int): Dye? {
            return entries.find { it.id == id }
        }
    }
}

@ServerStartupEvent
fun mapDyeMixing() {
    val dyeCombinations = mapOf(
        setOf(Dye.RED.id, Dye.YELLOW.id) to Dye.ORANGE.id,
        setOf(Dye.BLUE.id, Dye.YELLOW.id) to Dye.GREEN.id,
        setOf(Dye.BLUE.id, Dye.RED.id) to Dye.PURPLE.id
    )
    onItemOnItem(*Dye.entries.map { it.id }.toIntArray()) { e ->
        val result = dyeCombinations[setOf(e.item1.id, e.item2.id)]
        if (result != null) {
            e.player.inventory.deleteItem(e.item1.id, 1)
            e.player.inventory.deleteItem(e.item2.id, 1)
            e.player.inventory.addItem(result)
        }
    }
}

