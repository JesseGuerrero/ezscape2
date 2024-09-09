package com.rs.rsps.ChristianRSPS;

import com.rs.engine.cutscene.Cutscene;
import com.rs.engine.pathfinder.Direction;
import com.rs.game.World;
import com.rs.game.model.entity.Entity;
import com.rs.game.model.entity.player.Player;
import com.rs.lib.game.Tile;
import com.rs.lib.util.Utils;

public class Sermon extends Cutscene {
    Tile start = ChurchService.serviceTile;
    String[] sermon = ChurchService.sermons[Utils.random(ChurchService.sermons.length)];
    String[] praises = {"Amen.", "Preach", "Praise the Lord."};
    @Override
    public void construct(Player player) {
        setEndTile(start);
        action(() -> {
            ChurchService.priest.forceTalk("Let us pray.");
            player.setDailyB("HasDoneSermon", true);
        });
        delay(1);
        playerMove(start.x(), start.y(), start.plane(), Entity.MoveType.WALK);
        delay(2);
        action(() -> { ChurchService.priest.forceTalk("Father in heaven...");});
        delay(3);
        action(() -> { ChurchService.priest.forceTalk("Grant me the wisdom to preach...");});
        delay(4);
        action(() -> { ChurchService.priest.forceTalk("Forgive us our sins, Christ.");});
        playerFaceDir(Direction.SOUTH);
        delay(4);
        action(() -> { ChurchService.priest.forceTalk("Have mercy on us..");});
        delay(3);
        action(() -> { ChurchService.priest.forceTalk("Amen...");});
        delay(2);
        action(() -> { ChurchService.npcs.forEach(npc -> {
                npc.forceTalk("Amen."); player.forceTalk("Amen.");
                npc.anim(3114); player.anim(3114);
        }); } );
        delay(1);
        for(String sentence : sermon) {
            action(() -> { ChurchService.priest.forceTalk(sentence); });
            delay(sentence.length() / 5 / 2);
            action(() -> {
                if(Utils.random(2) == 0)
                    ChurchService.npcs.get(Utils.random(ChurchService.npcs.size())).forceTalk(praises[Utils.random(3)]);
            });
            delay(sentence.length() / 5 / 2);
        }
        action(() -> { ChurchService.priest.forceTalk("That concludes our service...");});
        delay(4);
        action(() -> { ChurchService.priest.forceTalk("Let us pray in closing.");});
        delay(4);
        action(() -> { ChurchService.priest.forceTalk("The Lord bless you");});
        delay(3);
        action(() -> { ChurchService.priest.forceTalk("and keep you.");});
        delay(3);
        action(() -> { ChurchService.priest.forceTalk("The Lord make His face.");});
        delay(4);
        action(() -> { ChurchService.priest.forceTalk("Shine upon you.");});
        delay(3);
        action(() -> { ChurchService.priest.forceTalk("And give you peace.");});
        delay(3);
        action(() -> {
            ChurchService.npcs.forEach(npc -> npc.forceTalk("Amen."));
            World.getPlayers().forEach(congregate -> {
                if(congregate.getControllerManager().getController() instanceof ChurchService) {
                    congregate.forceTalk("Amen.");
                    congregate.anim(3114);
                    congregate.sendMessage("<col=00FFFF>You have increased in knowledge of prayer...");
                    congregate.incrementCount("ServicesAttended");
                }
            });
        });
    }
}
