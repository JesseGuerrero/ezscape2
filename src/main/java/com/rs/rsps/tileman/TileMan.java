package com.rs.rsps.tileman;

import com.rs.game.model.entity.player.Player;
import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.plugin.handlers.LoginHandler;
import com.rs.plugin.handlers.XPGainHandler;
import com.rs.utils.Ticks;

@PluginEventHandler
public class TileMan {
    public static LoginHandler onLoginTileMan = new LoginHandler(e -> {
        e.getPlayer().setTileMan(true);
        e.getPlayer().setTileUsage(false);
        e.getPlayer().sendMessage("Click support and home above to toggle tiles.");
        e.getPlayer().updateTileManQP();
        e.getPlayer().addToolbeltTileMan(2347);
        e.getPlayer().addToolbeltTileMan(8794);
    });

    public static void addAccordingToLevel(Player player, int diff) {
        player.addTilesAvailableFor(diff, "level up");
    }

    public static XPGainHandler onXPGain = new XPGainHandler(e -> {
        if(e.getPlayer().getSkills().getTotalXp() > e.getPlayer().getPreviousXP()) {
            e.getPlayer().addTilesAvailableFor(1, "XP gain");
            e.getPlayer().addPreviousXP();
        }
    });
}
