package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.macro.MacroStateManager;
import menear.nclient.slate.modules.session.DailyFarmTimeTracker;
import menear.nclient.slate.modules.session.DynamicRestManager;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.ActionSetting;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;
import menear.nclient.slate.ui.settings.ToggleSetting;

import java.util.List;

public final class DynamicRestRegistryProvider extends AbstractModulesRegistryProvider {
    public DynamicRestRegistryProvider() {
        super(8);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        SettingGroup group = SettingGroup.alwaysOn(
                        "Dynamic Rest",
                        "Schedules automatic breaks during scripting")
                .add(new SliderSetting("Scripting Time", 1, 600,
                        () -> (float) SlateConfig.REST_SCRIPTING_TIME.get(),
                        v -> {
                            SlateConfig.REST_SCRIPTING_TIME.set(Math.round(v));
                            SlateConfig.save();
                            DynamicRestManager.refreshCurrentSession();
                        })
                        .withDecimals(0).withSuffix(" min"))
                .add(new SliderSetting("Scripting Offset", 0, 300,
                        () -> (float) SlateConfig.REST_SCRIPTING_TIME_OFFSET.get(),
                        v -> {
                            SlateConfig.REST_SCRIPTING_TIME_OFFSET.set(Math.round(v));
                            SlateConfig.save();
                            DynamicRestManager.refreshCurrentSession();
                        })
                        .withDecimals(0).withSuffix(" min"))
                .add(new SliderSetting("Break Time", 1, 600,
                        () -> (float) SlateConfig.REST_BREAK_TIME.get(),
                        v -> {
                            SlateConfig.REST_BREAK_TIME.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix(" min"))
                .add(new SliderSetting("Break Offset", 0, 300,
                        () -> (float) SlateConfig.REST_BREAK_TIME_OFFSET.get(),
                        v -> {
                            SlateConfig.REST_BREAK_TIME_OFFSET.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix(" min"))
                .add(new SliderSetting("Daily Threshold", 0, 24,
                        () -> (float) (double) SlateConfig.DAILY_FARM_THRESHOLD_HOURS.get(),
                        v -> {
                            SlateConfig.DAILY_FARM_THRESHOLD_HOURS.set((double) Math.max(0f, Math.min(24f, v)));
                            SlateConfig.save();
                        })
                        .withDecimals(1).withSuffix(" hr"))
                .add(new ToggleSetting("Close Game On Daily Threshold",
                        () -> SlateConfig.CLOSE_GAME_ON_DAILY_THRESHOLD.get(),
                        v -> {
                            SlateConfig.CLOSE_GAME_ON_DAILY_THRESHOLD.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> SlateConfig.DAILY_FARM_THRESHOLD_HOURS.get() > 0.0))
                .add(new ActionSetting("Reset Daily Timer", DailyFarmTimeTracker::resetToday));

        return MainGUIRegistry.toggleSubTab(
                "Dynamic Rest",
                "Schedules automatic breaks during scripting",
                () -> SlateConfig.DYNAMIC_REST_ENABLED.get(),
                DynamicRestRegistryProvider::setDynamicRestEnabled,
                List.of(group));
    }

    private static void setDynamicRestEnabled(boolean enabled) {
        SlateConfig.DYNAMIC_REST_ENABLED.set(enabled);
        SlateConfig.save();
        if (!enabled) {
            DynamicRestManager.reset();
        } else if (MacroStateManager.isMacroRunning()) {
            DynamicRestManager.scheduleNextRest();
        }
    }
}
