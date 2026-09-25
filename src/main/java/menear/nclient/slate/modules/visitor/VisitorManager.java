package menear.nclient.slate.modules.visitor;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.util.TablistUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import menear.nclient.slate.macro.MacroState;
import menear.nclient.slate.macro.MacroStateManager;
import menear.nclient.slate.macro.MacroWorkerThread;
import menear.nclient.slate.util.ClientUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import menear.nclient.slate.util.ItemStackModelUtil;
import menear.nclient.slate.modules.gear.GearManager;
import menear.nclient.slate.modules.gear.helpers.LoadoutManager;
import menear.nclient.slate.modules.pest.PestManager;
import menear.nclient.slate.modules.pest.helpers.PestPrepSwapManager;
import menear.nclient.slate.modules.pest.helpers.PestReturnManager;
import menear.nclient.slate.modules.profit.ProfitManager;

public class VisitorManager {
    private static final Pattern VISITORS_PATTERN = Pattern.compile("Visitors:\\s*\\(?(\\d+)\\)?");
    private static final Pattern OFFER_ACCEPTED_PATTERN = Pattern.compile(
            "^OFFER ACCEPTED with (.+?) \\(([A-Z]+(?: [A-Z]+)*)\\)$");
    private static final long VISITOR_REENTRY_COOLDOWN_MS = 60_000L;

    private static VisitorOffer pendingOffer = null;
    private static volatile long visitorReentryCooldownUntilMs = 0L;

    // -- Inner Data Classes --

    public static class VisitorOffer {
        public String visitorName;
        public long totalCost = 0;
    }

    // -- Existing Methods (unchanged) --

    public static int getVisitorCount(Minecraft client) {
        if (!SlateConfig.AUTO_VISITOR.get() || client.level == null)
            return 0;

        if (!client.isSameThread()) {
            java.util.concurrent.CompletableFuture<Integer> future = new java.util.concurrent.CompletableFuture<>();
            client.execute(() -> {
                future.complete(getVisitorCount(client));
            });
            try {
                return future.get(1, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Exception e) {
                return 0;
            }
        }

        try {
            for (String line : TablistUtils.getRawTabLines(client)) {
                Matcher m = VISITORS_PATTERN.matcher(line);
                if (m.find()) {
                    return Integer.parseInt(m.group(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void handleVisitorScriptFinished(Minecraft client) {
        startVisitorReentryCooldown(client);
        menear.nclient.slate.util.ClientUtils.sendMessage(client, "\u00A7aVisitor sequence complete. Returning to farm...", true);
        MacroWorkerThread.getInstance().submit("VisitorFinished-ReturnToFarm", () -> {
            try {
                if (MacroWorkerThread.shouldAbortTask(client))
                    return;
                ClientUtils.sendDebugMessage(client, "Warping to garden...");
                menear.nclient.slate.util.CommandUtils.warpGarden(client);
                VisitorsMacro.reenableCompactorsIfPending(client);
                PestReturnManager.isReturningFromPestVisitor = true;
                if (MacroWorkerThread.shouldAbortTask(client))
                    return;
                ClientUtils.sendDebugMessage(client, "Finalizing return to farm...");
                finalizeReturnToFarm(client);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void finalizeReturnToFarm(Minecraft client) {
        if (MacroWorkerThread.shouldAbortTask(client))
            return;
        ClientUtils.sendDebugMessage(client,
                "finalizeReturnToFarm triggered. State: " + MacroStateManager.getCurrentState());
        if (MacroStateManager.getCurrentState() == MacroState.State.OFF)
            return;

        int visitors = getVisitorCount(client);
        if (visitors >= SlateConfig.VISITOR_THRESHOLD.get() && shouldSkipVisitorsDuringJacobsContest(client, true)) {
            ClientUtils.sendDebugMessage(client,
                    "Visitor threshold met, but Jacob's Contest window is active. Continuing farming.");
        } else if (visitors >= SlateConfig.VISITOR_THRESHOLD.get() && !isVisitorReentryCooldownActive(client, true)) {
            menear.nclient.slate.util.ClientUtils.sendMessage(client, "\u00A7eVisitor threshold met (" + visitors + "). Redirecting to Visitors...", true);
            
            client.execute(() -> menear.nclient.slate.modules.visitor.VisitorsMacro.start(client));
            return;
        }

        if (visitors >= SlateConfig.VISITOR_THRESHOLD.get()) {
            ClientUtils.sendDebugMessage(client,
                    "Visitor threshold met, but re-entry cooldown is active. Continuing farming.");
        }

        client.execute(() -> {
            GearManager.swapToFarmingTool(client);
        });
        MacroWorkerThread.sleep(250);

        if (SlateConfig.AUTO_LOADOUT_VISITOR.get() && SlateConfig.LOADOUT_SLOT_FARMING.get() > 0
                && LoadoutManager.trackedLoadoutSlot != SlateConfig.LOADOUT_SLOT_FARMING.get()) {
            menear.nclient.slate.util.ClientUtils.sendMessage(client, 
                    "\u00A7eRestoring farming loadout (slot " + SlateConfig.LOADOUT_SLOT_FARMING.get() + ")...", true);
            GearManager.ensureLoadoutSlot(client, SlateConfig.LOADOUT_SLOT_FARMING.get());
            if (LoadoutManager.isSwappingLoadout) {
                ClientUtils.sendDebugMessage(client, "finalizeReturnToFarm: Waiting for loadout GUI...");
                ClientUtils.waitForWardrobeGui(client);
                ClientUtils.sendDebugMessage(client,
                        "finalizeReturnToFarm: Loadout GUI detected, waiting for swap to complete...");
                while (LoadoutManager.isSwappingLoadout)
                    MacroWorkerThread.sleep(50);
                while (LoadoutManager.loadoutCleanupTicks > 0)
                    MacroWorkerThread.sleep(50);
                MacroWorkerThread.sleep(350);
                ClientUtils.sendDebugMessage(client, "finalizeReturnToFarm: Loadout swap fully complete.");
            }
        }


        ClientUtils.waitForGearAndGui(client);

        ClientUtils.waitForGearAndGui(client);
        PestReturnManager.isReturningFromPestVisitor = false;
        PestReturnManager.isReturnToLocationActive = false;
        restartFarmingAfterVisitors(client);
        PestPrepSwapManager.prepSwappedForCurrentPestCycle = false;
        PestManager.isCleaningInProgress = false;
    }

    private static void restartFarmingAfterVisitors(Minecraft client) {
        menear.nclient.slate.util.ClientUtils.sendMessage(client, "\u00A7aRestarting farming...", true);
        ClientUtils.sendDebugMessage(client, "Restarting farming macro after visitor sequence.");
        client.execute(() -> {
            if (client.player == null) {
                return;
            }

            menear.nclient.slate.macro.FarmingMacroManager.disable(client);
            menear.nclient.slate.macro.MacroStateManager.setCurrentState(menear.nclient.slate.macro.MacroState.State.FARMING);
            GearManager.swapToFarmingTool(client);
            menear.nclient.slate.macro.FarmingMacroManager.enable(client,
                    menear.nclient.slate.macro.FarmingMacroManager.createMacroFromConfig());
        });
    }

    // -- Visitor ROI: GUI Scanning --

    @SuppressWarnings("rawtypes")
    public static void scanVisitorGui(Minecraft client,
            net.minecraft.client.gui.screens.inventory.AbstractContainerScreen screen) {
        if (!MacroStateManager.isMacroRunning() || client.player == null)
            return;

        Component titleComp = screen.getTitle();
        String title = titleComp.getString().trim();

        // The accept button is usually in slot 29 of a 54-slot chest
        int slotIndex = 29;
        if (screen.getMenu().slots.size() <= slotIndex)
            return;

        net.minecraft.world.inventory.Slot slot = screen.getMenu().getSlot(slotIndex);
        if (slot == null || !slot.hasItem())
            return;

        ItemStack stack = slot.getItem();
        String name = stack.getHoverName().getString();

        if (!name.contains("Accept Offer"))
            return;

        VisitorOffer offer = new VisitorOffer();
        offer.visitorName = title;
        StringBuilder costBreakdown = new StringBuilder("\u00A7d[Slate] \u00A77Costs: ");

        List<Component> lore = ItemStackModelUtil.getLore(stack);
        if (lore.isEmpty()) {
            menear.nclient.slate.util.ClientUtils.sendMessage(client, "\u00A7cNo lore found on the Accept Offer button!", false);
            return;
        }
        boolean parsingRequirements = false;

        for (Component line : lore) {
            String text = line.getString().replaceAll("\u00A7[0-9a-fk-or]", "").trim();
            if (text.isEmpty())
                continue;

            if (text.contains("Items Required:")) {
                parsingRequirements = true;
                continue;
            }
            if (text.contains("Rewards:")) {
                parsingRequirements = false;
                continue;
            }

            if (parsingRequirements && !text.contains("Farming XP") && !text.contains("Garden Experience")) {
                parseRequirement(client, text, offer, stack, costBreakdown);
            }
        }

        if (offer.totalCost > 0) {
            pendingOffer = offer;
        }
    }

    // -- Helpers --

    private static String formatPrice(long price) {
        if (price >= 1_000_000)
            return String.format("%.1fM", price / 1_000_000.0);
        if (price >= 1_000)
            return String.format("%.1fk", price / 1_000.0);
        return String.valueOf(price);
    }

    private static void parseRequirement(Minecraft client, String text, VisitorOffer offer, ItemStack stack,
            StringBuilder breakdown) {
        // Handle "Enchanted Hay Bale x256" format
        Matcher m = Pattern.compile("(.+?)\\s+x(\\d+)$").matcher(text);
        String itemName;
        long count = 1;

        if (m.find()) {
            itemName = m.group(1).trim();
            count = Long.parseLong(m.group(2));
        } else {
            itemName = text.trim();
        }

        String id = resolveId(itemName, stack);
        double price = ProfitManager.getItemPrice(id != null ? id : itemName);

        if (price > 0) {
            long total = (long) (price * count);
            offer.totalCost += total;
            breakdown.append("\u00A7e").append(count).append("x ").append(itemName)
                    .append(" \u00A77(\u00A7c").append(formatPrice(total)).append("\u00A77), ");
        } else {
            breakdown.append("\u00A7c?x ").append(itemName).append(" (price unknown), ");
            System.out.println("[Slate] Unknown Visitor Cost Item: " + itemName + " (ID: " + id + ")");
        }
    }

    /**
     * Resolves a Skyblock Item ID from NBT custom data, or falls back to the
     * Cofl API search cache in ProfitManager.fetchIdByName.
     */
    @SuppressWarnings("unchecked")
    private static String resolveId(String name, ItemStack scannerStack) {
        // 1. Try NBT lookup from the "Accept Offer" stack's custom data
        try {
            CompoundTag tag = scannerStack.getTag();
            if (tag != null && tag.contains("ExtraAttributes", Tag.TAG_COMPOUND)) {
                CompoundTag ea = tag.getCompound("ExtraAttributes");
                if (ea.contains("id", Tag.TAG_STRING)) {
                    String nbtId = ea.getString("id");
                    if (!nbtId.isEmpty()) {
                        return nbtId;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Slate] NBT lookup failed for '" + name + "': " + e.getMessage());
        }

        // 2. Fallback: Cofl API name-based lookup with cache
        return ProfitManager.fetchIdByName(name);
    }

    public static void onOfferAccepted(String visitorName) {
        if (pendingOffer != null) {
            String cleanPending = TablistUtils.stripColors(pendingOffer.visitorName).trim();
            String cleanAccepted = TablistUtils.stripColors(visitorName).trim();

            // Lenient matching: "Moby" should match "Moby (RARE)"
            if (cleanAccepted.startsWith(cleanPending) || cleanPending.startsWith(cleanAccepted)) {
                ProfitManager.addVisitorCost(pendingOffer.totalCost);
                pendingOffer = null;
            }
        }
    }

    public static String extractAcceptedVisitorName(String plainText) {
        if (plainText == null) {
            return null;
        }

        Matcher matcher = OFFER_ACCEPTED_PATTERN.matcher(plainText.trim());
        if (!matcher.matches()) {
            return null;
        }

        return matcher.group(1).trim();
    }

    public static void clearPendingOffer() {
        pendingOffer = null;
    }

    public static void startVisitorReentryCooldown(Minecraft client) {
        visitorReentryCooldownUntilMs = System.currentTimeMillis() + VISITOR_REENTRY_COOLDOWN_MS;
        ClientUtils.sendDebugMessage(client, "Visitor re-entry cooldown started (1 minute).");
    }

    public static long getVisitorReentryCooldownRemainingMs() {
        return Math.max(0L, visitorReentryCooldownUntilMs - System.currentTimeMillis());
    }

    public static boolean isVisitorReentryCooldownActive(Minecraft client, boolean showMessage) {
        long now = System.currentTimeMillis();
        long remainingMs = visitorReentryCooldownUntilMs - now;
        if (remainingMs <= 0) {
            return false;
        }

        long remainingSeconds = (remainingMs + 999L) / 1000L;
        ClientUtils.sendDebugMessage(client,
                "Visitor re-entry cooldown active (" + remainingSeconds + "s remaining). Skipping visitor macro.");
        if (showMessage && client.player != null) {
            menear.nclient.slate.util.ClientUtils.sendMessage(client, "\u00A7eVisitor cooldown active (" + remainingSeconds + "s). Staying on farm.", true);
        }
        return true;
    }

    public static boolean shouldSkipVisitorsDuringJacobsContest(Minecraft client, boolean showMessage) {
        if (!SlateConfig.DISABLE_VISITORS_DURING_JACOBS_CONTEST.get()) {
            return false;
        }

        if (ClientUtils.getJacobsContestRemainingMs() <= 0) {
            return false;
        }

        ClientUtils.sendDebugMessage(client,
                "Jacob's Contest visitor skip window active (:15-:35). Skipping visitors.");
        if (showMessage && client.player != null) {
            ClientUtils.sendMessage(client,
                    "\u00A7eJacob's Contest window active (:15-:35). Skipping visitors.", true);
        }
        return true;
    }
}



