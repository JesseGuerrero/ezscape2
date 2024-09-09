package com.rs.rsps;

import com.rs.game.model.entity.player.Player;
import com.rs.lib.util.GenericAttribMap;
import com.rs.lib.util.Utils;
import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.utils.Ticks;
import java.util.Arrays;

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

    private static double powerEquation(double power, double count) {
        return power * 1.0+(count/50_000.0);
    }

    public static void incrementPower(Player player) {
        if(lock3Seconds(player, "IncrementedPower") && Utils.random(40) == 1) //1/40 chance every 3 seconds
            return;
        String weaponId = String.valueOf(player.getEquipment().getWeaponId());
        player.incrementCount("PowerWeaponId_" + weaponId);
        double newPowerPercentage = (1.0 + ((double)player.getCounterValue("PowerWeaponId_" + weaponId))/50_000.0) * 100.0;
        String formattedPower = String.format("%.2f", newPowerPercentage);
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
        if(lock3Seconds(player, "IncrementedDefence") && Utils.random(40) == 1) //1/40 chance every 3 seconds
            return;

        int armourSlot = Arrays.asList(0, 1, 2, 4, 5, 7, 9, 10, 12).get(Utils.random(9));
        String armourId = String.valueOf(player.getEquipment().get(armourSlot).getId());
        player.incrementCount("DefenceAmourId_" + armourId);
        double newPowerPercentage = (1.0 + ((double)player.getCounterValue("DefenceAmourId_" + armourId))/50_000.0) * 100.0;
        String formattedPower = String.format("%.2f", newPowerPercentage);
        player.sendMessage("<col=00FF00>Your defence with your " + getArmourFromNumber(armourSlot) + " has increased to " + formattedPower + "%...");
    }

    public static int defenceBonus(Player player, int armourID, int bonus) {
        return (int)powerEquation(bonus, player.getCounterValue("DefenceArmourId_" + armourID));
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
