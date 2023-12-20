package com.rs.anarchy

import com.rs.engine.command.Commands
import com.rs.lib.game.Rights
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onLogin

@ServerStartupEvent
fun mapLoginModifiers() {
    onLogin {
        it.player.apply {
            checkZone(this, this.chunkId, true)
        }
    }

    Commands.add(Rights.DEVELOPER, "overlay", "") { p, args ->
        p.interfaceManager.sendOverlay(args[0].toInt())
    }
}