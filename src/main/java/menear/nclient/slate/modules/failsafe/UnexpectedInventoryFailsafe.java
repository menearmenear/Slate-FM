package menear.nclient.slate.modules.failsafe;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.macro.MacroState;
import menear.nclient.slate.macro.MacroStateManager;
import menear.nclient.slate.modules.ComposterManager;
import menear.nclient.slate.modules.SupercraftManager;
import menear.nclient.slate.modules.gear.helpers.LoadoutManager;
import menear.nclient.slate.modules.inventorymanager.BookCombineManager;
import menear.nclient.slate.modules.inventorymanager.GeorgeManager;
import menear.nclient.slate.modules.pest.helpers.PestExchangeManager;
import menear.nclient.slate.modules.pest.helpers.PestTrapManager;
import menear.nclient.slate.modules.visitor.VisitorsMacro;
import menear.nclient.slate.notification.NotificationManager;
import menear.nclient.slate.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

final class UnexpectedInventoryFailsafe {
    enum State {
        IDLE,
        WAIT,
        TRIGGERED
    }

    private static volatile long inventoryOpenSince = 0L;
    private static volatile long inventoryOpenRandomDelayMs = 0L;
    private static volatile boolean triggered = false;

    private UnexpectedInventoryFailsafe() {
    }

    static void reset() {
        inventoryOpenSince = 0L;
        inventoryOpenRandomDelayMs = 0L;
        triggered = false;
    }

    static void tick(Minecraft client) {
        if (client == null || client.player == null) {
            reset();
            return;
        }

        if (!SlateConfig.FAILSAFE_UNEXPECTED_INVENTORY_GUI.get()) {
            inventoryOpenSince = 0L;
            inventoryOpenRandomDelayMs = 0L;
            triggered = false;
            return;
        }

        MacroState.State state = MacroStateManager.getCurrentState();
        boolean shouldMonitor = state == MacroState.State.FARMING || state == MacroState.State.CLEANING;
        if (!shouldMonitor) {
            inventoryOpenSince = 0L;
            inventoryOpenRandomDelayMs = 0L;
            triggered = false;
            return;
        }

        if (!ClientUtils.isInventoryScreenOpen(client)) {
            inventoryOpenSince = 0L;
            inventoryOpenRandomDelayMs = 0L;
            return;
        }

        if (isExpectedInventoryGuiOpen()) {
            inventoryOpenSince = 0L;
            inventoryOpenRandomDelayMs = 0L;
            triggered = false;
            return;
        }

        long now = System.currentTimeMillis();
        if (inventoryOpenSince == 0L) {
            inventoryOpenSince = now;
            inventoryOpenRandomDelayMs = FailsafeManager.sampleAdditionalTriggerDelayMs();
            return;
        }

        long triggerDelayMs = Math.round(SlateConfig.FAILSAFE_UNEXPECTED_INVENTORY_GUI_DELAY_SECONDS.get() * 1000.0f)
                + inventoryOpenRandomDelayMs;
        if (now - inventoryOpenSince < triggerDelayMs) {
            return;
        }

        trigger(client, state);
    }

    static State getState(Minecraft client) {
        if (triggered) {
            return State.TRIGGERED;
        }
        if (client == null || client.player == null || !SlateConfig.FAILSAFE_UNEXPECTED_INVENTORY_GUI.get()) {
            return State.IDLE;
        }

        MacroState.State state = MacroStateManager.getCurrentState();
        if ((state != MacroState.State.FARMING && state != MacroState.State.CLEANING)
                || !ClientUtils.isInventoryScreenOpen(client)
                || isExpectedInventoryGuiOpen()) {
            return State.IDLE;
        }

        return inventoryOpenSince == 0L ? State.IDLE : State.WAIT;
    }

    static long getTriggerRemainingMs() {
        if (inventoryOpenSince == 0L) {
            return 0L;
        }

        long triggerDelayMs = Math.round(SlateConfig.FAILSAFE_UNEXPECTED_INVENTORY_GUI_DELAY_SECONDS.get() * 1000.0f)
                + inventoryOpenRandomDelayMs;
        long elapsedMs = System.currentTimeMillis() - inventoryOpenSince;
        return Math.max(0L, triggerDelayMs - elapsedMs);
    }

    private static void trigger(Minecraft client, MacroState.State state) {
        if (triggered) {
            return;
        }

        FailsafeAction action = FailsafeManager.getUnexpectedInventoryGuiAction();
        triggered = true;
        NotificationManager.error(
                FailsafeManager.getNotificationTitle(action),
                "Unexpected inventory GUI detected.");
        FailsafeManager.handleConfiguredAction(
                client,
                action,
                FailsafeCustomReplayManager.FailsafeReplayType.GUI_OPENED,
                "unexpected inventory GUI detected during " + state.name().toLowerCase(java.util.Locale.ROOT) + ".",
                "UnexpectedInventoryFailsafe: unexpected inventory GUI detected during " + state.name());
        reset();
    }

    private static boolean isExpectedInventoryGuiOpen() {
        return LoadoutManager.isSwappingLoadout
                || PestExchangeManager.isExchanging
                || PestTrapManager.isRunning
                || ComposterManager.isRunning()
                || SupercraftManager.isRunning()
                || BookCombineManager.isPreparingToCombine
                || BookCombineManager.isCombining
                || isPestTrapGuiOpen()
                || VisitorsMacro.isRunning
                || GeorgeManager.isPreparingToSell
                || GeorgeManager.isSelling;
    }

    private static boolean isPestTrapGuiOpen() {
        if (!(Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen)) {
            return false;
        }

        String title = screen.getTitle().getString().toLowerCase(java.util.Locale.ROOT);
        return title.contains("trap");
    }
}
