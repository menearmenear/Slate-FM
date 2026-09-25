package menear.nclient.slate.bootstrap;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.config.ConfigHelpers;
import menear.nclient.slate.macro.MacroState;
import menear.nclient.slate.macro.MacroStateManager;
import menear.nclient.slate.macro.MacroWorkerThread;
import menear.nclient.slate.modules.CropFeverManager;
import menear.nclient.slate.modules.metaldetector.MetalDetectorSolver;
import menear.nclient.slate.modules.misc.AutoCarnivalManager;
import menear.nclient.slate.modules.pest.PestManager;
import menear.nclient.slate.modules.pest.helpers.AutoPestExchangeManager;
import menear.nclient.slate.modules.pest.helpers.AutoSprayonatorManager;
import menear.nclient.slate.modules.pest.helpers.PestDestroyer;
import menear.nclient.slate.modules.profit.ProfitManager;
import menear.nclient.slate.modules.inventorymanager.AutoSellManager;
import menear.nclient.slate.modules.session.RestartManager;
import menear.nclient.slate.modules.visitor.VisitorManager;
import menear.nclient.slate.util.SlateResources;
import menear.nclient.slate.util.BazaarUtils;
import menear.nclient.slate.util.ClientUtils;
import menear.nclient.slate.util.CommandUtils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SlateChatEvents {
    private static final Pattern PLOT_PATTERN = Pattern.compile("Plot\\s*[\\-#:]\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SPAWN_COUNT_PATTERN =
            Pattern.compile("(?i)yuck!\\s*(\\d+)\\D+pest\\s+(?:have|has)\\s+spawned");
    private static final Pattern BAZAAR_BUY_PATTERN =
            Pattern.compile("\\[Bazaar\\]\\s*Bought\\s+(\\d+)x\\s+.+?for\\s+([\\d,]+)\\s+coins",
                    Pattern.CASE_INSENSITIVE);

    private static boolean isHandlingMessage = false;

    private SlateChatEvents() {
    }

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
                        String text = message.getString();
            String plainText = text.replaceAll("(?i)[\u00A7&][0-9a-fk-or]", "");
            String lowerText = text.toLowerCase();
            String lowerPlainText = plainText.toLowerCase();

            MetalDetectorSolver.onGameMessage(Minecraft.getInstance(), plainText, overlay);

            if (plainText.contains("Teleported you to The Barn!")) {
                menear.nclient.slate.modules.visitor.VisitorsMacro.hasBarnTeleportMessage = true;
            }
            if (plainText.contains("Executing instant sell...")) {
                BazaarUtils.detectedInstantSell = true;
            }
            if (plainText.contains("You don't have anything to sell!")) {
                BazaarUtils.detectedNoItemsToSell = true;
            }
            if (overlay || isHandlingMessage) {
                return;
            }

            CommandUtils.onChatMessage(plainText);

            try {
                isHandlingMessage = true;
                ProfitManager.handleChatMessage(message);
                AutoCarnivalManager.handleChatMessage(Minecraft.getInstance(), message, plainText);
                handleVisitorAcceptance(plainText);
                handlePestDestroyerNoPests(lowerPlainText);
                handleAutoSellNpcCoinLimit(plainText);
                if (handleProxyRestartMessage(text, plainText)) {
                    return;
                }
                if (handleRestartMessage(text, plainText)) {
                    return;
                }

                if (PestManager.isCleaningInProgress || AutoPestExchangeManager.isRunning()) {
                    PestManager.handlePhillipMessage(Minecraft.getInstance(), text);
                    AutoSprayonatorManager.onChatMessage(plainText);
                    return;
                }

                if (handleDisconnectMessage(text)) {
                    return;
                }

                handleSprayBazaarBuy(plainText);
                handleYuckDebug(lowerText, plainText);
                handlePestChatTrigger(lowerText, plainText);
                handleStashState(lowerText);
                AutoSprayonatorManager.onChatMessage(plainText);
                CropFeverManager.handleChatMessage(Minecraft.getInstance(), plainText);
            } finally {
                isHandlingMessage = false;
            }
        });
    }

    private static void handleVisitorAcceptance(String plainText) {
        String acceptedVisitorName = VisitorManager.extractAcceptedVisitorName(plainText);
        if (acceptedVisitorName != null) {
            VisitorManager.onOfferAccepted(acceptedVisitorName);
        }
    }

    private static void handlePestDestroyerNoPests(String lowerPlainText) {
        if (!lowerPlainText.contains("there are not any pests on your garden right now! keep farming!")) {
            return;
        }

        if (PestDestroyer.isActive()) {
            ClientUtils.sendDebugMessage(Minecraft.getInstance(),
                    "[PestDestroyer] Detected 'No Pests' message. Finishing destroyer.");
            PestDestroyer.finish(Minecraft.getInstance());
        }
    }

    private static void handleAutoSellNpcCoinLimit(String plainText) {
        if (!plainText.contains("You've reached the daily limit of coins you may earn from NPC shops.")) {
            return;
        }

        AutoSellManager.handleNpcCoinLimit(Minecraft.getInstance());
    }

    private static boolean handleProxyRestartMessage(String text, String plainText) {
        if (!containsValidTrigger(text, "This proxy is restarting soon.") || plainText.contains("[Slate]")) {
            return false;
        }

        RestartManager.handleProxyRestartMessage(Minecraft.getInstance());
        return true;
    }

    private static boolean handleRestartMessage(String text, String plainText) {
        boolean matches = containsValidTrigger(text, "[Important] This server will restart soon: Scheduled Reboot")
                || containsValidTrigger(text, "[Important] This server will restart soon: Game Update")
                || containsValidTrigger(text, "Evacuating to Hub...")
                || containsValidTrigger(text, "SERVER REBOOT!");
        if (!matches || plainText.contains("[Slate]")) {
            return false;
        }

        boolean isImmediate = containsValidTrigger(text, "Evacuating to Hub...")
                || containsValidTrigger(text, "SERVER REBOOT!");
        RestartManager.handleRestartMessage(Minecraft.getInstance(), isImmediate);
        return true;
    }

    private static boolean handleDisconnectMessage(String text) {
        if (!containsValidTrigger(text, "You were spawned in Limbo.")
                && !containsValidTrigger(text,
                "A disconnect occurred in your connection, so you were put in the SkyBlock Lobby!")) {
            return false;
        }

        if (MacroStateManager.getCurrentState() != MacroState.State.OFF
                && MacroStateManager.getCurrentState() != MacroState.State.RECOVERING) {
            ClientUtils.sendMessage(Minecraft.getInstance(),
                    "Disconnect detected! Starting recovery sequence...");
            MacroStateManager.stopMacro(Minecraft.getInstance());
            MacroStateManager.setCurrentState(MacroState.State.RECOVERING);
        }
        return true;
    }

    private static boolean containsValidTrigger(String text, String trigger) {
        int triggerIndex = text.indexOf(trigger);
        if (triggerIndex < 0) {
            return false;
        }

        return text.lastIndexOf(':', triggerIndex - 1) < 0;
    }

    private static void handleSprayBazaarBuy(String plainText) {
        if (!ProfitManager.isSprayPhaseActive || !plainText.contains("[Bazaar]") || !plainText.contains("Bought")) {
            return;
        }

        Matcher matcher = BAZAAR_BUY_PATTERN.matcher(plainText);
        if (!matcher.find()) {
            return;
        }

        try {
            int qty = Integer.parseInt(matcher.group(1));
            long coins = Long.parseLong(matcher.group(2).replace(",", ""));
            ProfitManager.addSprayCost(qty, coins);
            if (SlateConfig.SHOW_DEBUG.get()) {
                ClientUtils.sendDebugMessage(Minecraft.getInstance(),
                        "Spray buy detected: " + qty + " items for " + coins + " coins");
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private static void handleYuckDebug(String lowerText, String plainText) {
        if (lowerText.contains("yuck") && SlateConfig.SHOW_DEBUG.get()) {
            ClientUtils.sendDebugMessage(Minecraft.getInstance(),
                    "Diagnose: Seen YUCK in chat. Text: " + plainText);
        }
    }

    private static void handlePestChatTrigger(String lowerText, String plainText) {
        if (!PestManager.isPestDestroyerEnabled() || !lowerText.contains("yuck") || !lowerText.contains("plot")) {
            return;
        }

        if (SlateConfig.SHOW_DEBUG.get()) {
            ClientUtils.sendDebugMessage(Minecraft.getInstance(),
                    "YUCK detected. State: " + MacroStateManager.getCurrentState());
        }
        if ((!lowerText.contains("spawned") && !lowerText.contains("phillip"))
                || MacroStateManager.getCurrentState() != MacroState.State.FARMING) {
            return;
        }

        Matcher plotMatcher = PLOT_PATTERN.matcher(plainText);
        if (!plotMatcher.find()) {
            if (SlateConfig.SHOW_DEBUG.get()) {
                ClientUtils.sendDebugMessage(Minecraft.getInstance(),
                        "Chat pest trigger failed regex on: " + plainText);
            }
            return;
        }

        String plot = plotMatcher.group(1);
        int spawnedCount = 0;
        Matcher spawnMatcher = SPAWN_COUNT_PATTERN.matcher(plainText);
        if (spawnMatcher.find()) {
            try {
                spawnedCount = Integer.parseInt(spawnMatcher.group(1));
            } catch (NumberFormatException ignored) {
                spawnedCount = 0;
            }
        }

        final int parsedSpawnedCount = spawnedCount;
        MacroWorkerThread.getInstance().submit("PestClean-ChatTrigger-" + plot, () -> {
            if (MacroWorkerThread.shouldAbortTask(Minecraft.getInstance(), MacroState.State.FARMING)) {
                return;
            }

            int triggerDelay = ConfigHelpers.getRandomizedDelay(
                    SlateConfig.PEST_CHAT_TRIGGER_DELAY_MIN.get(),
                    SlateConfig.PEST_CHAT_TRIGGER_DELAY_MAX.get());
            if (triggerDelay > 0) {
                MacroWorkerThread.sleep(triggerDelay);
                if (MacroWorkerThread.shouldAbortTask(Minecraft.getInstance(), MacroState.State.FARMING)) {
                    return;
                }
            }

            PestManager.tryStartCleaningSequenceFromChat(Minecraft.getInstance(), plot, parsedSpawnedCount);
        });
    }

    private static void handleStashState(String lowerText) {
        if (lowerText.contains("stashed away!")) {
            SlateTickHandlers.setPickingUpStash(true);
        }
        if (lowerText.contains("your stash isn't holding any items or materials!")
                || lowerText.contains("couldn't unstash your")) {
            SlateTickHandlers.setPickingUpStash(false);
        }
    }

}

