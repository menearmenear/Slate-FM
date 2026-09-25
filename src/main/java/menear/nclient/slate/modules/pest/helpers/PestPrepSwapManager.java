package menear.nclient.slate.modules.pest.helpers;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.macro.MacroState;
import menear.nclient.slate.macro.MacroWorkerThread;
import menear.nclient.slate.modules.gear.GearManager;
import menear.nclient.slate.modules.gear.helpers.LoadoutManager;
import menear.nclient.slate.modules.pest.PestManager;
import menear.nclient.slate.util.ClientUtils;
import net.minecraft.client.Minecraft;

public class PestPrepSwapManager {
    public static volatile boolean prepSwappedForCurrentPestCycle = false;
    public static volatile boolean isPrepSwapping = false;

    public static void resetState() {
        prepSwappedForCurrentPestCycle = false;
        isPrepSwapping = false;
    }

    public static void updatePrepSwapFlag(int cooldownSeconds, boolean isCleaningInProgress) {
        if (cooldownSeconds > getPrepSwapResetCooldownSeconds()
                && prepSwappedForCurrentPestCycle
                && !isCleaningInProgress) {
            prepSwappedForCurrentPestCycle = false;
        }
    }

    public static boolean shouldTriggerPrepSwap(MacroState.State currentState, int cooldownSeconds,
            boolean isCleaningInProgress, boolean isReturnToLocationActive) {
        if (currentState != MacroState.State.FARMING) {
            return false;
        }
        if (cooldownSeconds == -1 || cooldownSeconds < 0) {
            return false;
        }
        if (prepSwappedForCurrentPestCycle || isCleaningInProgress || isReturnToLocationActive) {
            return false;
        }
        if (!hasAnyPrepSwapTasksEnabled()) {
            return false;
        }

        return cooldownSeconds <= getPrepSwapTriggerCooldownSeconds();
    }

    public static void triggerPrepSwap(Minecraft client) {
        prepSwappedForCurrentPestCycle = true;
        isPrepSwapping = true;
        ClientUtils.sendDebugMessage(client, "Pest cooldown detected. Triggering prep-swap...");
        MacroWorkerThread.getInstance().submit("PrepSwap", () -> {
            try {
                if (shouldAbortPrepSwap(client)) {
                    return;
                }
                ClientUtils.sendDebugMessage(client, "Disabling farming macro: Triggering prep-swap");
                client.execute(() -> menear.nclient.slate.macro.FarmingMacroManager.disable(client));
                MacroWorkerThread.sleep(400);
                if (shouldAbortPrepSwap(client)) {
                    return;
                }

                if (!runPrepLoadoutSwap(client)) {
                    return;
                }

                if (!PestManager.isCleaningInProgress) {
                    GearManager.finalResume(client);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isPrepSwapping = false;
            }
        });
    }

    private static boolean hasAnyPrepSwapTasksEnabled() {
        return SlateConfig.AUTO_LOADOUT_PEST.get();
    }

    private static int getPrepSwapTriggerCooldownSeconds() {
        if (SlateConfig.AUTO_LOADOUT_PEST.get()) {
            return SlateConfig.LOADOUT_PEST_SWAP_TIME_SECONDS.get();
        }
        return 3;
    }

    private static int getPrepSwapResetCooldownSeconds() {
        if (SlateConfig.AUTO_LOADOUT_PEST.get()) {
            return SlateConfig.LOADOUT_PEST_SWAP_TIME_SECONDS.get();
        }
        return 3;
    }

    private static boolean shouldAbortPrepSwap(Minecraft client) {
        if (MacroWorkerThread.shouldAbortTask(client, MacroState.State.FARMING) || PestManager.isCleaningInProgress) {
            if (menear.nclient.slate.macro.MacroStateManager.getCurrentState() != MacroState.State.FARMING
                    || !menear.nclient.slate.macro.MacroStateManager.isMacroRunning()) {
                prepSwappedForCurrentPestCycle = false;
            }
            return true;
        }
        return false;
    }

    private static boolean runPrepLoadoutSwap(Minecraft client) throws InterruptedException {
        if (!SlateConfig.AUTO_LOADOUT_PEST.get() || SlateConfig.LOADOUT_SLOT_PEST.get() <= 0) {
            return !shouldAbortPrepSwap(client);
        }

        ClientUtils.sendDebugMessage(client,
                "Prep-swap: Initiating loadout swap to slot " + SlateConfig.LOADOUT_SLOT_PEST.get());
        GearManager.ensureLoadoutSlot(client, SlateConfig.LOADOUT_SLOT_PEST.get());
        if (!LoadoutManager.isSwappingLoadout) {
            ClientUtils.sendDebugMessage(client, "Prep-swap: Loadout swap not needed (already on correct slot).");
            return !shouldAbortPrepSwap(client);
        }

        ClientUtils.sendDebugMessage(client, "Prep-swap: Waiting for loadout GUI...");
        ClientUtils.waitForWardrobeGui(client);
        if (!LoadoutManager.loadoutGuiDetected) {
            ClientUtils.sendDebugMessage(client, "\u00A7cPrep-swap: Loadout GUI not detected! Retrying in 1 second...");
            MacroWorkerThread.sleep(1000);
            if (shouldAbortPrepSwap(client)) {
                return false;
            }

            GearManager.ensureLoadoutSlot(client, SlateConfig.LOADOUT_SLOT_PEST.get());
            if (LoadoutManager.isSwappingLoadout) {
                ClientUtils.sendDebugMessage(client, "Prep-swap: Retry - Waiting for loadout GUI...");
                ClientUtils.waitForWardrobeGui(client);
                if (!LoadoutManager.loadoutGuiDetected) {
                    ClientUtils.sendDebugMessage(client,
                            "\u00A7cPrep-swap: Loadout GUI still not detected after retry! Aborting prep-swap.");
                    prepSwappedForCurrentPestCycle = false;
                    return false;
                }
            }
        }

        while (LoadoutManager.isSwappingLoadout && !PestManager.isCleaningInProgress) {
            MacroWorkerThread.sleep(50);
        }
        MacroWorkerThread.sleep(250);
        if (shouldAbortPrepSwap(client)) {
            return false;
        }

        ClientUtils.sendDebugMessage(client, "Prep-swap: Loadout swap completed.");
        return true;
    }
}
