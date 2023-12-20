package com.rs.anarchy

import com.rs.Settings
import com.rs.engine.command.Commands
import com.rs.game.World
import com.rs.game.content.achievements.Achievement
import com.rs.game.content.tutorialisland.TutorialIslandController
import com.rs.game.model.entity.player.managers.InterfaceManager
import com.rs.lib.game.Rights
import com.rs.lib.game.Tile
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onLogin

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
        }
    }

    Commands.add(Rights.DEVELOPER, "overlay", "") { p, args ->
        p.interfaceManager.sendOverlay(args[0].toInt())
    }
}