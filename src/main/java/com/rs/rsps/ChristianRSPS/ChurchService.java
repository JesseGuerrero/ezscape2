package com.rs.rsps.ChristianRSPS;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rs.anarchy.PVPZonesKt;
import com.rs.engine.cutscene.Cutscene;
import com.rs.engine.dialogue.Conversation;
import com.rs.engine.dialogue.Dialogue;
import com.rs.engine.dialogue.HeadE;
import com.rs.engine.pathfinder.Direction;
import com.rs.game.content.skills.magic.TeleType;
import com.rs.game.map.instance.Instance;
import com.rs.game.model.entity.Entity;
import com.rs.game.model.entity.npc.NPC;
import com.rs.game.model.entity.player.Controller;
import com.rs.game.model.entity.player.Player;
import com.rs.game.model.object.GameObject;
import com.rs.game.tasks.Task;
import com.rs.game.tasks.WorldTasks;
import com.rs.lib.game.Tile;
import com.rs.lib.util.Utils;
import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.plugin.annotations.ServerStartupEvent;
import com.rs.plugin.handlers.PlayerStepHandler;
import com.rs.utils.Ticks;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@PluginEventHandler
public class ChurchService extends Controller {
    protected static boolean serviceStarted = false;
    protected static String[][] sermons = null;
    private static String[] praises = {
            "Hallelujah.",
            "Thank you Jesus.",
            "Have mercy on us.",
            "We praise you Lord.",
            "Praise God.",
            "In Jesus name.",
            "I will dance like David.",
            "Amen",
            "Glory to God.",
            "Bless the Lord, O my soul.",
            "Holy, holy, holy.",
            "Jesus is Lord.",
            "We exalt your name.",
            "Your grace is sufficient.",
            "The Lord is my shepherd.",
            "He is worthy of all praise.",
            "The Lord reigns forever.",
            "Blessed be your name.",
            "Great is your faithfulness.",
            "Thank you for your mercy.",
            "The Lord is my rock and salvation.",
            "Your love endures forever.",
            "We lift your name on high.",
            "You are the King of kings.",
            "All glory to your name."
    };
    public static Instance church = null;
    private static Tile entranceTile = null;
    protected static Tile serviceTile = null;
    private static final Tile OUTSIDE = Tile.of(3237, 3209, 0);

    protected static NPC priest;
    protected static List<NPC> npcs = new ArrayList<>();

    public ChurchService() {
        super();
    }

    public boolean playAmbientMusic() {
        return false;
    }

    @Override
    public void start() {
        player.lock();
        Tile start = entranceTile;
        player.playCutscene(new Cutscene() {
            @Override
            public void construct(Player player) {
                player.lock();
                setEndTile(start);
                fadeIn(0);
                hideMinimap();
                delay(3);
                playerMove(start.x(), start.y(), start.getPlane(), Entity.MoveType.TELE, 0);
                delay(3);
                fadeOut(0);
                player.unlock();
            }
        });
    }

    @Override
    public boolean sendDeath() {
        player.stopAll();
        player.reset();
        player.tele(OUTSIDE);
        forceClose();
        return false;
    }

    @Override
    public boolean login() {
        player.tele(OUTSIDE);
        forceClose();
        return false;
    }

    @Override
    public boolean logout() {
        player.unlock();
        return false;
    }

    @Override
    public void forceClose() {
        player.getPackets().setBlockMinimapState(0);
        player.unlock();
        removeController();
    }

    @Override
    public void moved() {
        Tile tile = player.getTile();
        if(tile.matches(entranceTile) || tile.matches(Tile.of(entranceTile.x(), entranceTile.y() + 1, entranceTile.plane()))) {
            player.playCutscene(new Cutscene() {
                @Override
                public void construct(Player player) {
                    player.lock();
                    setEndTile(OUTSIDE);
                    fadeIn(0);
                    hideMinimap();
                    delay(3);
                    playerMove(OUTSIDE.x(), OUTSIDE.y(), OUTSIDE.getPlane(), Entity.MoveType.TELE, 0);
                    delay(3);
                    fadeOut(0);
                    player.unlock();
                    player.getControllerManager().removeControllerWithoutCheck();
                }
            });
        }
    }

    @Override
    public void onTeleported(TeleType type) {
        forceClose();
    }

    public static PlayerStepHandler enterChurch = new PlayerStepHandler(new Tile[] { Tile.of(3238, 3209, 0), Tile.of(3238, 3210, 0)}, e -> {
        if(e.getPlayer().getI("ChurchType", 0) > 0 || true) { // 0 no church, 1 pentacostal, 2 protestant
            e.getPlayer().getControllerManager().startController(new ChurchService());
        }
    });


    public boolean processObjectClick1(GameObject object) {
        if(object.getId() == 36972) {
            if(player.getDailyB("HasDoneSermon")) {
                player.startConversation(new Dialogue().addPlayer(HeadE.HAPPY_TALKING, "I have already done service for today. Maybe I can catch it with another player."));
                return true;
            }
            if(serviceStarted == true) {
                player.startConversation(new Dialogue().addPlayer(HeadE.HAPPY_TALKING, "There is a service right now. I will get my blessing at the end..."));
                return true;
            }
            serviceStarted = true;
            WorldTasks.delay(Ticks.fromMinutes(2), () -> {
                serviceStarted = false;
            });
            player.playCutscene(new Sermon());
            return false;
        }
        return true;
    }


    @ServerStartupEvent
    public static void init() {
        sermons = loadAllSermons("src/main/java/com/rs/rsps/ChristianRSPS/sermons");
        spawnNPC(2899, Tile.of(3237, 3208, 0), "Father John", Direction.NORTH, false);
        ChurchService.church = Instance.of(OUTSIDE, 6, 6, false);
        church.copyMapAllPlanes(403, 399);
        WorldTasks.delay(4, () -> {
            ChurchService.entranceTile = church.getLocalTile(14, 17);
            ChurchService.serviceTile = church.getLocalTile(21, 15);
            priest = spawnNPC(456, church.getLocalTile(20, 13), "Father Aereck", Direction.NORTH, false);
            npcs.add(spawnNPC(0, church.getLocalTile(23, 15), "Hans", Direction.SOUTHWEST, false));
            npcs.add(spawnNPC(668, church.getLocalTile(17, 17), "Johnathan", Direction.SOUTH, false));
            npcs.add(spawnNPC(7969, church.getLocalTile(21, 19), "Jack", Direction.SOUTH, false));
            npcs.add(spawnNPC(8864, church.getLocalTile(18, 19), "Hank", Direction.SOUTH, false));
            npcs.add(spawnNPC(758, church.getLocalTile(21, 17), "Fred", Direction.SOUTH, false));
            npcs.add(spawnNPC(4589, church.getLocalTile(17, 15), "Mike", Direction.SOUTHEAST, false));
        });
        WorldTasks.scheduleTimer(6, Ticks.fromSeconds(10), (ticks) -> {
            if(!serviceStarted)
                npcs.get(Utils.random(npcs.size())).forceTalk(praises[Utils.random(praises.length)]);
            return true;
        });

    }

    public static NPC spawnNPC(int id, Tile tile, String name, Direction direction, boolean randomWalk) {
        NPC npc = new NPC(id, tile);
        if (name != null)
            npc.setPermName(name);
        if (direction != null)
            npc.setFaceAngle(direction.getAngle());
        npc.setRandomWalk(randomWalk);
        return npc;
    }

    public static String[][] loadAllSermons(String directoryPath) {
        File directory = new File(directoryPath);
        List<String[]> sermonsList = new ArrayList<>();

        // Check if the directory exists and is a directory
        if (directory.exists() && directory.isDirectory()) {
            // Get all files in the directory
            File[] files = directory.listFiles((dir, name) -> name.endsWith(".json"));

            if (files != null) {
                Gson gson = new Gson();

                for (File file : files) {
                    try (FileReader reader = new FileReader(file)) {
                        // Parse each file's JSON content
                        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

                        // Extract the "Sermon" array from JSON
                        JsonArray sermonArray = jsonObject.getAsJsonArray("Sermon");

                        if (sermonArray != null) {
                            String[] sermonParts = new String[sermonArray.size()];

                            // Convert JsonArray to String array
                            for (int i = 0; i < sermonArray.size(); i++) {
                                sermonParts[i] = sermonArray.get(i).getAsString();
                            }

                            // Add this sermon to the list
                            sermonsList.add(sermonParts);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Convert the list to a 2D array
        return sermonsList.toArray(new String[0][]);
    }

}
