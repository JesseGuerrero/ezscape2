package com.rs.rsps.tileman;

import com.rs.Settings;
import com.rs.engine.cutscene.Cutscene;
import com.rs.game.World;
import com.rs.game.content.achievements.Achievement;
import com.rs.game.content.skills.magic.LodestoneAction.Lodestone;
import com.rs.game.model.entity.player.Player;
import com.rs.game.model.entity.player.managers.InterfaceManager;
import com.rs.lib.game.Animation;
import com.rs.lib.game.SpotAnim;

public class StartTilemanCutscene extends Cutscene {
    private Lodestone loadstone;
    public StartTilemanCutscene(Lodestone loadstone) {
        this.loadstone = loadstone;
    }

    @Override
    public void construct(Player player) {
        setEndTile(loadstone.getTile().transform(0, -1, 0));
         action(1, () -> {
             player.getControllerManager().forceStop();
             player.abortDialogue();
             player.getAppearance().transformIntoNPC(266);
             player.tele(loadstone.getTile());
             player.setNextFaceTile(loadstone.getTile().transform(0, -1, 0));
         });
        action(4, () -> {
            player.getAppearance().transformIntoNPC(-1);
            player.setNextAnimation(new Animation(16385+1));
            player.setNextSpotAnim(new SpotAnim(3017+1));
        });
        action(2, () -> {
            player.setNextAnimation(new Animation(16393));
        });

         action(1, () ->{
             World.sendWorldMessage("<img=5><col=FF0000>" + player.getDisplayName() + " has just joined "+
                     Settings.getConfig().getServerName()+"!</col>", false);
             player.getInterfaceManager().flashTabOff();
             player.getInterfaceManager().sendSubDefaults(InterfaceManager.Sub.ALL_GAME_TABS);
             player.getInterfaceManager().sendAchievementComplete(Achievement.THE_JOURNEY_BEGINS_3521);
             player.giveStarter();
             player.tele(loadstone.getTile().transform(0, -1, 0));
             player.setNextAnimation(new Animation(-1));
             player.setNextSpotAnim(new SpotAnim(-1));
             player.setTileUsage(true);
             player.setChosenAccountType(true);
         });


    }
}
