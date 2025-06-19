package com.rs.rsps.Power;

import com.rs.Settings;
import com.rs.cache.loaders.Bonus;
import com.rs.engine.dialogue.Dialogue;
import com.rs.engine.dialogue.HeadE;
import com.rs.game.World;
import com.rs.game.model.entity.Entity;
import com.rs.game.model.entity.Hit;
import com.rs.game.model.entity.npc.NPC;
import com.rs.game.model.entity.npc.combat.NPCCombatDefinitions;
import com.rs.game.model.entity.player.Player;
import com.rs.lib.Constants;
import com.rs.lib.game.Item;
import com.rs.lib.util.Utils;
import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.plugin.handlers.ItemOnItemHandler;
import com.rs.plugin.handlers.NPCDeathHandler;
import com.rs.plugin.handlers.NPCDropHandler;

import java.util.Arrays;

import static com.rs.rsps.Power.ScalingWorld.extractScaleFromName;

@PluginEventHandler
public class SpecialItems {
    //---Features---
    //High value
        //Allow shops to save to the world save in data.
    //Moderate value
        //Every 2000 cannonballs you get 1 cannonball added per steel bar smithing
        //Every 1000 fired cannonballs you get 1% damage and attack boost
        //Skill Cape, Max cape, completionist all share augments
        //Scaling summoning -> consumes summoning shards
        //Unlock optional house exits
        //Respawn GWD boss item, maybe use 50 summoning shards on the prayer altar
    //Low value
        //Enhance Corp, QBD, Nex
            //Scale QBD Loot
        //Scale barrows
        //All soft bonuses like these only happen when bonuses are 1 or greater
        //Create a consumeX
        //HP Copy coin pouch code and clamp varbit to UI limit
        //Special Effects Augmented Barrows, Spirit Shield and DFS
        //100k Herb XP increases duration 5 second and stat +1
        //Add scaling to potions with hammer
        //Ring of wealth scaling perk
        //Perks past 1000%
        //Item accumulator (20M)

    //---BUGS---
    //Small
        //(Doesnt work) Make a codex that converts dragonfire to healing by 1 for fire damage. 1/512 from QBD
        //(Doesnt work) Make a codex which converts poison to healing by 1, cap it on full poisen. 1/512 from KBD
        //Fix requirement of relogging for spec weapons
        //Fix broadbolts in shop selling for slayer points
    //Moderate
    //Huge


    public static NPCDeathHandler dropCodexes = new NPCDeathHandler(new Object[]{"Kree\u0027arra", "General Graardor",
            "K\u0027ril Tsutsaroth", "Commander Zilyana", "King Black Dragon", "Dagannoth Prime", "Dagannoth Rex", "Dagannoth Supreme",
            "Corporeal Beast"}, e -> {
        if(e.getKiller() instanceof Player player) {
            if(e.getNPC().getName().equalsIgnoreCase("Kree\u0027arra")) {
                dropCodex(player, e.getNPC(), rollCodexNames(new String[]{"Ranged Attack", "Ranged Strength", "Ranged Defense", "Stab Defense"}));
                dropCodex(player, e.getNPC(), "Weight");
                dropCodex(player, e.getNPC(), Integer.toString(player.getI("WorldScale")+1) + " Scale Unlock");
            }
            if(e.getNPC().getName().equalsIgnoreCase("General Graardor")) {
                dropCodex(player, e.getNPC(), "Alchemy");
                dropCodex(player, e.getNPC(), "Weight");
                dropCodex(player, e.getNPC(), rollCodexNames(new String[]{"Melee Strength", "Stab Attack", "Crush Attack", "Slash Attack"}));
                dropCodex(player, e.getNPC(), Integer.toString(player.getI("WorldScale")+1) + " Scale Unlock");
            }
            if(e.getNPC().getName().equalsIgnoreCase("K\u0027ril Tsutsaroth")) {
                dropCodex(player, e.getNPC(), "Poison");
                dropCodex(player, e.getNPC(), "Weight");
                dropCodex(player, e.getNPC(), Integer.toString(player.getI("WorldScale")+1) + " Scale Unlock");
            }
            if(e.getNPC().getName().equalsIgnoreCase("King Black Dragon")) {
                dropCodex(player, e.getNPC(), "Poison");
                dropCodex(player, e.getNPC(), "Dragon Fire");
                dropCodex(player, e.getNPC(), "Weight");
                dropCodex(player, e.getNPC(), Integer.toString(player.getI("WorldScale")+1) + " Scale Unlock");
            }
            if(e.getNPC().getName().equalsIgnoreCase("Dagannoth Prime")) {
                dropCodex(player, e.getNPC(), rollCodexNames(new String[]{"Mage Attack", "Mage Strength", "Mage Defense", "Crush Defense", "Slash Defense"}));
                dropCodex(player, e.getNPC(), "Agility");
                dropCodex(player, e.getNPC(), "Weight");
                dropCodex(player, e.getNPC(), Integer.toString(player.getI("WorldScale")+1) + " Scale Unlock");
            }
            if(e.getNPC().getName().equalsIgnoreCase("Dagannoth Rex")) {
                dropCodex(player, e.getNPC(), rollCodexNames(new String[]{"Melee Strength", "Stab Attack", "Crush Attack", "Slash Attack"}));
                dropCodex(player, e.getNPC(), Integer.toString(player.getI("WorldScale")+1) + " Scale Unlock");
            }
            if(e.getNPC().getName().equalsIgnoreCase("Dagannoth Supreme")) {
                dropCodex(player, e.getNPC(), rollCodexNames(new String[]{"Ranged Attack", "Ranged Strength", "Ranged Defense", "Stab Defense"}));
                dropCodex(player, e.getNPC(), Integer.toString(player.getI("WorldScale") + 1) + " Scale Unlock");
            }
            if(e.getNPC().getName().equalsIgnoreCase("Tormented demon")) {
                dropCodex(player, e.getNPC(), "Prayer");
                dropCodex(player, e.getNPC(), "Weight");
                dropCodex(player, e.getNPC(), Integer.toString(player.getI("WorldScale")+1) + " Scale Unlock");
            }
            if(e.getNPC().getName().equalsIgnoreCase("Corporeal Beast")) {
                dropCodex(player, e.getNPC(), "Prayer");
                dropCodex(player, e.getNPC(), "Agility");
                dropCodex(player, e.getNPC(), "Weight");
                dropCodex(player, e.getNPC(), Integer.toString(player.getI("WorldScale")+1) + " Scale Unlock");
            }
            if(e.getNPC().getName().equalsIgnoreCase("Commander Zilyana")) {
                dropCodex(player, e.getNPC(), "Alchemy");
                dropCodex(player, e.getNPC(), "Weight");
                dropCodex(player, e.getNPC(), rollCodexNames(new String[]{"Melee Strength", "Stab Attack", "Crush Attack", "Slash Attack"}));
                dropCodex(player, e.getNPC(), Integer.toString(player.getI("WorldScale")+1) + " Scale Unlock");
            }
        }
    });

    public static String rollCodexNames(String[] names) {
        return names[Utils.random(0, names.length)];
    }

    public static void dropCodex(Player player, NPC npc, String type) {
        int npcScale = extractScaleFromName(npc.getName());
        int chance = 256-npcScale;
        if(chance <= 64)
            chance = 64;
        for(int i = 0; i < 3; i++)
            if(Settings.getConfig().isDebug() || Utils.random(chance) == 0) {
                Item codex = new Item(6767);
                codex.setMetaDataO("Codex", type);
                npc.sendDrop(player, codex);
            }
    }

    public static Item getQBDCodex(String type) {
        Item codex = new Item(6767);
        codex.setMetaDataO("Codex", type);
        return codex;
    }
    public static ItemOnItemHandler consumeCodex = new ItemOnItemHandler(new int[]{6767}, new int[]{2347}, e -> {
        Item codex = e.getItem1().getId() == 2347 ? e.getItem2() : e.getItem1();
        if(!codex.containsMetaData())
            return;
        String codexType = codex.getMetaDataO("Codex");
        e.getPlayer().startConversation(new Dialogue()
                .addItem(codex.getId(), "You look at your Codex of " + codexType + "...")
                .addOptions("Do you really want to destroy it?", option -> {
                    option.add("Yes", new Dialogue()
                            .addSimple("You hammer at the book...")
                            .addSimple("It seems to be glowing with power...")
                            .addNext(() -> {
                                if(codexType.contains(" Scale Unlock")) {
                                    if (e.getPlayer().getScaleAvailable() + 1 == Integer.valueOf(codexType.split(" ")[0])) {
                                        e.getPlayer().addScaleAvailable();
                                    } else {
                                        e.getPlayer().sendMessage("You need a scale " + String.valueOf(e.getPlayer().getScaleAvailable() + 1) + " codex to unlock the next scale.");
                                    }
                                }
                                else if(codexType.contains(" Tiles")) {
                                    int tileCount = Integer.valueOf(codexType.split(" ")[0]);
                                    e.getPlayer().changeTilesAvailableByDiff(tileCount);
                                } else {
                                    e.getPlayer().incrementCount("Codex_" + codexType);
                                }
                                e.getPlayer().sendMessage("You have used the Codex of " + codexType);
                                codex.deleteMetaData();
                            })
                    );
                    option.add("No");
                })
        );

    });

    public static ItemOnItemHandler createTileCodex = new ItemOnItemHandler(new int[]{995}, new int[]{2347}, e -> {
        Item coins = e.getItem1().getId() == 2347 ? e.getItem2() : e.getItem1();
        e.getPlayer().startConversation(new Dialogue()
                .addItem(995, "You look at your coins and the " + String.valueOf(e.getPlayer().getTilesAvailable()) + " tiles that you have...")
                .addOptions("Do you really want to turn your tiles into a codex?", option -> {
                    option.add("Yes", new Dialogue()
                            .addSimple("You hammer the coins into your tiles...")
                            .addSimple("A book forms and seems to be glowing with power...")
                            .addNext(() -> {
                                e.getPlayer().sendInputInteger("How many tiles do you want to put into the book?", (tileCount) -> {
                                    if(!e.getPlayer().getInventory().hasFreeSlots()) {
                                        e.getPlayer().sendMessage("You need at least one free slot...");
                                        return;
                                    }
                                    if(tileCount < 1) {
                                        e.getPlayer().sendMessage("At least 1 tile");
                                        return;
                                    }
                                    if(tileCount > 1_000_000) {
                                        e.getPlayer().sendMessage("Max is 1M tiles");
                                        return;
                                    }
                                    if(tileCount > e.getPlayer().getTilesAvailable()) {
                                        e.getPlayer().sendMessage("You have " + e.getPlayer().getTilesAvailable() + " tiles not " + tileCount + " tiles...");
                                        return;
                                    }
                                    e.getPlayer().changeTilesAvailableByDiff(0-tileCount);
                                    Item codex = new Item(6767);
                                    codex.setMetaDataO("Codex", String.valueOf(tileCount) + " Tiles");
                                    e.getPlayer().getInventory().addItem(codex);
                                });
                            })
                    );
                    option.add("No");
                })
        );

    });

    public static int statBonus(Player player, int skillId) {
        double xp = player.getSkills().getXp(skillId) - 13_000_000.0;
        if(xp < 1.0)
            return 0;
        int bonus = (int)Math.ceil(xp / 100_000.0);
        return bonus;
    }

    public static int codexBonuses(Player player, Bonus bonus) {
        switch (bonus) {
            case STAB_ATT -> {return statBonus(player, Constants.ATTACK) + player.getCounterValue("Codex_Stab Attack");}
            case SLASH_ATT -> {return statBonus(player, Constants.ATTACK) + player.getCounterValue("Codex_Slash Attack");}
            case CRUSH_ATT -> {return statBonus(player, Constants.ATTACK) + player.getCounterValue("Codex_Crush Attack");}
            case MAGIC_ATT -> {return statBonus(player, Constants.ATTACK) + player.getCounterValue("Codex_Mage Attack");}
            case RANGE_ATT -> {return statBonus(player, Constants.ATTACK) + player.getCounterValue("Codex_Ranged Attack");}
            case STAB_DEF -> {return statBonus(player, Constants.DEFENSE) + player.getCounterValue("Codex_Stab Defense");}
            case SLASH_DEF -> {return statBonus(player, Constants.DEFENSE) + player.getCounterValue("Codex_Slash Defense");}
            case CRUSH_DEF -> {return statBonus(player, Constants.DEFENSE) + player.getCounterValue("Codex_Crush Defense");}
            case MAGIC_DEF -> {return statBonus(player, Constants.MAGIC) + player.getCounterValue("Codex_Mage Defense");}
            case RANGE_DEF -> {return statBonus(player, Constants.RANGE) + player.getCounterValue("Codex_Ranged Defense");}
            case SUMM_DEF -> {return player.getCounterValue("");}
            case ABSORB_MELEE -> {return player.getCounterValue("");}
            case ABSORB_MAGIC -> {return player.getCounterValue("");}
            case ABSORB_RANGE -> {return player.getCounterValue("");}
            case MELEE_STR -> {return statBonus(player, Constants.STRENGTH) + player.getCounterValue("Codex_Melee Strength");}
            case RANGE_STR -> {return statBonus(player, Constants.RANGE) + player.getCounterValue("Codex_Ranged Strength");}
            case PRAYER -> {return statBonus(player, Constants.PRAYER) + player.getCounterValue("Codex_Prayer");}
            case MAGIC_STR -> {return player.getCounterValue("Codex_Mage Strength");}
            default -> {return 0;}
        }
    }

    public static int limitAbsorbBonuses(Player player, Bonus bonus, int bonusNum) {
        switch (bonus) {
            case ABSORB_MELEE, ABSORB_MAGIC, ABSORB_RANGE -> {
                if(bonusNum >= 90)
                    return 90;
                else return bonusNum;
            }
            default -> {return bonusNum;}
        }
    }

    public static int reduceDragonFire(Entity entity, int damage) {
        if(entity instanceof Player player) {
            if (player.getCounterValue("Codex_Dragon Fire") < 1)
                return damage;
            return Math.max(0, damage - player.getCounterValue("Codex_Dragon Fire"));
        }
        return damage;
    }

    public static void codexPoison(Player player, int poisonDamage) {
        if(player.getCounterValue("Codex_Poison") > 0)
            player.applyHit(new Hit(player, Math.max(0, poisonDamage - player.getCounterValue("Codex_Dragon Fire")), Hit.HitLook.HEALED_DAMAGE));
    }

    public static int hpCodex(Player player) {
        return statBonus(player, Constants.HITPOINTS)*5 + player.getCounterValue("Codex_Health") * 5;
    }

    public static boolean metaDataDoesntAllowCombine(Item item1, Item item2) {
        return (item1.containsMetaData() || item2.containsMetaData());
    }
}
