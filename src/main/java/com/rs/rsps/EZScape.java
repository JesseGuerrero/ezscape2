package com.rs.rsps;

import com.rs.Settings;
import com.rs.engine.pathfinder.RouteEvent;
import com.rs.engine.quest.Quest;
import com.rs.game.content.minigames.treasuretrails.TreasureTrailsManager;
import com.rs.game.content.quests.shieldofarrav.ShieldOfArrav;
import com.rs.game.content.skills.fishing.Fish;
import com.rs.game.content.skills.fishing.FishingSpot;
import com.rs.game.content.skills.mining.Ore;
import com.rs.game.content.skills.slayer.Task;
import com.rs.game.content.skills.thieving.PickPocketableNPC;
import com.rs.game.model.entity.player.Player;
import com.rs.game.model.entity.player.Skills;
import com.rs.lib.game.Item;
import com.rs.lib.game.Tile;
import com.rs.lib.util.Utils;
import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.plugin.handlers.LoginHandler;
import com.rs.plugin.handlers.ObjectClickHandler;
import com.rs.rsps.ChristianRSPS.ChurchService;
import com.rs.utils.DropSets;
import com.rs.utils.WorldUtil;
import com.rs.utils.drop.DropTable;
import java.util.Arrays;
import java.util.stream.Stream;

@PluginEventHandler
public class EZScape {
    public static LoginHandler onLogin = new LoginHandler(e -> {
        if(!e.getPlayer().getBool("XPNormalized")) {
            for(int skill = 0; skill < Skills.SIZE; skill++) {
                int max = 99;
                if(skill == 24)
                    max = 120;
                double xp = e.getPlayer().getSkills().getXp(skill);
                if(xp > Skills.getXPForLevel(max)) {
                    double diff = xp - Skills.getXPForLevel(max);
                    diff /= Settings.getConfig().getXpRate();
                    xp = Skills.getXPForLevel(max) + diff;
                }
                e.getPlayer().getSkills().setXp(skill, xp);
            }
            e.getPlayer().set("XPNormalized", true);
        }
        for (Quest quest : Quest.values())
            if (quest.isImplemented())
                if (!e.getPlayer().getQuestManager().isComplete(quest)) {
                    e.getPlayer().getQuestManager().setStage(quest, quest.getHandler().getCompletedStage());
                    e.getPlayer().getQuestManager().sendQuestStage(quest);
                    e.getPlayer().getQuestManager().sendQuestPoints();
                }
        ShieldOfArrav.setGang(e.getPlayer(), "Phoenix");
    });

    public static boolean shouldIAllowHighXPRate(int skill, int level) {
        if(skill == 24 && level >= 120)
            return false;
        if(skill != 24 && level >= 99)
            return false;
        return true;
    }

    public static boolean corpReset() {
        return false;
    }

    public static boolean removeIronMan() {
        return true;
    }

    public static Item[] tripleNPCDrops(Player killer, int id) {
        Item[] drop1 = DropTable.calculateDrops(killer, DropSets.getDropSet(id));
        Item[] drop2 = DropTable.calculateDrops(killer, DropSets.getDropSet(id));
        Item[] drop3 = DropTable.calculateDrops(killer, DropSets.getDropSet(id));
        return Stream.of(drop1, drop2, drop3)
                .flatMap(Arrays::stream)
                .toArray(Item[]::new);
    }

    public static void giveTwoExtraOre(Player player, Ore ore) {
        ore.giveOre(player);
        ore.giveOre(player);
    }

    public static boolean canEmptyInventoryPickpocket() {
        return true;
    }

    public static void giveTwoExtraFish(Player player, FishingSpot spot, Fish f) {
        f.giveFish(player, spot);
        f.giveFish(player, spot);
    }

    public static boolean giveTwoExtraLog() {
        return true;
    }

    public static boolean updateLoyalty(Player player) {
        player.loyaltyPoints += 800;
        player.setPestPoints(player.getPestPoints() + 5);
        if(Utils.randomInclusive(0, 1) == 0)
            player.soulWarsZeal+=1;
        if(Utils.randomInclusive(0, 2) == 0)
            player.scPoints += 1;
        player.incDailyI("loyaltyTicks");
        if(player.getDailyI("loyaltyTicks") % 12 == 0) {
            player.sendMessage("<col=FF0000>You have " + Utils.formatNumber(player.loyaltyPoints) + " loyalty points from play time...");
            player.sendMessage("<col=FF0000>You have " + Utils.formatNumber(player.getPestPoints()) + " pest control points from play time...");
            player.sendMessage("<col=FF0000>You have " + Utils.formatNumber(player.soulWarsZeal) + " soul wars zeal from play time...");
        }
        return false;
    }

    public static boolean isChurch(Player player) {
        return player.getControllerManager().getController() instanceof ChurchService;
    }

    public static boolean limit99PestControl(Player player,int skillId) {
        return player.getSkills().getLevelForXp(skillId) >= 99;
    }

    public static boolean limitZealUse(Player player, int skill) {
        boolean is99 = player.getSkills().getLevel(skill) >= 99;
        if(is99) {
            player.sendMessage("You can't use zeal on a 99 skill.");
            return true;
        }
        return false;
    }

    public static ObjectClickHandler handleEdge = new ObjectClickHandler(false, new Object[] { 26933, 26934 }, (e) -> {
        e.getPlayer().move(Tile.of(3096, 9868, 0));
    });
    public static ObjectClickHandler handleTower = new ObjectClickHandler(false, new Object[] { 4490,4487 }, (e) -> {
        if(e.getPlayer().getY() < 3536)
            e.getPlayer().move(Tile.of(3429, 3536, 0));
        else
            e.getPlayer().move(Tile.of(3429, 3534, 0));
    });

    public static ObjectClickHandler handleNezzyGate = new ObjectClickHandler(false, new Object[] { 21505, 21506, 21507, 21508 }, (e) -> {
        if(e.getPlayer().getX() <= 2328)
            e.getPlayer().move(Tile.of(2330, 3805, 0));
        else
            e.getPlayer().move(Tile.of(2328, 3805, 0));
    });

    public static ObjectClickHandler handleYakGate = new ObjectClickHandler(false, new Object[] { 21600, 21601 }, (e) -> {
        if(e.getPlayer().getY() >= 3802)
            e.getPlayer().move(Tile.of(2326, 3801, 0));
        else
            e.getPlayer().move(Tile.of(2326, 3802, 0));
    });

    public static boolean removeLoyaltyAuraCooldowns() {
        return true;
    }

    public static int hpBoostLengthMultiplier() {
        return 9999;
    }

    public static Item[] tenTimesPickPocket(Player player, PickPocketableNPC npcData) {
        Item[] drop1 = DropTable.calculateDrops(player, npcData.getLoot());
        Item[] drop2 = DropTable.calculateDrops(player, npcData.getLoot());
        Item[] drop3 = DropTable.calculateDrops(player, npcData.getLoot());
        Item[] drop4 = DropTable.calculateDrops(player, npcData.getLoot());
        Item[] drop5 = DropTable.calculateDrops(player, npcData.getLoot());
        Item[] drop6 = DropTable.calculateDrops(player, npcData.getLoot());
        Item[] drop7 = DropTable.calculateDrops(player, npcData.getLoot());
        Item[] drop8 = DropTable.calculateDrops(player, npcData.getLoot());
        Item[] drop9 = DropTable.calculateDrops(player, npcData.getLoot());
        Item[] drop10 = DropTable.calculateDrops(player, npcData.getLoot());
        return Stream.of(drop1, drop2, drop3, drop4, drop5, drop6, drop7, drop8, drop9, drop10)
                .flatMap(Arrays::stream)
                .toArray(Item[]::new);
    }

    public static int getFightCavesWave(Player player) {
        return player.getI("fightCavesWaveI", 1);
    }

    public static void saveFightCaveWave(Player player, int wave) {
        player.set("fightCavesWaveI", wave);
    }

    public static int getFightKilnWave(Player player) {
        return player.getI("fightKilnWaveI", 0);
    }

    public static void saveFightKilnWave(Player player, int wave) {
        player.set("fightKilnWaveI", wave);
    }

    public static int lootChestMultiplier() {
        return 10;
    }

    public static int farmMultiplier() {
        return 2;
    }

    public static int defenderDropRate() {
        return 17;
    }

    public static int multiplySlayerPointsTimesTen(int amount) {
        return amount * 10;
    }

    public static LoginHandler updateSlayerPoints = new LoginHandler(e -> {
        if(!e.getPlayer().getBool("slayerTimesTen")) {
            e.getPlayer().setSlayerPoints(e.getPlayer().getSlayerPoints()*10);
            e.getPlayer().set("slayerTimesTen", true);
        }
    });

    public static int totalXPCap() {
        return 2_000_000_000;
    }

    public static int getLargeDungPartSizeRequirement() {
        return 1;
    }

    public static double[] dungCombatLevelsByComplexity() {
        return new double[]{0.20, 0.30, 0.35, 0.45, 0.55, 0.65};
    }

    public static double getDungBossMultiplier() {
        return 0.8;
    }

    public static int dungTokenMultiplier() {
        return 50;
    }

    public static boolean openClueReward(Player player, TreasureTrailsManager manager, Item item, int level) {
        manager.openReward(level);
        player.getInventory().deleteItem(item.getId(), 1);
        return true;
    }

    public static boolean allowOffTaskMonsters() {
        return true;
    }

    public static int reduceSlayerTaskAmount(Task task) {
        return Utils.random(Math.floorDiv(task.getMin(), 3), Math.floorDiv(task.getMax(), 3));
    }

    public static boolean tripleBarrowsReward() {
        return true;
    }

    public static int multiplyRunespanPoints() {
        return 10;
    }

    public static int runecraftMultiplier() {
        return 3;
    }

    public static void saveKillCount(Player player, int[] killCount) {
        for(int i = 0; i < killCount.length; i++)
            player.set("godwars_killcount" + String.valueOf(i), killCount[i]);
    }

    public static int[] getKillCount(Player player) {
        int[] killCount = new int[5];
        for(int i = 0; i < killCount.length; i++)
            killCount[i] = player.getI("godwars_killcount" + String.valueOf(i), 0);
        return killCount;
    }

    public static boolean showKillCountPrompt() {
        return false;
    }

    public static int farmingTick() {
        return 250;
    }

}
