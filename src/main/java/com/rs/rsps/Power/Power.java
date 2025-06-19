package com.rs.rsps.Power;

import com.rs.Settings;
import com.rs.cache.loaders.Bonus;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.content.ItemConstants;
import com.rs.game.content.combat.CombatFormulaKt;
import com.rs.game.content.combat.CombatMod;
import com.rs.game.model.entity.npc.NPC;
import com.rs.game.model.entity.player.Equipment;
import com.rs.game.model.entity.player.Player;
import com.rs.game.model.entity.player.Skills;
import com.rs.lib.game.Item;
import com.rs.lib.util.GenericAttribMap;
import com.rs.lib.util.Utils;
import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.plugin.annotations.ServerStartupEvent;
import com.rs.plugin.handlers.ItemOnItemHandler;
import com.rs.plugin.handlers.LoginHandler;
import com.rs.utils.Ticks;

import java.util.ArrayList;
import java.util.Arrays;

@PluginEventHandler
public class Power {
    protected static int consumeIncrease = 50;
    protected static double denom = 5_000.0;
    protected static double foodDenom = 2_100.0;
    protected static double defMul = 1;


    private static boolean lock3Seconds(Player player, String key) {
        GenericAttribMap temp = player.getTempAttribs();
        if(temp.getB(key))
            return true;
        temp.setB(key, true);
        player.getTasks().schedule(Ticks.fromSeconds(3), () -> temp.setB(key, false));
        return false;
    }

    public static int multiplyBossKillsDomTower() {
        return 10;
    }

    public static int skullTimer() {
        return 3;
    }

    private static double powerEquation(double count) {
        return 1 + (count/denom);
    }

    private static double bonusEquation(double bonus, double count) {
        return bonus * (((powerEquation(count) - 1) * defMul) + 1);
    }

    public static void incrementItem(Player player, int itemId, int count) {
        switch(itemId) {
            case 11283, 11284 -> { incrementAll(player, new int[]{11283, 11284}, count); } //Dragonfire Shield
            case 6733, 15019 -> { incrementAll(player, new int[]{6733, 15019}, count); } //Archers Ring
            case 6735, 15020 -> { incrementAll(player, new int[]{6735, 15020}, count); } //Warrior Ring
            case 6737, 15220 -> { incrementAll(player, new int[]{6737, 15220}, count); } //Berzerker Ring
            default -> { incrementAll(player, new int[]{itemId}, count); }
        }

    }

    public static int setInfCapDamage() {
        return 9_999_999;
    }

    private static void incrementAll(Player player, int[] itemIds, int count) {
        for(int itemId : itemIds) {
            int slot = ItemDefinitions.getDefs(itemId).getEquipSlot();
            if(slot == Equipment.WEAPON)
                player.incrementCount("PowerWeaponId_" + itemId, count);
            else
                player.incrementCount(armourString(slot, itemId), count);
        }
    }


    public static void incrementPower(Player player) {
        if(lock3Seconds(player, "IncrementedPower")) //1/40 chance every 3 seconds
            return;
        int mul = 1;
        int gloveId = player.getEquipment().getGlovesId();
        if(player.getEquipment().getWeaponId() == -1 && gloveId != 22358 && gloveId != 22366 && gloveId != 22362)
            mul *= 5;
        if(player.getEquipment().getWeaponId() == 10034)
            mul *= 2;
        if(Utils.random(40) < 1 * mul || Settings.getConfig().isDebug()) {//1/40 to continue
            String weaponId = String.valueOf(player.getEquipment().getWeaponId());
            if((gloveId == 22358 || gloveId == 22366 || gloveId == 22362) && player.getEquipment().getWeaponId() == -1)
                weaponId = String.valueOf(gloveId);
            player.incrementCount("PowerWeaponId_" + weaponId);
            double newPowerPercentage = (((double) player.getCounterValue("PowerWeaponId_" + weaponId)) / denom) * 100.0;
            String formattedPower = String.format("%.2f", newPowerPercentage);
            player.sendMessage("<col=00FF00>Your power with this weapon has increased to " + formattedPower + "%...");
        }
    }

    public static void incrementPowerCooking(Player player, int foodId) {
        if(lock3Seconds(player, "IncrementedPower")) //1/40 chance every 3 seconds
            return;
        int mul = 1;
        if(player.getDungManager().isInsideDungeon())
            mul *= 10;
        if(Utils.random(25) < 1 * mul || Settings.getConfig().isDebug()) {//1/40 to continue
            player.incrementCount("FoodId_" + foodId);
            double newPowerPercentage = (((double) player.getCounterValue("FoodId_" + foodId)) / foodDenom) * 100.0;
            String formattedPower = String.format("%.2f", newPowerPercentage);
            player.sendMessage("<col=00FF00>Your health increase with this food has increased to " + formattedPower + "%...");
        }
    }

    public static void incrementAlchemy(Player player, int coins) {
        player.set("AlchemyCoins", player.getI("AlchemyCoins", 0) + coins);
    }

    public static int addCoins(Player player) {
        return player.getCounterValue("Codex_Alchemy") + player.getI("AlchemyCoins", 0) / 200_000;
    }


    public static double getFoodAmplificationPercentage(Player player, int foodId) { // 10% extra bonus -> 10.0
        return (((double) player.getCounterValue("FoodId_" + foodId)) / foodDenom) * 100.0;
    }
    public static int foodAmplification(Player player, int heal, int foodId) {
        return (int)((double)heal * (1 + (Power.getFoodAmplificationPercentage(player, foodId) / 100.0)));
    }

    public static int noLimitOnRunDrain() {
        return 1000;
    }

    public static double ampedWeight(Player player, int itemId, double weight) {
        if(weight <= 0)
            return weight;
        double percentageIncrease = getCombatAmplificationPercentage(player, itemId);
        return weight + percentageIncrease;
    }
    public static boolean limitRunTop() {
        return true;
    }

    public static double ampedHPByPower(Player player, Item item, double hpIncrease) {
        switch(item.getId()) { //Godsword, glaiven, steadfast, ragefire, fury -> helmet level | zaryte, spirit sheilds, royal Crossbow -> robe level | Visage, dag rings, GWD Amulets, kiln -> Shoe
            case 6585, 21787, 21790, 21793, 11694, 11696, 11698, 11700, 20135, 20137, 20147, 20149, 20159, 20161 -> hpIncrease += Power.itemBonus(player, Equipment.getItemSlot(item.getId()), item.getId(), 66);
            case 20139, 20141, 20151, 20153, 20163, 20165 -> hpIncrease += Power.itemBonus(player, Equipment.getItemSlot(item.getId()), item.getId(), 200);
            case 24338, 13738, 137440, 13742, 13744, 20171, 20173, 20143, 20145, 20155, 20157, 20167, 20169 -> hpIncrease += Power.itemBonus(player, Equipment.getItemSlot(item.getId()), item.getId(), 134);
            case 23659, 6733, 6737, 6735, 6731, 11284, 24974, 24975, 24977, 24978, 24980, 24981, 24983, 24984, 24986, 24987, 24989, 24990, 25058, 25060, 25062, 25064, 25066, 25068, 25031, 25034, 25028 -> hpIncrease += Power.itemBonus(player, Equipment.getItemSlot(item.getId()), item.getId(), 25);

            //GWD
            case 25022, 25019, 11718, 25013, 24992, 25001, 11730 -> hpIncrease += Power.itemBonus(player, item.getEquipId(), item.getId(), (int)(66/5)); //Shields, helmets
            case 11724, 11720, 24995 -> hpIncrease += Power.itemBonus(player, Equipment.getItemSlot(item.getId()), item.getId(), (int)(200/5));  // Tops
            case 11726, 11722, 25037, 24998, 11716 -> hpIncrease += Power.itemBonus(player, Equipment.getItemSlot(item.getId()), item.getId(), (int)(134/5)); // Robes, Armadyl Crossbow, Zammy Spear
            case 11728, 25025, 25010, 25016, 25004, 25007 -> hpIncrease += Power.itemBonus(player, Equipment.getItemSlot(item.getId()), item.getId(), (int)(25/5)); //Shoes, amulets, gloves
            case //Barrows
                    4856, 4862,	4868, 4874,
                    //Dharok
                    4880, 4886, 4892, 4898,
                    //Guthan
                    4904, 4910,	4916, 4922,
                    //Karil
                    4928, 4934, 4940, 4946,
                    //Torag
                    4952, 4958, 4964, 4970,
                    //Verac
                    4976, 4982,	4988, 4994 -> hpIncrease += Power.itemBonus(player, Equipment.getItemSlot(item.getId()), item.getId(), (int)(3)); //Barrows
        }
        return hpIncrease;
    }

    public static double weightReduction(Player player) {
        double xp = player.getSkills().getXp(Skills.AGILITY) - 13_000_000;
        if(xp < 0.0)
            return 0.0;
        double weightDecrease = xp/100_000.0 + player.getCounterValue("Codex_Weight");
        return weightDecrease;
    }

    public static double maxPower(Player player) {
        String weaponId = String.valueOf(player.getEquipment().getWeaponId());
        return powerEquation(player.getCounterValue("PowerWeaponId_" + weaponId));
    }

    public static void incrementDefence(Player player) {
        if(lock3Seconds(player, "IncrementedDefence")) //1/40 chance every 3 seconds
            return;
        if(Utils.random(40) == 1 || Settings.getConfig().isDebug()) {//1/40 to continue
            int armourId = -1;
            int slot = -1;
            ArrayList<Integer> armourSlots = new ArrayList<>(Arrays.asList(0, 1, 2, 4, 5, 7, 9, 10, 12));
            for (int i = armourSlots.size(); i > 0; i--) {
                slot = armourSlots.remove(Utils.random(armourSlots.size())).intValue();
                if (player.getEquipment().get(slot) != null) {
                    armourId = player.getEquipment().get(slot).getId();
                    break;
                }
            }
            if (armourId == -1) {
                player.sendMessage("<col=FF0000>No defence was added to armour as there is no armour...");
                return;
            }
            player.incrementCount(armourString(slot, armourId));
            double count = (double) player.getCounterValue(armourString(slot, armourId));
            double newPowerPercentage = (count / denom) * defMul * 100.0;
            String formattedPower = String.format("%.2f", newPowerPercentage);
            player.sendMessage("<col=00FF00>Your defence with your " + getArmourFromNumber(slot).toLowerCase() + " armour has increased to " + formattedPower + "%...");
        }
    }

    public static double getCombatAmplificationPercentage(Player player, int itemId) { // 10% extra bonus -> 10.0
        for(ItemConstants.ItemDegrade item : ItemConstants.ItemDegrade.values())
            if(item.getBrokenId() == itemId || item.getItemId() == itemId)
                itemId = item.getDegradedId();
        int slot = ItemDefinitions.getDefs(itemId).getEquipSlot();
        if(slot == WEAPON) {
            return (((double) player.getCounterValue("PowerWeaponId_" + itemId)) / denom) * 100.0;
        }
        double count = (double) player.getCounterValue(armourString(slot, itemId));
        double powerPercentage = (count / denom) * defMul * 100.0;
        return powerPercentage;
    }

    public static int regenBraceletRegenBonus(Player player) {
        double percentage = getCombatAmplificationPercentage(player, 11133);
        return (int) (percentage / 50.0 + 2);
    }
    public static int itemBonus(Player player, int slot, int armourID, int bonus) {
        if (bonus < 1)
            return bonus;
        double powerResult = 1;
        if(slot == WEAPON) {
            double power = maxPower(player);
            if(player.getDungManager().isInsideDungeon())
                power = (power - 1) * 7 + 1;
            if(player.getEquipment().getWeaponId() == -1)
                power = 1.0;
            powerResult = (double) bonus * power;
        }
        else
            powerResult = bonusEquation(bonus, player.getCounterValue(armourString(slot, armourID)));
        return (int) Math.ceil(powerResult);
    }

    public static int rangedStr(Player player, int weaponId, int bonusAmount) {
        if(weaponId == 20171 || weaponId == 20173 || weaponId == 20174)
            return bonusAmount;
        else {
            double perc = 1 + (getCombatAmplificationPercentage(player, weaponId) / 100);
            return (int) Math.ceil(bonusAmount * perc);
        }
    }

    public static String armourString(int armourSlot, int armourId){
        return "Defence"+ getArmourFromNumber(armourSlot) + "Id_" + armourId;
    }


    public static final int HEAD = 0,
            CAPE = 1,
            NECK = 2,
            WEAPON = 3,
            CHEST = 4,
            SHIELD = 5,
            LEGS = 7,
            HANDS = 9,
            FEET = 10,
            RING = 12,
            AMMO = 13,
            AURA = 14;

    public static String getArmourFromNumber(int number) {
        switch (number) {
            case HEAD:
                return "HEAD";
            case CAPE:
                return "CAPE";
            case NECK:
                return "NECK";
            case WEAPON:
                return "WEAPON";
            case CHEST:
                return "CHEST";
            case SHIELD:
                return "SHIELD";
            case LEGS:
                return "LEGS";
            case HANDS:
                return "HANDS";
            case FEET:
                return "FEET";
            case RING:
                return "RING";
            case AMMO:
                return "AMMO";
            case AURA:
                return "AURA";
            default:
                return "UNKNOWN";
        }
    }

    public static LoginHandler specialWeapons = new LoginHandler(e -> {

    });

    @ServerStartupEvent
    public static void addPower() {
        CombatFormulaKt.onCombatFormulaAdjust((entity, target, _, _) -> {
            if(entity instanceof Player player) {
                if(target instanceof NPC npc)
                    ScalingWorld.changeCombatBoss(player, npc);
                incrementPower(player);
                incrementDefence(player);
                double power = maxPower(player);
                if(player.getDungManager().isInsideDungeon())
                    power = (power - 1) * 5 + 1;
                if(player.getEquipment().getWeaponId() == -1)
                    return new CombatMod(power, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
            }
            return new CombatMod(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
        });
    }
}
