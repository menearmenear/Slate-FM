package menear.nclient.slate.modules.pest.helpers;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.config.ConfigHelpers;
import menear.nclient.slate.macro.MacroState;
import menear.nclient.slate.macro.MacroStateManager;
import menear.nclient.slate.macro.MacroWorkerThread;
import menear.nclient.slate.modules.gear.helpers.LoadoutManager;
import menear.nclient.slate.modules.farming.SqueakyMousematManager;
import menear.nclient.slate.modules.pest.PestManager;
import menear.nclient.slate.util.ClientUtils;
import net.minecraft.client.Minecraft;

/**
 * Watches the pest tab-list bonus line and runs Phillip exchange when
 * "Bonus: INACTIVE" is detected while farming.
 */
public final class AutoPestExchangeManager {

    private static final long RUN_COOLDOWN_MS = 30_000L;

    private static volatile boolean running = false;
    private static volatile long bonusInactiveSinceMs = 0L;
    private static volatile long lastRunMs = 0L;
    private static volatile long pendingTriggerReadyAtMs = 0L;
    private static volatile boolean sawBonusInactive = false;
    private static volatile boolean pendingTrigger = false;

    private AutoPestExchangeManager() {}

    public static boolean isRunning() {
        return running;
    }

    public static long getBonusInactiveElapsedMs() {
        if (!PestBonusManager.isBonusInactive || bonusInactiveSinceMs == 0L) {
            return 0L;
        }
        return Math.max(0L, System.currentTimeMillis() - bonusInactiveSinceMs);
    }

    public static long getRunCooldownRemainingMs() {
        return Math.max(0L, RUN_COOLDOWN_MS - (System.currentTimeMillis() - lastRunMs));
    }

    public static void reset() {
        running = false;
        bonusInactiveSinceMs = 0L;
        lastRunMs = 0L;
        pendingTriggerReadyAtMs = 0L;
        sawBonusInactive = false;
        pendingTrigger = false;
    }

    public static boolean shouldBlockFarmingResume() {
        if (!SlateConfig.AUTO_PEST_EXCHANGE.get()) {
            return false;
        }
        if (running || PestExchangeManager.isExchanging) {
            return true;
        }
        if (!PestBonusManager.isBonusInactive || !pendingTrigger) {
            return false;
        }
        return isPendingTriggerReady(System.currentTimeMillis());
    }

    public static void update(Minecraft client) {
        if (client == null || client.player == null || client.getConnection() == null) return;
        if (!SlateConfig.AUTO_PEST_EXCHANGE.get()) {
            bonusInactiveSinceMs = 0L;
            pendingTriggerReadyAtMs = 0L;
            sawBonusInactive = false;
            pendingTrigger = false;
            return;
        }

        long now = System.currentTimeMillis();
        if (!PestBonusManager.isBonusInactive) {
            bonusInactiveSinceMs = 0L;
            pendingTriggerReadyAtMs = 0L;
            sawBonusInactive = false;
            pendingTrigger = false;
            return;
        }

        if (bonusInactiveSinceMs == 0L) {
            bonusInactiveSinceMs = now;
        }
        if (!sawBonusInactive) {
            sawBonusInactive = true;
            pendingTrigger = true;
            pendingTriggerReadyAtMs = now + ConfigHelpers.getRandomizedDelay(
                    SlateConfig.PEST_EXCHANGE_DELAY_MIN.get(),
                    SlateConfig.PEST_EXCHANGE_DELAY_MAX.get());
        }

        tryTriggerPending(client, now);
    }

    public static boolean tryTriggerPending(Minecraft client) {
        return tryTriggerPending(client, System.currentTimeMillis());
    }

    private static boolean tryTriggerPending(Minecraft client, long now) {
        if (client == null || client.player == null || client.getConnection() == null) {
            return false;
        }
        if (!SlateConfig.AUTO_PEST_EXCHANGE.get()) {
            return false;
        }
        if (running || PestExchangeManager.isExchanging) {
            return true;
        }
        if (PestManager.isCleaningInProgress) {
            return false;
        }

        MacroState.State state = MacroStateManager.getCurrentState();
        boolean stateAllowsPriority = state == MacroState.State.FARMING
                || state == MacroState.State.WARDROBE;
        if (!stateAllowsPriority || !pendingTrigger || !isPendingTriggerReady(now)) {
            return false;
        }

        if (LoadoutManager.isSwappingLoadout) {
            LoadoutManager.abortSwapForPriorityTask(client, "pest exchange");
        }
        if (MacroWorkerThread.getInstance().isBusy()) {
            MacroWorkerThread.getInstance().cancelCurrent();
            ClientUtils.sendDebugMessage(client,
                    "AutoPestExchange: cancelled queued worker tasks for priority.");
        }

        MacroStateManager.setCurrentState(MacroState.State.CLEANING);
        PestManager.isCleaningInProgress = true;
        running = true;
        lastRunMs = now;
        // Keep the trigger armed until the bonus actually flips back to active.
        // That lets the manager retry after cooldown if the first run fails or
        // the exchange completes without reactivating the bonus yet.
        MacroWorkerThread.getInstance().submit("AutoPestExchange", () -> runSequence(client));
        return true;
    }

    private static void runSequence(Minecraft client) {
        long guiDelay = Math.max(50L, menear.nclient.slate.util.ClientUtils.getGuiClickDelayMs(false));
        boolean canResumeFarming = true;

        try {
            if (MacroWorkerThread.shouldAbortTask(client))
                return;

            MacroStateManager.setCurrentState(MacroState.State.CLEANING);
            PestManager.isCleaningInProgress = true;

            msg(client, "\u00A7eBonus inactive detected. Running pest exchange...");
            client.execute(() -> menear.nclient.slate.macro.FarmingMacroManager.disable(client));
            MacroWorkerThread.sleep(guiDelay);

            if (MacroWorkerThread.shouldAbortTask(client))
                return;

            if (!SlateConfig.AUTO_PEST_USE_ABIPHONE.get()) {
                if (!menear.nclient.slate.util.CommandUtils.setSpawn(client)) {
                    msg(client, "\u00A7c/setspawn failed before pest exchange. Returning to farm.");
                    return;
                }
            }

            if (MacroWorkerThread.shouldAbortTask(client))
                return;

            PestExchangeManager.runExchangeBlocking(client);

            if (MacroWorkerThread.shouldAbortTask(client))
                return;

            if (!SlateConfig.AUTO_PEST_USE_ABIPHONE.get()) {
                if (!menear.nclient.slate.util.CommandUtils.warpGarden(client)) {
                    msg(client, "\u00A7c/warp garden failed after pest exchange.");
                    canResumeFarming = false;
                    return;
                }
            }

            MacroWorkerThread.sleep(guiDelay);
        } catch (Exception e) {
            e.printStackTrace();
            msg(client, "\u00A7cAuto pest exchange error: " + e.getMessage());
        } finally {
            running = false;
            if (MacroStateManager.isMacroRunning() && canResumeFarming) {
                if (!SlateConfig.AUTO_PEST_USE_ABIPHONE.get()) {
                    SqueakyMousematManager.armReapplyAttempt();
                }
                client.execute(() -> menear.nclient.slate.macro.FarmingMacroManager.enable(client,
                        menear.nclient.slate.macro.FarmingMacroManager.createMacroFromConfig()));
                MacroStateManager.setCurrentState(MacroState.State.FARMING);
            } else if (MacroStateManager.isMacroRunning() && !canResumeFarming) {
                msg(client, "\u00A7cAuto pest exchange did not resume farming because garden warp failed.");
            }
            PestManager.isCleaningInProgress = false;
            if (canResumeFarming && MacroStateManager.isMacroRunning()) {
                msg(client, "\u00A7aAuto pest exchange finished. Resuming farming.");
            }
        }
    }

    private static boolean isPendingTriggerReady(long now) {
        return pendingTriggerReadyAtMs > 0L
                && now >= pendingTriggerReadyAtMs
                && now - lastRunMs >= RUN_COOLDOWN_MS;
    }

    private static void msg(Minecraft client, String text) {
        menear.nclient.slate.util.ClientUtils.sendMessage(client, text);
    }
}
