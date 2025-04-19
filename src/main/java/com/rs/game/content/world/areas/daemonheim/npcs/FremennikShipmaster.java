package com.rs.game.content.world.areas.daemonheim.npcs;

import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.plugin.handlers.NPCClickHandler;

@PluginEventHandler
public class FremennikShipmaster {
    public static NPCClickHandler FremennikShipmasterSail = new NPCClickHandler(new Object[]{ 9707, 9708, 14847 }, e -> {
        int npcId = e.getNPCId();
        boolean backing = npcId == 9707;
        switch (e.getOption()) {
            case "Talk-to" -> e.getPlayer().startConversation(new com.rs.game.content.world.areas.rellekka.npcs.FremennikShipmaster(e.getPlayer(), npcId, backing));
            case "Sail" -> com.rs.game.content.world.areas.rellekka.npcs.FremennikShipmaster.sail(e.getPlayer(), backing);
        }
    });
}
