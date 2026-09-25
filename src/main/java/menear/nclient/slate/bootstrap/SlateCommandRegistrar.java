package menear.nclient.slate.bootstrap;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import menear.nclient.slate.bootstrap.SlateUiActions;
import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.macro.MacroState;
import menear.nclient.slate.macro.MacroStateManager;
import menear.nclient.slate.modules.ComposterManager;
import menear.nclient.slate.modules.GreenhouseManager;
import menear.nclient.slate.modules.SupercraftManager;
import menear.nclient.slate.modules.discord.DiscordStatusManager;
import menear.nclient.slate.modules.forge.ForgeManager;
import menear.nclient.slate.modules.failsafe.FailsafeTestManager;
import menear.nclient.slate.modules.interaction.EntityInteractManager;
import menear.nclient.slate.modules.metaldetector.MetalDetectorSolver;
import menear.nclient.slate.modules.inventorymanager.AutoSellManager;
import menear.nclient.slate.modules.movement.MovementPlaybackManager;
import menear.nclient.slate.modules.pathfinding.PathfindingManager;
import menear.nclient.slate.modules.pathfinding.debug.PathVisualizer;
import menear.nclient.slate.modules.pest.DynamicPestsManager;
import menear.nclient.slate.modules.pest.helpers.PestDestroyer;
import menear.nclient.slate.modules.pest.helpers.PestExchangeManager;
import menear.nclient.slate.modules.pest.helpers.PestTrapManager;
import menear.nclient.slate.modules.rotation.RotationManager;
import menear.nclient.slate.modules.visitor.VisitorsMacro;
import menear.nclient.slate.util.SlateLang;
import menear.nclient.slate.util.BazaarUtils;
import menear.nclient.slate.util.ClientUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class SlateCommandRegistrar {
    private SlateCommandRegistrar() {
    }

    public static void register() {
        registerLegacyCommandIntercepts();
        registerClientCommands();
    }

    private static void registerLegacyCommandIntercepts() {
        ClientSendMessageEvents.CHAT.register(message ->
                MovementPlaybackManager.recordOutgoingChat(message, false));
        ClientSendMessageEvents.COMMAND.register(command -> {
            MovementPlaybackManager.recordOutgoingChat(command, true);
            if (command.equalsIgnoreCase("call george")) {
                menear.nclient.slate.modules.inventorymanager.GeorgeManager.onCallGeorgeSent();
            }

            String normalized = command.toLowerCase().trim();
            if (normalized.startsWith("slate goto ")) {
                startCoordinatePathfind(command, false, "\u00A7eUsage: /slate goto <x> <y> <z>");
            }
            if (normalized.startsWith("slate flyto ")) {
                startCoordinatePathfind(command, true, "\u00A7eUsage: /slate flyto <x> <y> <z>");
            }
            if (normalized.equals("slate debug path")) {
                PathVisualizer.toggle();
                ClientUtils.sendMessage(Minecraft.getInstance(),
                        "\u00A7aPath visualizer: " + (PathVisualizer.isEnabled() ? "ON" : "OFF"), false);
            }
            if (normalized.startsWith("slate pathtest ")) {
                startPathTest(command);
            }
        });
    }

    private static void registerClientCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("slate")
                            .executes(ctx -> {
                                SlateUiActions.toggleMainGui(Minecraft.getInstance());
                                return 1;
                            })
                            .then(ClientCommandManager.literal("farming")
                                    .executes(ctx -> {
                                        Minecraft client = Minecraft.getInstance();
                                        if (MacroStateManager.getCurrentState() != MacroState.State.OFF) {
                                            ClientUtils.sendMessage(client, "\u00A7cA macro is already running.", false);
                                            return 0;
                                        }

                                        SlateKeybindHandler.startFarmingMacro(client);
                                        return 1;
                                    }))
                            .then(ClientCommandManager.literal("stop")
                                    .executes(ctx -> {
                                        Minecraft client = Minecraft.getInstance();
                                        if (MacroStateManager.getCurrentState() == MacroState.State.OFF) {
                                            ClientUtils.sendMessage(client, "\u00A7eNo macro is currently running.", false);
                                            return 0;
                                        }

                                        MacroStateManager.stopMacro(client);
                                        ClientUtils.sendMessage(client, "\u00A7eStopped active macro.", false);
                                        return 1;
                                    }))
                            .then(ClientCommandManager.literal("status")
                                    .executes(ctx -> {
                                        Minecraft client = Minecraft.getInstance();
                                        if (DiscordStatusManager.requestManualStatusUpdate(client)) {
                                            ClientUtils.sendMessage(client,
                                                    "\u00A7eSending Discord webhook status update.", false);
                                            return 1;
                                        }

                                        ClientUtils.sendMessage(client,
                                                "\u00A7cUnable to send Discord status update. Check your webhook URL or wait for the current screenshot to finish.",
                                                false);
                                        return 0;
                                    }))
                            .then(ClientCommandManager.literal("printscoreboard")
                                    .executes(ctx -> printScoreboard(Minecraft.getInstance())))
                            .then(ClientCommandManager.literal("rotate")
                                    .then(ClientCommandManager.argument("pitch", FloatArgumentType.floatArg(-90.0f, 90.0f))
                                            .suggests((ctx, builder) -> suggestAngles(builder, "-90", "-45", "0", "45", "90"))
                                            .then(ClientCommandManager.argument("yaw", FloatArgumentType.floatArg())
                                                    .suggests((ctx, builder) -> suggestAngles(builder,
                                                            "-180", "-135", "-90", "-45", "0", "45", "90", "135", "180"))
                                                    .executes(ctx -> {
                                                        float pitch = FloatArgumentType.getFloat(ctx, "pitch");
                                                        float yaw = FloatArgumentType.getFloat(ctx, "yaw");
                                                        return rotateToPitchYaw(Minecraft.getInstance(), pitch, yaw);
                                                    }))))
                            .then(ClientCommandManager.literal("movement")
                                    .executes(ctx -> {
                                        sendMovementHelp(Minecraft.getInstance());
                                        return 1;
                                    })
                                    .then(ClientCommandManager.literal("record")
                                            .executes(ctx -> {
                                                MovementPlaybackManager.startRecording(Minecraft.getInstance());
                                                return 1;
                                            }))
                                    .then(ClientCommandManager.literal("stop")
                                            .executes(ctx -> {
                                                MovementPlaybackManager.stop(Minecraft.getInstance());
                                                return 1;
                                            }))
                                    .then(ClientCommandManager.literal("folder")
                                            .executes(ctx -> {
                                                MovementPlaybackManager.openMovementFolder(Minecraft.getInstance());
                                                return 1;
                                            }))
                                    .then(ClientCommandManager.literal("play")
                                            .then(ClientCommandManager.argument("replay_file", StringArgumentType.greedyString())
                                                    .suggests((ctx, builder) -> suggestMovementReplays(builder))
                                                    .executes(ctx -> {
                                                        String replayFile = StringArgumentType.getString(ctx, "replay_file");
                                                        MovementPlaybackManager.play(Minecraft.getInstance(), replayFile);
                                                        return 1;
                                                    }))))
                            .then(ClientCommandManager.literal("testfailsafe")
                                    .executes(ctx -> {
                                        sendFailsafeTestHelp(Minecraft.getInstance());
                                        return 1;
                                    })
                                    .then(ClientCommandManager.literal("inventoryslot")
                                            .then(ClientCommandManager.argument("slot", IntegerArgumentType.integer(1, 9))
                                                    .suggests((ctx, builder) -> suggestAngles(builder,
                                                            "1", "2", "3", "4", "5", "6", "7", "8", "9"))
                                                    .executes(ctx -> {
                                                        int slot = IntegerArgumentType.getInteger(ctx, "slot");
                                                        FailsafeTestManager.scheduleInventorySlot(Minecraft.getInstance(), slot);
                                                        return 1;
                                                    })))
                                    .then(ClientCommandManager.literal("rotation")
                                            .then(ClientCommandManager.argument("pitch", FloatArgumentType.floatArg())
                                                    .suggests((ctx, builder) -> suggestAngles(builder, "-30", "-15", "0", "15", "30"))
                                                    .then(ClientCommandManager.argument("yaw", FloatArgumentType.floatArg())
                                                            .suggests((ctx, builder) -> suggestAngles(builder,
                                                                    "-90", "-45", "-30", "-15", "15", "30", "45", "90"))
                                                            .executes(ctx -> {
                                                                float pitch = FloatArgumentType.getFloat(ctx, "pitch");
                                                                float yaw = FloatArgumentType.getFloat(ctx, "yaw");
                                                                FailsafeTestManager.scheduleRotation(Minecraft.getInstance(), pitch, yaw);
                                                                return 1;
                                                            }))))
                                    .then(ClientCommandManager.literal("guiflash")
                                            .then(ClientCommandManager.argument("duration", IntegerArgumentType.integer(1))
                                                    .executes(ctx -> {
                                                        int duration = IntegerArgumentType.getInteger(ctx, "duration");
                                                        FailsafeTestManager.scheduleGuiFlash(Minecraft.getInstance(), duration);
                                                        return 1;
                                                    }))))
                            .then(ClientCommandManager.literal("pathfind")
                                    .executes(ctx -> {
                                        sendPathfindHelp(Minecraft.getInstance());
                                        return 1;
                                    })
                                    .then(ClientCommandManager.literal("stop")
                                            .executes(ctx -> {
                                                PathfindingManager.stop();
                                                ClientUtils.sendMessage(Minecraft.getInstance(),
                                                        "\u00A7ePathfinder stopped.", false);
                                                return 1;
                                            }))
                                    .then(ClientCommandManager.literal("fly")
                                            .executes(ctx -> {
                                                sendPathfindHelp(Minecraft.getInstance());
                                                return 1;
                                            })
                                            .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
                                                    .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
                                                            .then(ClientCommandManager.argument("z", IntegerArgumentType.integer())
                                                                    .executes(ctx -> {
                                                                        int x = IntegerArgumentType.getInteger(ctx, "x");
                                                                        int y = IntegerArgumentType.getInteger(ctx, "y");
                                                                        int z = IntegerArgumentType.getInteger(ctx, "z");
                                                                        PathfindingManager.startDebugFlyPathfind(
                                                                                Minecraft.getInstance(), x, y, z);
                                                                        return 1;
                                                                    })))))
                                    .then(ClientCommandManager.literal("etherwarp")
                                            .executes(ctx -> {
                                                sendPathfindHelp(Minecraft.getInstance());
                                                return 1;
                                            })
                                            .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
                                                    .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
                                                            .then(ClientCommandManager.argument("z", IntegerArgumentType.integer())
                                                                    .executes(ctx -> {
                                                                        int x = IntegerArgumentType.getInteger(ctx, "x");
                                                                        int y = IntegerArgumentType.getInteger(ctx, "y");
                                                                        int z = IntegerArgumentType.getInteger(ctx, "z");
                                                                        PathfindingManager.startDebugEtherwarpPathfind(
                                                                                Minecraft.getInstance(), x, y, z);
                                                                        return 1;
                                                                    })))))
                                    .then(ClientCommandManager.literal("walk")
                                            .executes(ctx -> {
                                                sendPathfindHelp(Minecraft.getInstance());
                                                return 1;
                                            })
                                            .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
                                                    .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
                                                            .then(ClientCommandManager.argument("z", IntegerArgumentType.integer())
                                                                    .executes(ctx -> {
                                                                        int x = IntegerArgumentType.getInteger(ctx, "x");
                                                                        int y = IntegerArgumentType.getInteger(ctx, "y");
                                                                        int z = IntegerArgumentType.getInteger(ctx, "z");
                                                                        PathfindingManager.startDebugPathfind(
                                                                                Minecraft.getInstance(), x, y, z);
                                                                        return 1;
                                                                    })))))
                                    .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
                                            .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
                                                    .then(ClientCommandManager.argument("z", IntegerArgumentType.integer())
                                                            .executes(ctx -> {
                                                                int x = IntegerArgumentType.getInteger(ctx, "x");
                                                                int y = IntegerArgumentType.getInteger(ctx, "y");
                                                                int z = IntegerArgumentType.getInteger(ctx, "z");
                                                                PathfindingManager.startDebugPathfind(
                                                                        Minecraft.getInstance(), x, y, z);
                                                                return 1;
                                                            })))))
                            .then(ClientCommandManager.literal("pestexchange")
                                    .executes(ctx -> {
                                        PestExchangeManager.start(Minecraft.getInstance());
                                        return 1;
                                    }))
                            .then(ClientCommandManager.literal("metaldetector")
                                    .executes(ctx -> {
                                        MetalDetectorSolver.toggle(Minecraft.getInstance());
                                        return 1;
                                    })
                                    .then(ClientCommandManager.literal("scan")
                                            .executes(ctx -> {
                                                MetalDetectorSolver.forceScan(Minecraft.getInstance());
                                                return 1;
                                            })))
                            .then(ClientCommandManager.literal("bazaar")
                                    .executes(ctx -> {
                                        ClientUtils.sendMessage(Minecraft.getInstance(),
                                                "\u00A7eUsage: /slate bazaar <item> <count>", false);
                                        return 1;
                                    })
                                    .then(ClientCommandManager.argument("item", StringArgumentType.string())
                                            .then(ClientCommandManager.argument("count", IntegerArgumentType.integer(1))
                                                    .executes(ctx -> {
                                                        String item = StringArgumentType.getString(ctx, "item");
                                                        int count = IntegerArgumentType.getInteger(ctx, "count");
                                                        BazaarUtils.buy(Minecraft.getInstance(), item, count, success -> {
                                                            if (!success) {
                                                                Minecraft.getInstance().execute(() -> {
                                                                    if (Minecraft.getInstance().player != null) {
                                                                        ClientUtils.sendMessage(Minecraft.getInstance(),
                                                                                "\u00A7cBazaar buy failed.", false);
                                                                    }
                                                                });
                                                            }
                                                        });
                                                        return 1;
                                                    }))))
                            .then(ClientCommandManager.literal("visitors")
                                    .executes(ctx -> {
                                        VisitorsMacro.start(Minecraft.getInstance(), true);
                                        return 1;
                                    }))
                            .then(ClientCommandManager.literal("pestdestroyer")
                                    .executes(ctx -> {
                                        Minecraft client = Minecraft.getInstance();
                                        if (PestDestroyer.isActive()) {
                                            PestDestroyer.stop(client);
                                            ClientUtils.sendMessage(client, "\u00A7ePest Destroyer stopped.", false);
                                        } else {
                                            PestDestroyer.start(client);
                                        }
                                        return 1;
                                    }))
                            .then(ClientCommandManager.literal("dynamicpest")
                                    .then(ClientCommandManager.argument("crop", StringArgumentType.greedyString())
                                            .suggests((ctx, builder) -> suggestDynamicPestCrops(builder))
                                            .executes(ctx -> {
                                                String crop = StringArgumentType.getString(ctx, "crop");
                                                return triggerDynamicPest(Minecraft.getInstance(), crop);
                                            })))
                            .then(ClientCommandManager.literal("autosell")
                                    .executes(ctx -> {
                                        Minecraft client = Minecraft.getInstance();
                                        if (AutoSellManager.isSelling || AutoSellManager.isPreparingToSell) {
                                            ClientUtils.sendMessage(client, "\u00A7cAutoSell is already running.", false);
                                        } else {
                                            AutoSellManager.manualTrigger(client);
                                        }
                                        return 1;
                                    }))
                            .then(ClientCommandManager.literal("pesttraps")
                                    .executes(ctx -> {
                                        PestTrapManager.start(Minecraft.getInstance());
                                        return 1;
                                    }))
                            .then(ClientCommandManager.literal("greenhouseharvest")
                                    .executes(ctx -> {
                                        GreenhouseManager.harvest(Minecraft.getInstance());
                                        return 1;
                                    }))
                            .then(ClientCommandManager.literal("debugskulls")
                                    .executes(ctx -> {
                                        GreenhouseManager.debugScanSkulls(Minecraft.getInstance());
                                        return 1;
                                    }))
                            .then(ClientCommandManager.literal("composter")
                                    .executes(ctx -> {
                                        ComposterManager.manualTrigger(Minecraft.getInstance());
                                        return 1;
                                    }))
                            .then(ClientCommandManager.literal("supercraft")
                                    .executes(ctx -> {
                                        SupercraftManager.manualTrigger(Minecraft.getInstance());
                                        return 1;
                                    }))
                            .then(ClientCommandManager.literal("refilltraps")
                                    .executes(ctx -> {
                                        PestTrapManager.startRefill(Minecraft.getInstance());
                                        return 1;
                                    }))
                            .then(ClientCommandManager.literal("forge")
                                    .executes(ctx -> {
                                        ForgeManager.start(Minecraft.getInstance());
                                        return 1;
                                    }))
                            .then(ClientCommandManager.literal("interact")
                                    .then(ClientCommandManager.argument("entity_name", StringArgumentType.greedyString())
                                            .executes(ctx -> {
                                                String entityName = StringArgumentType.getString(ctx, "entity_name");
                                                EntityInteractManager.start(Minecraft.getInstance(), entityName);
                                                return 1;
                                            })))
                            .then(ClientCommandManager.literal("config")
                                    .executes(ctx -> {
                                        sendConfigHelp(Minecraft.getInstance());
                                        return 1;
                                    })
                                    .then(ClientCommandManager.literal("export")
                                            .executes(ctx -> exportConfig(Minecraft.getInstance())))
                                    .then(ClientCommandManager.literal("import")
                                            .executes(ctx -> {
                                                sendConfigHelp(Minecraft.getInstance());
                                                return 1;
                                            })
                                            .then(ClientCommandManager.argument("config_string", StringArgumentType.greedyString())
                                                    .executes(ctx -> importConfig(Minecraft.getInstance(),
                                                            StringArgumentType.getString(ctx, "config_string")))))));
        });
    }

    private static void startCoordinatePathfind(String command, boolean fly, String usage) {
        String[] args = command.trim().split("\\s+");
        if (args.length != 5) {
            return;
        }

        try {
            int x = Integer.parseInt(args[2]);
            int y = Integer.parseInt(args[3]);
            int z = Integer.parseInt(args[4]);
            if (fly) {
                PathfindingManager.startDebugFlyPathfind(Minecraft.getInstance(), x, y, z);
            } else {
                PathfindingManager.startDebugPathfind(Minecraft.getInstance(), x, y, z);
            }
        } catch (NumberFormatException ignored) {
            ClientUtils.sendMessage(Minecraft.getInstance(), usage, false);
        }
    }

    private static void startPathTest(String command) {
        String[] args = command.trim().split("\\s+");
        if (args.length != 5) {
            return;
        }

        try {
            int x = Integer.parseInt(args[2]);
            int y = Integer.parseInt(args[3]);
            int z = Integer.parseInt(args[4]);
            PathfindingManager.startPathTest(Minecraft.getInstance(), x, y, z);
        } catch (NumberFormatException ignored) {
            ClientUtils.sendMessage(Minecraft.getInstance(), "\u00A7eUsage: /slate pathtest <x> <y> <z>", false);
        }
    }

    private static void sendPathfindHelp(Minecraft client) {
        ClientUtils.sendMessage(client, "\u00A7ePathfind commands:", false);
        ClientUtils.sendMessage(client, "\u00A77  /slate pathfind \u00A7fx y z \u00A78- walk to coords", false);
        ClientUtils.sendMessage(client, "\u00A77  /slate pathfind \u00A7fwalk x y z \u00A78- walk to coords", false);
        ClientUtils.sendMessage(client, "\u00A77  /slate pathfind \u00A7ffly x y z \u00A78- fly to coords", false);
        ClientUtils.sendMessage(client, "\u00A77  /slate pathfind \u00A7fetherwarp x y z \u00A78- etherwarp to coords", false);
        ClientUtils.sendMessage(client, "\u00A77  /slate pathfind \u00A7fstop \u00A78- stop pathfinding", false);
    }

    private static void sendFailsafeTestHelp(Minecraft client) {
        ClientUtils.sendMessage(client, "\u00A7eFailsafe test commands:", false);
        ClientUtils.sendMessage(client, "\u00A77  /slate testfailsafe \u00A7finventoryslot <slot> \u00A78- switch hotbar slot", false);
        ClientUtils.sendMessage(client, "\u00A77  /slate testfailsafe \u00A7frotation <pitch> <yaw>", false);
        ClientUtils.sendMessage(client, "\u00A77  /slate testfailsafe \u00A7fguiflash <durationTicks>", false);
    }

    private static void sendConfigHelp(Minecraft client) {
        ClientUtils.sendMessage(client, "\u00A7eConfig commands:", false);
        ClientUtils.sendMessage(client,
                "\u00A77  /slate config \u00A7fexport \u00A78- copy your config to the clipboard", false);
        ClientUtils.sendMessage(client,
                "\u00A77  /slate config \u00A7fimport <string> \u00A78- apply a pasted config string", false);
    }

    private static int exportConfig(Minecraft client) {
        String json = SlateConfig.exportSanitizedJson();
        client.keyboardHandler.setClipboard(json);
        ClientUtils.sendMessage(client, String.format(
                "\u00A7aConfig copied to clipboard (%d chars). Sensitive fields (license key, webhook, usernames) were blanked.",
                json.length()), false);
        return 1;
    }

    private static int importConfig(Minecraft client, String json) {
        if (SlateConfig.importFromJson(json)) {
            SlateBootstrapHooks.onConfigProfileLoaded(SlateConfig.getConfigFile());
            ClientUtils.sendMessage(client, "\u00A7aConfig imported and applied.", false);
            return 1;
        }
        ClientUtils.sendMessage(client,
                "\u00A7cConfig import failed - the string is not valid config JSON.", false);
        return 0;
    }

    private static void sendMovementHelp(Minecraft client) {
        ClientUtils.sendMessage(client, "\u00A7eMovement commands:", false);
        ClientUtils.sendMessage(client, "\u00A77  /slate movement \u00A7frecord \u00A78- record movement events", false);
        ClientUtils.sendMessage(client, "\u00A77  /slate movement \u00A7fstop \u00A78- stop recording or playback", false);
        ClientUtils.sendMessage(client, "\u00A77  /slate movement \u00A7ffolder \u00A78- open the movement folder", false);
        ClientUtils.sendMessage(client, "\u00A77  /slate movement \u00A7fplay <replay_file> \u00A78- play a recording", false);
    }

    private static int rotateToPitchYaw(Minecraft client, float pitch, float yaw) {
        if (client.player == null) {
            return 0;
        }

        if (RotationManager.isRotating()) {
            RotationManager.cancelRotation();
        }

        RotationManager.rotateToYawPitch(client, yaw, pitch, 0L);
        ClientUtils.sendMessage(client,
                String.format("\u00A7eRotating to pitch %.1f yaw %.1f.", pitch, yaw), false);
        return 1;
    }

    private static int printScoreboard(Minecraft client) {
        List<String> lines = ClientUtils.getSidebarLines(client);
        if (lines.isEmpty()) {
            ClientUtils.sendMessage(client, "\u00A7c" + SlateLang.localize("No scoreboard lines found."), false);
            return 0;
        }

        ClientUtils.sendMessage(client,
                "\u00A7e" + SlateLang.localize("Scoreboard lines (%d):").formatted(lines.size()), false);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).isBlank() ? SlateLang.localize("[blank]") : lines.get(i);
            ClientUtils.sendMessage(client, "\u00A77" + (i + 1) + ". " + line, false);
        }
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestAngles(SuggestionsBuilder builder, String... values) {
        String remaining = builder.getRemaining();
        for (String value : values) {
            if (value.startsWith(remaining)) {
                builder.suggest(value);
            }
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestMovementReplays(SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        for (String file : MovementPlaybackManager.listReplayFiles()) {
            if (file.toLowerCase().startsWith(remaining)) {
                builder.suggest(file);
            }
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestDynamicPestCrops(SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        for (String crop : DynamicPestsManager.getAvailableCrops()) {
            if (crop.toLowerCase().startsWith(remaining)) {
                builder.suggest(crop);
            }
        }
        return builder.buildFuture();
    }

    private static int triggerDynamicPest(Minecraft client, String crop) {
        if (client.player == null || client.getConnection() == null) {
            return 0;
        }

        if (DynamicPestsManager.triggerTestApply(client, crop)) {
            ClientUtils.sendMessage(client,
                    SlateLang.localize("Dynamic Pests test triggered for %s.").formatted(crop), false);
            return 1;
        }

        ClientUtils.sendMessage(client, SlateLang.localize("Unable to trigger Dynamic Pests test."), false);
        return 0;
    }
}

