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
    //Quality check code
    //Push and update

    //Allow shops to save to the world save in data.
    //Agility xp for running
    //All soft bonuses like these only happen when bonuses are 1 or greater
    //Strength: +1 str every 1 million xp
    //Attack: +1 crush, slash, stab every 1 million xp
    //Defense: +1 crush, slash, stab, range defense every 1 million xp
    //Range: +1 ranged att and str every 1 million xp
    //Mage: +1 mage str, def and att
    //Every 2000 cannonballs you get 1 cannonball added per steel bar smithing
    //Every 1000 fired cannonballs you get 1% damage and attack boost
    //Make codexes consumable and tradeable for different bosses or situations

    //QBD
    //Make a codex that gives 1 xp based on codexes consumed every 5 tiles ran, not walked. 1/512 drop rate from QBD
    //Make a codex that converts dragonfire to healing by 1 for fire damage. 1/512 from QBD

    //Bandos
    //Make a codex which increases alchs by 1 coin, 1/512 drop rate

    //Tormented demons
    //Make a codex which regenerates prayer by 5 every 20 ticks. This is half of the 10 in 990, 1/512 drop rate

    //KBD
    //Make a codex which converts poison to healing by 1, cap it on full poisen. 1/512 from KBD

    //Zamorak
    //Add the poison codex 1/512

    //Dagganoth Kings
    //Rex: A codex which adds 1 point to one of these 4, str, crush, slash, stab attack. It only picks one. 1/512
    //Prime: A codex which adds 1 point to one of these 5 mage att, mage def, mage damage 0.25%, def crush, slash
    //Supreme: A codex which adds 1 point to these 4 range str, att, def, defensive stab, 1/512
    //Prime and supreme drop agility codex

    //Armadyl
    //Drop ranged codex

    //Nex
    //Make a codex that increases hitpoints permanently 1 point 1/512 drop rate

    //#1 Do ranged strength fraction based on weapon. Do the same for mage.
    //Create a consumeX
    //Fix item container usage with hammer
    //Combine special attack
    //Later...
    //TODO: Scale QBD Loot, keep it 1x loot
    //TODO: Enhance Corp, QBD, Nex
    //Make dwarf cannon consume cannonballs
    //scale qbd and nex
    //Copy coin pouch code and clamp varbit to UI limit

    //Max cape <-> completionist
    //Effects overhaul barrows
    //1M Herb XP increases duration 5 second and stat +1
    //Add scaling to potions with hammer
    //Do NPCDropHandler instead of injected drops
    //Do gano only charged or full
    //Make a money investment ability
    //Penny Codex

    //Ring of wealth scaling perk
    //Perks past 1000%
    //Scaling summoning -> money
    //1m xp -> Extra time, lowered special
    //Unlock optional house exits
    //Fix requirement of relogging for spec weapons
    //Fix loss of scale
    //Fix GE Bug
    //Fix special attack usage
    //Fix 100% specs not speccing Saradomin sword, double check old bug
    //Fix broadbolts in shop selling for slayer points
    //Teleport Yak gate
    //Item accumulator (20M)
    //Scale chinchompas and cannons
    //Limit scale drops cost
    //Scale Unlock codex, Combat level / 15000*(increase with scale of combat level) chance of dropping a scale X augment, tradeable
    //Make all codexes tradeable.


    public static NPCDeathHandler dropCodexes = new NPCDeathHandler(new Object[]{"Kree\u0027arra", "General Graardor",
            "K\u0027ril Tsutsaroth", "Commander Zilyana", "King Black Dragon", "Dagannoth Prime", "Dagannoth Rex", "Dagannoth Supreme",
            "Corporeal Beast"}, e -> {
        if(e.getKiller() instanceof Player player) {
            if(e.getNPC().getName().equalsIgnoreCase("Kree\u0027arra")) {
                dropCodex(player, e.getNPC(), rollCodexNames(new String[]{"Ranged Attack", "Ranged Strength", "Ranged Defense", "Stab Defense"}));
                dropCodex(player, e.getNPC(), "Weight");
            }
            if(e.getNPC().getName().equalsIgnoreCase("General Graardor")) {
                dropCodex(player, e.getNPC(), "Alchemy");
                dropCodex(player, e.getNPC(), "Weight");
                dropCodex(player, e.getNPC(), rollCodexNames(new String[]{"Melee Strength", "Stab Attack", "Crush Attack", "Slash Attack"}));
            }
            if(e.getNPC().getName().equalsIgnoreCase("K\u0027ril Tsutsaroth")) {
                dropCodex(player, e.getNPC(), "Poison");
                dropCodex(player, e.getNPC(), "Weight");
            }
            if(e.getNPC().getName().equalsIgnoreCase("King Black Dragon")) {
                dropCodex(player, e.getNPC(), "Poison");
                dropCodex(player, e.getNPC(), "Dragon Fire");
                dropCodex(player, e.getNPC(), "Weight");
            }
            if(e.getNPC().getName().equalsIgnoreCase("Dagannoth Prime")) {
                dropCodex(player, e.getNPC(), rollCodexNames(new String[]{"Mage Attack", "Mage Strength", "Mage Defense", "Crush Defense", "Slash Defense"}));
                dropCodex(player, e.getNPC(), "Agility");
                dropCodex(player, e.getNPC(), "Weight");
            }
            if(e.getNPC().getName().equalsIgnoreCase("Dagannoth Rex")) {
                dropCodex(player, e.getNPC(), rollCodexNames(new String[]{"Melee Strength", "Stab Attack", "Crush Attack", "Slash Attack"}));
            }
            if(e.getNPC().getName().equalsIgnoreCase("Dagannoth Supreme"))
                dropCodex(player, e.getNPC(), rollCodexNames(new String[]{"Ranged Attack", "Ranged Strength", "Ranged Defense", "Stab Defense"}));
            if(e.getNPC().getName().equalsIgnoreCase("Tormented demon")) {
                dropCodex(player, e.getNPC(), "Prayer");
                dropCodex(player, e.getNPC(), "Weight");
            }
            if(e.getNPC().getName().equalsIgnoreCase("Corporeal Beast")) {
                dropCodex(player, e.getNPC(), "Prayer");
                dropCodex(player, e.getNPC(), "Agility");
                dropCodex(player, e.getNPC(), "Weight");
            }
            if(e.getNPC().getName().equalsIgnoreCase("Commander Zilyana")) {
                dropCodex(player, e.getNPC(), "Alchemy");
                dropCodex(player, e.getNPC(), "Weight");
                dropCodex(player, e.getNPC(), rollCodexNames(new String[]{"Melee Strength", "Stab Attack", "Crush Attack", "Slash Attack"}));
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
                                e.getPlayer().incrementCount("Codex_" + codexType);
                                e.getPlayer().sendMessage("You have used the Codex of " + codexType);
                                codex.deleteMetaData();
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
        int bonus = (int)Math.ceil(xp / 1_000_000.0);
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
}
