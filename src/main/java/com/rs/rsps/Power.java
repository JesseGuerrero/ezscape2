package com.rs.rsps;

import com.rs.game.model.entity.player.Player;
import com.rs.lib.util.GenericAttribMap;
import com.rs.lib.util.Utils;
import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.utils.Ticks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@PluginEventHandler
public class Power {
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

    private static double powerEquation(double power, double count) {
        return power * 1.0+(count/100_000.0);
    }

    public static void incrementPower(Player player) {
        if(lock3Seconds(player, "IncrementedPower")) //1/40 chance every 3 seconds
            return;
        if(Utils.random(40) != 1) //1/40 to continue
            return;
        String weaponId = String.valueOf(player.getEquipment().getWeaponId());
        player.incrementCount("PowerWeaponId_" + weaponId);
        double newPowerPercentage = (((double)player.getCounterValue("PowerWeaponId_" + weaponId))/100_000.0) * 100.0;
        String formattedPower = String.format("%.3f", newPowerPercentage);
        player.sendMessage("<col=00FF00>Your power with this weapon has increased to " + formattedPower + "%...");
    }

    public static int maxPower(Player player, int maxHit) {
        String weaponId = String.valueOf(player.getEquipment().getWeaponId());
        int newMax = (int)powerEquation(maxHit, player.getCounterValue("PowerWeaponId_" + weaponId));
        return newMax;
    }

    public static double accuracyPower(Player player, double accuracy) {
        String weaponId = String.valueOf(player.getEquipment().getWeaponId());
        return powerEquation(accuracy, player.getCounterValue("PowerWeaponId_" + weaponId));
    }

    public static void incrementDefence(Player player) {
        if(lock3Seconds(player, "IncrementedDefence")) //1/40 chance every 3 seconds
            return;
        if(Utils.random(40) != 1) //1/40 to continue
            return;
        int armourId = -1;
        int slot = -1;
        ArrayList<Integer> armourSlots = new ArrayList<>(Arrays.asList(0, 1, 2, 4, 5, 7, 9, 10, 12));
        for(int i = armourSlots.size(); i > 0; i--) {
            slot = armourSlots.remove(Utils.random(armourSlots.size())).intValue();
            if(player.getEquipment().get(slot) != null) {
                armourId = player.getEquipment().get(slot).getId();
                break;
            }
        }
        if(armourId == -1) {
            player.sendMessage("<col=FF0000>No defence was added to armour as there is no armour...");
            return;
        }
        player.incrementCount(armourString(slot, armourId));
        double newPowerPercentage = (((double)player.getCounterValue(armourString(slot, armourId)))/100_000.0) * 100.0;
        String formattedPower = String.format("%.3f", newPowerPercentage);
        player.sendMessage("<col=00FF00>Your defence with your " + getArmourFromNumber(slot).toLowerCase() + " armour has increased to " + formattedPower + "%...");
    }

    public static int defenceBonus(Player player, int armourSlot, int armourID, int bonus) {
        if(bonus < 1)
            return bonus;
        return (int)powerEquation(bonus, player.getCounterValue(armourString(armourSlot, armourID)));
    }

    private static String armourString(int armourSlot, int armourId){
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
}
