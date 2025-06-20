package com.rs.rsps.Power;

import java.util.Random;
import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.engine.dialogue.Dialogue;
import com.rs.engine.dialogue.HeadE;
import com.rs.game.content.ItemConstants;
import com.rs.game.content.bosses.qbd.QBDController;
import com.rs.game.content.combat.special_attacks.SpecialAttacksKt;
import com.rs.game.content.holidayevents.christmas.christ19.Christmas2019;
import com.rs.game.content.minigames.castlewars.CastleWarsPlayingController;
import com.rs.game.content.minigames.fightcaves.FightCavesController;
import com.rs.game.content.minigames.fightkiln.FightKilnController;
import com.rs.game.content.minigames.soulwars.SoulWarsGameController;
import com.rs.game.model.entity.npc.NPC;
import com.rs.game.model.entity.npc.combat.NPCCombatDefinitions;
import com.rs.game.model.entity.player.Controller;
import com.rs.game.model.entity.player.Equipment;
import com.rs.game.model.entity.player.Player;
import com.rs.lib.game.Item;
import com.rs.lib.util.Utils;
import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.plugin.handlers.ItemOnItemHandler;
import com.rs.plugin.handlers.LoginHandler;
import com.rs.plugin.handlers.NPCClickHandler;
import com.rs.plugin.handlers.NPCDropHandler;
import com.rs.utils.Ticks;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PluginEventHandler
public class ScalingWorld {
    public static int minimumBossLevel = 0;
    public static Object[] uniqueSuperScaledItems = {};
    public static int costPerUpOne = 2750;
    public static int megaPerUpOne = 200;
    public static int topPerUpOne = 500;
    public static int megaUpAmount = 18;
    public static int topUpAmount = 9;
    public static double upperNormie = 5;
    public static double lowerNormie = 0.5;
    /**
     * Also increase scale of Nex items with consuming
     * Mega tier is +22%//Best and toughest items to get
     * Top tier is +13% //Uniques from bosses
     * Tier 8 is 3%
     * Tier 1 is 0.5%
     * Fighter torso and fighter hat completely excluded
     * Make the cost per percent included
     */



    public static Object[] excludedItems = {
        10551, //Fighters Torso
        10548, //Fighters hat
        11283, //Extra dragonfire sheild
        //Chaotics
        18350, 18352, 18354, 18356, 18358, 18360, 18362, 18364,
    };

    public static Object[] includedItems = {
        //godswords
        11702, //bandos
        11704,//arma
        11706,//sara
        11708,//zam
        //spirit shields
        13746, //arc
        13748,//div
        13750, //ely
        13752, //spec
        //visage
        11286,
        //dragon platebody parts
        14472, 14474, 14476
    };

    public static Object[] scaledMegaItems = { //+ 15%
            14484, //Dragon claws
            11284, //Dragonfire shield
            6570, //Fire cape
            23659, //Kiln cape
            6585, //Fury
            24338, //Royal Crossbow

            //nex, make sure to add to HP increase too
            //torva
            20135, 20137, //helm
            20139, 20141, //platebody
            20143, 20145, //legs
            24977, 24978, 25060, //gloves
            24983, 24984, 25064, //boots

            //pernix
            20147, 20149, //cowl
            20151, 20153, //body
            20155, 20157, //chaps
            24974, 24975, 25058, //gloves
            24989, 24990, 25068, //boots

            //virtus
            20159, 20161, //mask
            20163, 20165, //top
            20167, 20169, //legs
            24980, 24981, 25062, //gloves
            24986, 24987, 25066, //boots

            20171, 20173, //Zaryte Bow

            //Godswords
            11694, //arma
            11696, //bandos
            11698, //sara
            11700, //zam

            //spirit shields
            13738, //arc
            13740, //div
            13742, //ely
            13744, //spec

            11133, //Regen Bracelet
            7158, //Dragon 2H
            11335, //Dragon Full Helm
    };

    public static Object[] scaledTopItems = {
            1249, //Dragon Spear
            21787, //Steadfast
            21790, //Glaiven
            21793, //Ragefire
            2513, //Dragon chain
            24360, //Dragon platebody
            15486, //Staff of light

            6739, // D hatchet
            6733, // Archers Ring
            6737, // Berz Ring
            6735, // Warrior Ring
            6731, // Seers Ring

            //bandos
            25022, //helm
            11724, //chestplate
            11726, //tassets
            11728, //boots
            25025, //gloves
            25019, //warshield

            //zamorak
            11716, //spear
            24992, //hood
            24995, //garb
            24998, //gown
            25001, //ward
            25004, //boots
            25007, //gloves

            //sara
            11730, //sword
            25028, //whisper
            25031, //hiss
            25034, //murmur
            25037, //arma crossbow

            //arma
            11718, //helm
            11720, //chest
            11722, //legs
            25010, //boots
            25013, //buckler
            25016, //gloves

            //Chaotics
            18349,
            18351,
            18353,
            18355,
            18357,
            18359,
            18361,
            18363,

            //Barrows
            //Ahrim
            4856,
            4862,
            4868,
            4874,
            //Dharok
            4880,
            4886,
            4892,
            4898,
            //Guthan
            4904,
            4910,
            4916,
            4922,
            //Karil
            4928,
            4934,
            4940,
            4946,
            //Torag
            4952,
            4958,
            4964,
            4970,
            //Verac
            4976,
            4982,
            4988,
            4994
    };
    public static int[] scaledAllItems = {};
    public static int[] weaponItems = {};

    public static void tithe(Player player, int hours) {
        double yourIncrease = player.getCounterValue("ServicesAttended") * 0.000001;
        if(yourIncrease > 0.0001) // .2% -> 5%/day
            yourIncrease = 0.0001;
        yourIncrease = 10_000.0 * yourIncrease * (double)player.getCounterValue("10kputin");
        if(Settings.getConfig().isDebug())
            hours = 3;
        int coinsAdded = (int)yourIncrease * hours;
        if(coinsAdded > 0) {
            player.sendMessage("You have " + coinsAdded + " coins added for " + hours + " hours.");
            player.getInventory().addCoins(coinsAdded);
        } else if(Settings.getConfig().isDebug())
            player.sendMessage("You have " + coinsAdded + " coins added.");
    }

    public static int scaleToItem(int itemId, int power) {
        if(Arrays.stream(scaledMegaItems).toList().contains(itemId))
            return power* megaUpAmount;
        if(Arrays.stream(scaledTopItems).toList().contains(itemId))
            return power* topUpAmount;
        else {
            int maxTier = 1;
            for (Integer skillRequirement : ItemDefinitions.getDefs(itemId).getWearingSkillRequiriments().values())
                maxTier = Math.max(maxTier, skillRequirement);
            if(maxTier >= 80)
                maxTier = 80;
            return (int)Math.max((power*(maxTier/80.0*upperNormie)), power*lowerNormie);
        }
    }

    static {
        Set<Integer> itemIds = new HashSet<>(Arrays.asList());
        Set<Integer> weaponIds = new HashSet<>(Arrays.asList());
        for (int i = 0; i < Utils.getItemDefinitionsSize(); i++)
            if (ItemDefinitions.getDefs(i).isNoted() || ItemDefinitions.getDefs(i).isStackable()) {
                if(ItemDefinitions.getDefs(i).getName().toLowerCase().contains("charm") || i == 995)
                    continue;
                itemIds.add(i);
            }
        scaledAmountItems = itemIds.toArray();
        itemIds.clear();
        for (int i = 0; i < Utils.getItemDefinitionsSize(); i++) {
            if (Arrays.stream(excludedItems).toList().contains(i))
                continue;
            if (Arrays.stream(includedItems).toList().contains(i)) {
                itemIds.add(i);
                continue;
            }
            if (!ItemDefinitions.getDefs(i).isNoted() && !ItemDefinitions.getDefs(i).isStackable()
                    && ItemDefinitions.getDefs(i).isWearItem() && ItemDefinitions.getDefs(i).getEquipSlot() != Equipment.AURA
                    && ItemDefinitions.getDefs(i).getEquipSlot() != Equipment.AMMO) {
                itemIds.add(i);
                if(ItemDefinitions.getDefs(i).getEquipSlot() == Equipment.WEAPON || ItemDefinitions.getDefs(i).isEquipType(5))
                    weaponIds.add(i);
            }
        }
        scaledAllItems = itemIds.stream().mapToInt(Integer::intValue).toArray();
        weaponItems = itemIds.stream().mapToInt(Integer::intValue).toArray();

        uniqueSuperScaledItems = new Object[scaledTopItems.length + scaledMegaItems.length + includedItems.length];
        int index = 0;
        for (int j = 0; j < scaledTopItems.length; j++)
            uniqueSuperScaledItems[index++] = (int) scaledTopItems[j];
        for (int j = 0; j < scaledMegaItems.length; j++)
            uniqueSuperScaledItems[index++] = (int) scaledMegaItems[j];
        for (int j = 0; j < includedItems.length; j++)
            uniqueSuperScaledItems[index++] = (int) includedItems[j];
    }

    /*
    * Make a 33% chance of listed items having a scaled consumable metadata
    * Each scale amount is +10% boss stats and HP.
    * The item adds 3%*scale to the total item bonus.
    * Default scaled items are 1%
    * Use it with a hammer
    * Only NPCs as high as 275
    * Only Specific items to avoid abuse
    *
    */
    public static NPCClickHandler handleScaleModerator = new NPCClickHandler(new Object[]{ 2899 }, new String[]{"Talk-to"}, e -> {
        e.getPlayer().startConversation(new Dialogue()
            .addNPC(2899, HeadE.CALM_TALK, "Your highest unlocked scale is " + String.valueOf(e.getPlayer().getScaleAvailable()))
            .addNPC(2899, HeadE.CALM_TALK, "We fight not with flesh and blood but with principalities and rulers of darkness of this world", () ->{
                e.getPlayer().getMusicsManager().reset();
            })
            .addNPC(2899, HeadE.CALM_TALK, "The monasteries and Christians of the world hold back the true strength of evil in our reality.")
            .addNPC(2899, HeadE.CALM_TALK, "However evil rulers will have more possessions when defeated.")
            .addNPC(2899, HeadE.CALM_TALK, "We can allow them to gain power for your sake if you wish...")
            .addNext(() -> {
//                if(e.getPlayer().getBool("WaitScale") && !Settings.isOwner(e.getPlayer().getUsername()) && !Settings.getConfig().isDebug()) {
//                    e.getPlayer().sendMessage("You must wait about 10 minutes between scale changes or lodestone teleport");
//                    return;
//                }
                e.getPlayer().sendInputInteger("How much would you like to scale the world?", (scale) -> {
                    if(scale < 1)
                        scale = 0;
                    if(scale > e.getPlayer().getScaleAvailable()) {
                        scale = e.getPlayer().getScaleAvailable();
                        e.getPlayer().sendMessage("Your unlocked scale is " + e.getPlayer().getScaleAvailable());
                    }
                    e.getPlayer().set("WorldScale", scale);
//                    e.getPlayer().set("WaitScale", true);
//                    e.getPlayer().getTasks().schedule(Ticks.fromMinutes(10), () -> e.getPlayer().set("WaitScale", false));
                    e.getPlayer().playCutscene(new ReleaseStrongholds());
                });
            })
        );
    });

    public static void resetWorldScale(Player player) { //On Lodestone Teleport
        player.set("WaitScale", false);
    }


    private static int scaleAmountEquation(Player player, int npcScale, int amount) {
        return (int)Math.ceil(amount*(npcScale*0.1 + 1));
    }

    private static Object[] scaledAmountItems;

    public static NPCDropHandler scaleItemAmounts = new NPCDropHandler(null, scaledAmountItems, e -> {
        if(e.getPlayer().getDungManager().isInsideDungeon() || e.getNPC().getDefinitions().combatLevel < minimumBossLevel)
            return;
        int npcScale = extractScaleFromName(e.getNPC().getName());
        e.getItem().setAmount(scaleAmountEquation(e.getPlayer(), npcScale, e.getItem().getAmount()));
    });

    public static NPCDropHandler scaleItemPower = new NPCDropHandler(null, uniqueSuperScaledItems, e -> {
        if(e.getPlayer().getDungManager().isInsideDungeon() || e.getNPC().getDefinitions().combatLevel < minimumBossLevel)
            return;
        int npcScale = extractScaleFromName(e.getNPC().getName());
        e.getPlayer().sendMessage(npcScale + "." + e.getNPC().getName());
        if(npcScale < 1)
            return;
        if(Utils.random(0, 3) == 0) {
            e.getPlayer().soundEffect(4042, true);
            e.getPlayer().sendMessage("<col=00FF00>Your item sparkles...");
            npcScale += 2;
        }
        e.getItem().addMetaData("Power", (int)(npcScale/1.5));
    });

    public static ItemOnItemHandler consumeWeaponItems = new ItemOnItemHandler(weaponItems, new int[]{995}, e -> {
        Item weaponItem = e.getItem1().getId() == 995 ? e.getItem2() : e.getItem1();
        String itemName = weaponItem.getName();
        int weaponItemId = weaponItem.getId();
        double percentage = Power.getCombatAmplificationPercentage(e.getPlayer(), weaponItemId);
        String formattedPower = String.format("%.2f", percentage);
        if(percentage >= 500.0 || Settings.getConfig().isDebug()) {
            e.getPlayer().startConversation(new Dialogue().addOptions("You have " + formattedPower + " do you want to use 500%?", options -> {
                options.add("Yes", new Dialogue()
                        .addSimple("You chip the coin at the weapon...")
                        .addSimple("A book seems to grow from the " + itemName)
                        .addNext(() -> {
                            if(e.getPlayer().getInventory().hasFreeSlots()) {
                                if (Settings.getConfig().isDebug())
                                    Power.incrementItem(e.getPlayer(), weaponItemId, 0);
                                else
                                    Power.incrementItem(e.getPlayer(), weaponItemId, -25_000);
                                Item codex = new Item(6767);
                                codex.setMetaDataO("Codex", itemName);
                                codex.setMetaDataO("WeaponId", weaponItemId);
                                e.getPlayer().getInventory().addItem(codex);
                            } else
                                e.getPlayer().sendMessage("You need at least one inventory space for this.");
                        })
                );

                options.add("No");
            }));
        } else {
            e.getPlayer().startConversation(new Dialogue().addSimple("You have " + formattedPower + " but you require 500%..."));
        }

    });

    public static boolean checkMetaListForString(String string, String searchString) {
        if(string == null || searchString == null)
            return false;
        return Arrays.asList((string).split(",")).contains(searchString);
    }

    public static int[] getMetaListFromString(String string) {
        if (string == null) {
            return new int[0]; // Return empty array if input is null
        }

        return Arrays.stream(string.split(","))
                .filter(Objects::nonNull) // Skip nulls (though split() won't produce nulls)
                .map(String::trim)
                .filter(s -> !s.isEmpty()) // Skip empty strings after trimming
                .map(s -> {
                    try {
                        return Integer.parseInt(s);
                    } catch (NumberFormatException e) {
                        return null; // Convert parsing exceptions to null
                    }
                })
                .filter(Objects::nonNull) // Skip nulls from failed parsing
                .mapToInt(Integer::intValue)
                .toArray();
    }


    //Add Login to this
    public static ItemOnItemHandler addSpec = new ItemOnItemHandler(weaponItems, new int[]{6767}, e -> {
        Item weaponItem = e.getItem1().getId() == 6767 ? e.getItem2() : e.getItem1();
        Item codex = e.getItem1().getId() != 6767 ? e.getItem2() : e.getItem1();
        int weaponItemId = weaponItem.getId();
        if(!codex.containsMetaData() || SpecialAttacksKt.getSpec(weaponItemId) == null)
            return;
        String weaponName = weaponItem.getName();
        String idForToMergeWeapon = Integer.toString(weaponItemId);
        String specWeaponName = codex.getMetaDataO("Codex");
        int specWeaponid = ((Double)codex.getMetaDataO("WeaponId")).intValue();
        String idForExtracted = Integer.toString(specWeaponid);
        e.getPlayer().startConversation(new Dialogue().addOptions("Do you want to insert " + specWeaponName + " special into " + weaponName + "?", options -> {
            options.add("Yes", new Dialogue()
                    .addSimple("You place the weapon inside the codex and close the book...")
                    .addSimple("A book seems to glow...")
                    .addNext(() -> {
                        if(SpecialAttacksKt.getSpec(weaponItemId).getType() == SpecialAttacksKt.getSpec(specWeaponid).getType()) {
                            if(e.getPlayer().getO("weaponsWithSpecials") == null) {
                                e.getPlayer().set("weaponsWithSpecials", idForToMergeWeapon);
                                e.getPlayer().set(idForToMergeWeapon + "_specs", idForExtracted);
                            } else if(!checkMetaListForString(e.getPlayer().getO("weaponsWithSpecials"), Integer.toString(weaponItemId))) {
                                e.getPlayer().set("weaponsWithSpecials", e.getPlayer().getO("weaponsWithSpecials") + "," + idForToMergeWeapon);
                                e.getPlayer().set(idForToMergeWeapon + "_specs", e.getPlayer().getO(idForToMergeWeapon + "_specs") + "," + idForExtracted);
                            } else {
                                e.getPlayer().set(idForToMergeWeapon + "_specs", e.getPlayer().getO(idForToMergeWeapon + "_specs") + "," + idForExtracted);
                            }
                            codex.deleteMetaData();
                        } else {
                            e.getPlayer().startConversation(new Dialogue().addSimple("The codex weapon is a different type than your weapon"));
                        }

                    })
            );

            options.add("No");
        }));


    });

    /**
     *
     * Get the sum of all requirements and add that, triple d claws
     */
    public static ItemOnItemHandler consumePowerItems = new ItemOnItemHandler(scaledAllItems, new int[]{2347}, e -> {
        Item scaledItem = e.getItem1().getId() == 2347 ? e.getItem2() : e.getItem1();
        String itemName = scaledItem.getName();
        int buffItemId = scaledItem.getId();
        for(ItemConstants.ItemDegrade item :  ItemConstants.ItemDegrade.values()) {
            if(scaledItem.getId() == item.getBrokenId()) {
                e.getPlayer().sendMessage("You cannot consume broken items...");
                return;
            }
            if(scaledItem.getId() == item.getItemId())
                buffItemId = item.getDegradedId();
        }
        switch(scaledItem.getId()) {
            //godswords
            case 11702 -> { buffItemId = 11694; } //bandos
            case 11704 -> { buffItemId = 11696; } //arma
            case 11706 -> { buffItemId = 11698; } //sara
            case 11708 -> { buffItemId = 11700; } //zam

            //spirit shields
            case 13746 -> { buffItemId = 13738; } //arc
            case 13748 -> { buffItemId = 13740; } //div
            case 13750 -> { buffItemId = 13742; } //ely
            case 13752 -> { buffItemId = 13744; } //spec

            //visage
            case 11286 -> { buffItemId = 11283; }

            //Ahrim
            case 4708 -> { buffItemId = 4856; }
            case 4710 -> { buffItemId = 4862; }
            case 4712 -> { buffItemId = 4868; }
            case 4714 -> { buffItemId = 4874; }
            //Dharok
            case 4716 -> { buffItemId = 4880; }
            case 4718 -> { buffItemId = 4886; }
            case 4720 -> { buffItemId = 4892; }
            case 4722 -> { buffItemId = 4898; }
            //Guthan
            case 4724 -> { buffItemId = 4904; }
            case 4726 -> { buffItemId = 4910; }
            case 4728 -> { buffItemId = 4916; }
            case 4730 -> { buffItemId = 4922; }
            //Karil
            case 4732 -> { buffItemId = 4928; }
            case 4734 -> { buffItemId = 4934; }
            case 4736 -> { buffItemId = 4940; }
            case 4738 -> { buffItemId = 4946; }
            //Torag
            case 4745 -> { buffItemId = 4952; }
            case 4747 -> { buffItemId = 4958; }
            case 4749 -> { buffItemId = 4964; }
            case 4751 -> { buffItemId = 4970; }
            //Verac
            case 4753 -> { buffItemId = 4976; }
            case 4755 -> { buffItemId = 4982; }
            case 4757 -> { buffItemId = 4988; }
            case 4759 -> { buffItemId = 4994; }

            case 14472, 14474, 14476 -> { buffItemId = 14476; }
            default -> {}
        }
        int itemId = buffItemId;
        int scale = scaledItem.getMetaDataI("Power", 1);
        int powerIncrease = scaleToItem(itemId, Power.consumeIncrease * scale);
        int cost = powerIncrease * (Arrays.stream(scaledMegaItems).toList().contains(itemId) ? megaPerUpOne : Arrays.stream(scaledTopItems).toList().contains(itemId) ? topPerUpOne : costPerUpOne);
        double newPowerPercentage = (((double) powerIncrease / Power.denom) * 100.0);
        String formattedPower = String.format("%.2f", newPowerPercentage);
        if(e.getPlayer().getInventory().hasCoins(cost))
            e.getPlayer().startConversation(new Dialogue()
                    .addItem(scaledItem.getId(), "You look at the hammer and you look at your " + itemName + "(" + scale + ")?")
                    .addSimple("You think for a minute....")
                    .addPlayer(HeadE.SKEPTICAL_THINKING, "Do I really want to destroy and consume " + itemName + "("
                            + scale + ")? Hmm, looks like " + String.format("%,d", cost) + "GP for about +" + formattedPower + "%...")
                    .addOptions("Do you really want to destroy " + itemName + "?", option -> {
                        option.add("Yes", new Dialogue()
                            .addSimple("You hit the " + itemName + "(" + scale + ")" + " with your hammer and " + String.format("%,d", cost) + "GP goes into the armour...")
                            .addSimple("Sparks fly...")
                            .addNext(() -> {
                                Power.incrementItem(e.getPlayer(), itemId, powerIncrease);
                                if(itemId == 11283)
                                    Power.incrementItem(e.getPlayer(), itemId+1, powerIncrease);
                                e.getPlayer().getInventory().deleteItem(scaledItem);
                                e.getPlayer().getInventory().removeCoins(cost);
                                e.getPlayer().sendMessage("<col=00FF00>Your power with " + itemName + " has increased by " + formattedPower + "%...");
                            })
                        );
                        option.add("No");
                    })
            );
        else
            e.getPlayer().startConversation(new Dialogue().addSimple("You need " + String.format("%,d", cost) + "GP..."));
    });

    public static boolean incrementChins() {
        return true;
    }

    public static void chinGiveMore(Player player) {
        int chinchompas = player.getCounterValue("Chinchompas_Success");
        int multiplier = chinchompas / 15000;

        int remaining = chinchompas % 15000; // Remaining chinchompas after guaranteed rolls
        double chance = (remaining / 150) * 0.01;

        if (new Random().nextDouble() < chance)
            multiplier += 1;

        player.getInventory().addItem(10034, multiplier);
    }

    private static Class<? extends Controller>[] exemptControllers = new Class[]{
            FightKilnController.class, FightCavesController.class, SoulWarsGameController.class, CastleWarsPlayingController.class, QBDController.class
    };

    public static void changeCombatBoss(Player player, NPC npc) {
        if(npc.getName() == "Umbra" || npc.getName() == "Nex" || npc.getName() == "Fumus" || npc.getName() == "Glacies"
                || npc.getName() == "Cruor" || npc.getName() == "Queen Black Dragon" || npc.getName() == "Nex")
            return;
        double scale = player.getI("WorldScale", 0);
        if(scale == 0 || player.getCutsceneManager().hasCutscene() || player.getDungManager().isInsideDungeon()
                || npc.getHitpoints() < npc.getMaxHitpoints() || npc.getCombatLevel() < minimumBossLevel)
            return;
        for (Class<? extends Controller> controller : exemptControllers) // This includes Queen Black Dragon
            if (controller.isInstance(player.getControllerManager().getController()))
                return;

        int npcScale = extractScaleFromName(npc.getName());
        if(scale > npcScale) {
            String originalName = NPCDefinitions.getDefs(npc.getId()).getName();
            int originalLevel = NPCDefinitions.getDefs(npc.getId()).combatLevel;
            double boost = 1 + (scale / 10.0);
            Arrays.stream(NPCCombatDefinitions.Skill.values()).forEach(skill -> {
                npc.setStat(skill, (int)(npc.getCombatDefinitions().getLevels().get(skill)*boost));
                npc.setHitpoints((int) (npc.getMaxHitpoints()*boost));
            });
            npc.setCombatLevel((int) (npc.getCombatLevel() * boost));
            npc.setName(originalName + " (" + (int)scale + ")");
            npc.getTasks().schedule("ResetScale", Ticks.fromMinutes(7), () -> {
                npc.setName(originalName);
                npc.setCombatLevel(originalLevel);
                npc.resetLevels();
                if(npc.getHitpoints() > npc.getMaxHitpoints());
                    npc.setHitpoints(npc.getMaxHitpoints());
            });
        }
    }

    public static void printCombatLevelsOnExamine(Player player, NPC npc) {
        Arrays.stream(NPCCombatDefinitions.Skill.values()).toList().forEach((skill) -> {
            player.sendMessage(skill.name() + ": " +  npc.getStat(skill) + ", ");
        });
    }

    public static void resetNameAndCombatLevelOnDeath(NPC npc) {
        if(npc.getCombatLevel() < minimumBossLevel)
            return;
        String originalName = NPCDefinitions.getDefs(npc.getId()).getName();
        int originalLevel = NPCDefinitions.getDefs(npc.getId()).combatLevel;
        npc.setName(originalName);
        npc.setCombatLevel(originalLevel);
        npc.resetLevels();
    }

    public static int extractScaleFromName(String npcName) {
        Pattern pattern = Pattern.compile("\\((\\d+)\\)$");
        Matcher matcher = pattern.matcher(npcName);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return -1; // Return -1 if no valid number is found
    }

    public static int scaleCoins(Player player, NPC npc, int amount) {
        if(player.getDungManager().isInsideDungeon() || npc.getDefinitions().combatLevel < minimumBossLevel)
            return amount;
        return scaleAmountEquation(player, extractScaleFromName(npc.getName()), (int)(amount));
    }

    public static int scaleCharms(Player player, NPC npc, Item item) {
        int amount = item.getAmount();
        if(player.getDungManager().isInsideDungeon() || npc.getDefinitions().combatLevel < minimumBossLevel)
            return amount;
        if(item.getDefinitions().isStackable())
            amount = scaleAmountEquation(player, extractScaleFromName(npc.getName()), item.getAmount());
        return amount;
    }
}
