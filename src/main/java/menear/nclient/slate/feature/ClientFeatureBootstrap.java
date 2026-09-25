package menear.nclient.slate.feature;

import menear.nclient.slate.bootstrap.SlateChatEvents;
import menear.nclient.slate.bootstrap.SlateCommandRegistrar;
import menear.nclient.slate.bootstrap.SlateScreenHooks;
import menear.nclient.slate.bootstrap.SlateTickHandlers;
import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.config.ConfigManager;
import menear.nclient.slate.hud.HudRegistry;
import menear.nclient.slate.macro.MacroStateManager;
import menear.nclient.slate.macro.MacroWorkerThread;
import menear.nclient.slate.macro.ReconnectScheduler;
import menear.nclient.slate.modules.failsafe.FailsafeSoundManager;
import menear.nclient.slate.modules.misc.AutoCarnivalManager;
import menear.nclient.slate.modules.pathfinding.debug.PathVisualizer;
import menear.nclient.slate.modules.performance.MuteManager;
import menear.nclient.slate.modules.performance.PerformanceModeManager;
import menear.nclient.slate.modules.profit.ProfitManager;
import menear.nclient.slate.modules.visuals.StreamerModeManager;
import menear.nclient.slate.notification.NotificationManager;
import menear.nclient.slate.renderer.FunRenderer;
import menear.nclient.slate.renderer.PositionHighlighter;
import menear.nclient.slate.ui.theme.Theme;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;

import java.io.File;

public final class ClientFeatureBootstrap {
    private static boolean initialized;

    private ClientFeatureBootstrap() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        ConfigManager.init();
        SlateConfig.init();
        FailsafeSoundManager.init();
        Theme.loadTheme();
        ProfitManager.loadLifetime();
        ProfitManager.loadDaily();
        MacroStateManager.syncFromConfig();
        AutoCarnivalManager.syncFromConfig(Minecraft.getInstance());
        ReconnectScheduler.clearState();
        HudRegistry.register();
        MacroWorkerThread.getInstance().start();
        PathVisualizer.register();

        WorldRenderEvents.END.register(ctx -> {
            if (StreamerModeManager.isEnabled()) {
                return;
            }

            boolean drawPathVisualizer = PathVisualizer.shouldRender();
            boolean drawPositionHighlights = PositionHighlighter.hasVisibleHighlights();
            boolean drawFunEffects = FunRenderer.hasVisibleEffects();
            if (!drawPathVisualizer && !drawPositionHighlights && !drawFunEffects) {
                return;
            }
            if (drawPathVisualizer) {
                PathVisualizer.renderWorld();
            }
            if (drawPositionHighlights) {
                PositionHighlighter.renderWorld(ctx);
            }
            if (drawFunEffects) {
                FunRenderer.renderWorld(ctx);
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(PerformanceModeManager::stop);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> SlateConfig.flush());

        SlateScreenHooks.register();
        SlateChatEvents.register();
        SlateCommandRegistrar.register();
        SlateTickHandlers.register();

        initialized = true;
    }

    public static synchronized void shutdown() {
        // Commit any in-progress daily farming time before teardown: a macro left running
        // until the game closes never fires onMacroStop(), so this prevents the day's
        // un-committed time from being lost on exit.
        menear.nclient.slate.modules.session.DailyFarmTimeTracker.persistNow();
        PerformanceModeManager.stop(Minecraft.getInstance());
        NotificationManager.clearAll();
        HudRegistry.reset();
        PathVisualizer.clear();
        ReconnectScheduler.clearState();
        MacroWorkerThread.getInstance().cancelCurrent();
        MacroWorkerThread.getInstance().clearPendingTasks();
        initialized = false;
    }

    public static synchronized void onConfigProfileLoaded(File profileFile) {
        FailsafeSoundManager.refresh();
        MacroStateManager.syncFromConfig();
        AutoCarnivalManager.syncFromConfig(Minecraft.getInstance());

        Minecraft client = Minecraft.getInstance();
        PerformanceModeManager.stop(client);
        MuteManager.stop(client);
        if (MacroStateManager.isMacroRunning()) {
            PerformanceModeManager.start(client);
            MuteManager.start(client);
        }
    }

}

