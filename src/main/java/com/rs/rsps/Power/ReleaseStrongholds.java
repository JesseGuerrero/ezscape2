package com.rs.rsps.Power;

import com.rs.engine.cutscene.Cutscene;
import com.rs.game.World;
import com.rs.game.content.bosses.godwars.GodwarsController;
import com.rs.game.model.entity.Entity;
import com.rs.game.model.entity.player.Player;
import com.rs.lib.game.Tile;
import com.rs.lib.net.packets.encoders.Sound;

public class ReleaseStrongholds extends Cutscene {
    Tile bandosTile = Tile.of(2870, 5356, 2);
    Tile corpTile = Tile.of(2983, 4385, 2);
    Tile kbdTile = Tile.of(2274, 4698, 0);
    Tile startTile;

    @Override
    public void construct(Player player) {
        startTile = player.getTile();
        setEndTile(startTile);
        music(999);
        fadeIn(0);
        hideMinimap();
        delay(4);
        action(()-> {
            player.getAppearance().setHidden(true);
            if(player.hasFamiliar())
                player.getFamiliar().setHidden(true);
        });
        playerMove(bandosTile, Entity.MoveType.TELE);
        delay(1);
        camPos(53, -59, 5650);
        camLook(53, 6, 700);
        delay(1);
        camPos(53, -9, 5650, 3, 3);
        camLook(53, -19, 400, 3, 3);
        fadeOut(0);
        delay(2);
        action(() -> {World.soundEffect(bandosTile, 1341);});
        delay(1);
        action(() -> {World.soundEffect(bandosTile, 1341);});
        delay(1);
        action(() -> {World.soundEffect(bandosTile, 1341);});
        delay(1);
        action(() -> {
            World.getPlayers().forEach((otherPlayer) -> {
                if(!otherPlayer.getCutsceneManager().hasCutscene() && otherPlayer.getControllerManager().getController() instanceof GodwarsController && otherPlayer.withinDistance(player, 18))
                    otherPlayer.forceTalk("What was that... Someone is watching...");
            });
        });
        delay(3);

        //Corp
        fadeIn(0);
        delay(4);
        playerMove(corpTile, Entity.MoveType.TELE);
        delay(1);
        camPos(-115, 33, 5000);
        camLook(-80, 30, 700);
        delay(1);
        camPos(-105, 33, 5000, 3, 3);
        camLook(-80, 30, 700);
        fadeOut(0);
        delay(2);
        action(() -> {World.soundEffect(corpTile, 1341);});
        delay(1);
        action(() -> {World.soundEffect(corpTile, 1341);});
        delay(1);
        action(() -> {World.soundEffect(corpTile, 1341);});
        delay(1);
        action(() -> {
            World.getPlayers().forEach((otherPlayer) -> {
                if(!otherPlayer.getCutsceneManager().hasCutscene() && otherPlayer.withinDistance(player, 50))
                    otherPlayer.forceTalk("What was that... Someone is watching...");
            });
        });
        delay(3);

        //KBD
        fadeIn(0);
        delay(4);
        playerMove(kbdTile, Entity.MoveType.TELE);
        delay(1);
        camPos(-70, 90, 5000);
        camLook(20, 90, 1000);
        delay(1);
        camPos(-20, 90, 5000, 3, 3);
        camLook(20, 90, 1000, 3, 3);
        fadeOut(0);
        delay(2);
        action(() -> {World.soundEffect(kbdTile, 1341);});
        delay(1);
        action(() -> {World.soundEffect(kbdTile, 1341);});
        delay(1);
        action(() -> {
            World.soundEffect(kbdTile, 1341);
            World.getPlayers().forEach((otherPlayer) -> {
                if(!otherPlayer.getCutsceneManager().hasCutscene() && otherPlayer.withinDistance(player, 50))
                    otherPlayer.forceTalk("What was that... Someone is watching...");
            });
        });
        delay(4);
        fadeIn(0);
        delay(4);
        playerMove(startTile, Entity.MoveType.TELE);
        camPosReset();
        action(()-> {
            player.getAppearance().setHidden(false);
            if(player.hasFamiliar())
                player.getFamiliar().setHidden(false);
        });
        fadeOut(0);
        delay(2);
        hideMinimap(false);
        delay(2);
    }
}
