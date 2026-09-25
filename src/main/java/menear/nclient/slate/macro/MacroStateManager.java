package menear.nclient.slate.macro;

import net.minecraft.client.Minecraft;
import menear.nclient.slate.util.ClientUtils;
import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.modules.failsafe.FailsafeManager;
import menear.nclient.slate.modules.metaldetector.MetalDetectorSolver;
import menear.nclient.slate.modules.misc.AutoCarnivalManager;
import menear.nclient.slate.modules.pathfinding.PathfindingManager;
import menear.nclient.slate.modules.session.DailyFarmTimeTracker;

public class MacroStateManager {
    private static volatile MacroState.State currentState = MacroState.State.OFF;
    private static volatile boolean intentionalDisconnect = false;
    private static volatile long sessionAccumulated = 0;
    private static volatile long lifetimeAccumulated = 0;
    private static volatile long lastSessionStartTime = 0;
    private static long lastPeriodicSaveTime = 0;

    public static void resetSession() {
        if (isMacroRunning()) {
            lastSessionStartTime = System.currentTimeMillis();
        } else {
            lastSessionStartTime = 0;
        }
        sessionAccumulated = 0;
        menear.nclient.slate.modules.profit.ProfitManager.reset();
        AutoCarnivalManager.resetTokenSession();
        menear.nclient.slate.modules.session.DynamicRestManager.reset();
        menear.nclient.slate.util.BpsTracker.reset();
    }

    public static void syncFromConfig() {
        lifetimeAccumulated = (long) (double) SlateConfig.LIFETIME_ACCUMULATED.get();
        DailyFarmTimeTracker.syncFromConfig();
    }

    public static void periodicUpdate() {
        if (currentState == MacroState.State.OFF || currentState == MacroState.State.RECOVERING)
            return;

        long now = System.currentTimeMillis();
        if (lastSessionStartTime <= 0) {
            lastPeriodicSaveTime = now;
            return;
        }
        if (now - lastPeriodicSaveTime > 60000) { // 1 minute
            lastPeriodicSaveTime = now;
            long diff = Math.max(0L, now - lastSessionStartTime);

            if (SlateConfig.PERSIST_SESSION_TIMER.get()) {
                // Keep session timer as is for pause/unpause if enabled
            } else {
                // Not actually hit here since we're periodic other than if someone pauses?
                // Wait, sessionAccumulated is only saved to disk if we want it to survive
                // RESTART
            }
            DailyFarmTimeTracker.periodicSave();
            SlateConfig.LIFETIME_ACCUMULATED.set((double) (lifetimeAccumulated + diff));
            SlateConfig.save();
        }
    }

    public static long getSessionRunningTime() {
        if (currentState != MacroState.State.OFF && currentState != MacroState.State.RECOVERING
                && lastSessionStartTime != 0) {
            return sessionAccumulated + (System.currentTimeMillis() - lastSessionStartTime);
        }
        return sessionAccumulated;
    }

    public static long getLifetimeRunningTime() {
        if (currentState != MacroState.State.OFF && currentState != MacroState.State.RECOVERING
                && lastSessionStartTime != 0) {
            return lifetimeAccumulated + (System.currentTimeMillis() - lastSessionStartTime);
        }
        return lifetimeAccumulated;
    }

    public static boolean isMacroRunning() {
        return currentState != MacroState.State.OFF;
    }

    public static boolean isIntentionalDisconnect() {
        return intentionalDisconnect;
    }

    public static void setIntentionalDisconnect(boolean intentional) {
        intentionalDisconnect = intentional;
    }

    public static MacroState.State getCurrentState() {
        return currentState;
    }

    public static void setCurrentState(MacroState.State state) {
        MacroState.State prevState = currentState;
        currentState = state;
        Minecraft client = Minecraft.getInstance();

        if (state == MacroState.State.FARMING && prevState != MacroState.State.FARMING) {
            MacroWorkerThread.getInstance().clearPendingTasks();
            PathfindingManager.stop();
        }

        if (prevState == MacroState.State.OFF && state != MacroState.State.OFF
                && state != MacroState.State.RECOVERING) {
            lastSessionStartTime = System.currentTimeMillis();
            DailyFarmTimeTracker.onMacroStart();
            if (!SlateConfig.PERSIST_SESSION_TIMER.get()) {
                sessionAccumulated = 0;
                menear.nclient.slate.modules.profit.ProfitManager.reset();
                AutoCarnivalManager.resetTokenSession();
            }
            lastPeriodicSaveTime = System.currentTimeMillis();
        } else if (prevState == MacroState.State.RECOVERING && state != MacroState.State.OFF
                && state != MacroState.State.RECOVERING) {
            lastSessionStartTime = System.currentTimeMillis();
            DailyFarmTimeTracker.onMacroStart();
            FailsafeManager.syncExpectedRotationFromClient(client);
            FailsafeManager.addRotationGracePeriod(SlateConfig.FAILSAFE_ROTATION_WARP_GRACE_MS.get());
        } else if (prevState != MacroState.State.OFF && prevState != MacroState.State.RECOVERING
                && (state == MacroState.State.OFF || state == MacroState.State.RECOVERING)) {
            if (lastSessionStartTime != 0) {
                long diff = System.currentTimeMillis() - lastSessionStartTime;
                sessionAccumulated += diff;
                lifetimeAccumulated += diff;
                lastSessionStartTime = 0;

                SlateConfig.LIFETIME_ACCUMULATED.set((double) lifetimeAccumulated);
                SlateConfig.save();
            }
            DailyFarmTimeTracker.onMacroStop();
        }

        // Mouse Grab/Ungrab Logic
        if (state != MacroState.State.OFF) {
            runOnClientThread(client, () -> {
                if (SlateConfig.MACRO_UNGRAB_MOUSE.get()) {
                    menear.nclient.slate.modules.farming.UngrabMouse.requestMacroUngrab();
                }
                menear.nclient.slate.modules.performance.PerformanceModeManager.start(client);
                menear.nclient.slate.modules.performance.MuteManager.start(client);
            });
        } else {
            runOnClientThread(client, () -> {
                menear.nclient.slate.modules.farming.UngrabMouse.clearMacroUngrab();
                menear.nclient.slate.modules.performance.PerformanceModeManager.stop(client);
                menear.nclient.slate.modules.performance.MuteManager.stop(client);
            });
        }
    }

    public static void stopMacro(Minecraft client) {
        stopMacro(client, "Macro stopped by user");
    }

    public static void stopMacro(Minecraft client, String debugReason) {
        stopMacro(client, debugReason, true);
    }

    public static void stopMacro(Minecraft client, String debugReason, boolean closeScreen) {
        MacroWorkerThread.getInstance().cancelCurrent();
        // Stop any active internal farming macro.
        runOnClientThread(client, () -> FarmingMacroManager.disable(client));
        MetalDetectorSolver.stopForMacro(client);
        AutoCarnivalManager.stopForMacro(client);
        FailsafeManager.reset();
        menear.nclient.slate.modules.farming.SqueakyMousematManager.clearReapplyAttempt();
        if (client != null) {
            client.execute(() -> {
                if (closeScreen && client.screen != null) {
                    client.setScreen(null);
                }
                menear.nclient.slate.modules.farming.UngrabMouse.clearMacroUngrab();
            });
        }
        setCurrentState(MacroState.State.OFF);
        ClientUtils.forceReleaseKeys(client);
        ClientUtils.sendDebugMessage(client, debugReason);
        menear.nclient.slate.modules.pest.PestManager.reset();
        menear.nclient.slate.modules.pest.helpers.PestExchangeManager.stop();
        menear.nclient.slate.modules.pest.helpers.PestDestroyer.stop(client);
        menear.nclient.slate.modules.pest.helpers.PestTrapManager.cancel(client);
        menear.nclient.slate.modules.inventorymanager.AutoSellManager.cancel(client);
        menear.nclient.slate.modules.pest.helpers.AutoSprayonatorManager.cancel();
        menear.nclient.slate.modules.pest.helpers.AutoSprayonatorManager.reset();
        menear.nclient.slate.modules.pest.helpers.AutoPestExchangeManager.reset();
        menear.nclient.slate.modules.GreenhouseManager.reset();
        menear.nclient.slate.modules.ComposterManager.reset();
        menear.nclient.slate.modules.SupercraftManager.reset();
        menear.nclient.slate.modules.gear.GearManager.reset();
        menear.nclient.slate.modules.inventorymanager.GeorgeManager.reset();
        menear.nclient.slate.modules.inventorymanager.BookCombineManager.reset();
        menear.nclient.slate.modules.inventorymanager.JunkManager.reset();
        menear.nclient.slate.modules.session.RecoveryManager.reset();
        menear.nclient.slate.modules.session.RestartManager.reset();
        if (!SlateConfig.PERSIST_SESSION_TIMER.get()) {
            menear.nclient.slate.modules.session.DynamicRestManager.reset();
            menear.nclient.slate.modules.profit.ProfitManager.reset();
            AutoCarnivalManager.resetTokenSession();
        }
        ReconnectScheduler.cancel();
        menear.nclient.slate.modules.pathfinding.PathfindingManager.stop();
        menear.nclient.slate.modules.visitor.VisitorsMacro.stop(client);
    }

    private static void runOnClientThread(Minecraft client, Runnable action) {
        if (client == null || action == null) {
            return;
        }
        if (client.isSameThread()) {
            action.run();
            return;
        }
        client.execute(action);
    }
}
