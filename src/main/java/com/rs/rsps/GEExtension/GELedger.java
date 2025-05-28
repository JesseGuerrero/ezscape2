package com.rs.rsps.GEExtension;

import com.rs.engine.book.Book;
import com.rs.engine.dialogue.Dialogue;
import com.rs.game.ge.GE;
import com.rs.lib.game.Item;
import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.plugin.handlers.ItemClickHandler;
import com.rs.rsps.Power.Codex;

import java.util.Arrays;

@PluginEventHandler
public class GELedger {
    public static ItemClickHandler read = new ItemClickHandler(new Object[] { 3845 }, new String[] { "Read" }, e -> {
        if(e.getPlayer().isCanPvp()) {
            e.getPlayer().sendMessage("You can't read this while other players can attack you.");
            return;
        }
        if(e.getPlayer().inCombat()) {
            e.getPlayer().sendMessage("You can't read this while in combat.");
            return;
        }
        e.getPlayer().startConversation(new Dialogue().addOptions("Grand Exchange Ledger", ops -> {
            ops.add("Open", () -> {
                e.getPlayer().sendInputInteger("Which exchange would you like to see", number -> {
                    if(number < 0)
                        number = 0;
                    if(number > 127)
                        number = 127;
                    e.getPlayer().set("geShift", Integer.valueOf(number)*6);
                    GE.updateGE(e.getPlayer());
                    GE.updateCollectionBox(e.getPlayer());
                    GE.open(e.getPlayer());
                });
            });
            ops.add("Collect Items", () -> {
                GE.openCollection(e.getPlayer());
            });
        }));
    });
}
