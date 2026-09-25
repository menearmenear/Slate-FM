package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.modules.failsafe.FailsafeCustomReplayManager.FailsafeReplayType;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;

import java.util.List;

public final class BpsFailsafeRegistryProvider extends AbstractFailsafesRegistryProvider {
    public BpsFailsafeRegistryProvider() {
        super(2);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        SettingGroup group = SettingGroup.alwaysOn(
                        "BPS Failsafe",
                        "Triggers when block breaks per second fall below the configured threshold")
                .add(FailsafeActionSettings.createActionDropdown("Action",
                        () -> SlateConfig.FAILSAFE_BPS_ACTION.get(),
                        value -> SlateConfig.FAILSAFE_BPS_ACTION.set(value)))
                .add(FailsafeActionSettings.createCustomReplayDropdown(FailsafeReplayType.BPS,
                        () -> SlateConfig.FAILSAFE_BPS_ACTION.get()))
                .add(new SliderSetting("Threshold", 5, 15,
                        () -> (float) SlateConfig.FAILSAFE_BPS_THRESHOLD.get(),
                        v -> {
                            SlateConfig.FAILSAFE_BPS_THRESHOLD.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0))
                .add(new SliderSetting("Window", 5, 30,
                        () -> (float) SlateConfig.FAILSAFE_BPS_WINDOW_SECONDS.get(),
                        v -> {
                            SlateConfig.FAILSAFE_BPS_WINDOW_SECONDS.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix("s"))
                .add(new SliderSetting("Trigger Delay", 0, 5,
                        () -> SlateConfig.FAILSAFE_BPS_TRIGGER_DELAY_SECONDS.get(),
                        v -> {
                            SlateConfig.FAILSAFE_BPS_TRIGGER_DELAY_SECONDS.set(v);
                            SlateConfig.save();
                        })
                        .withDecimals(1).withSuffix("s"));

        return MainGUIRegistry.toggleSubTab(
                "BPS",
                "Triggers when block breaks per second fall below the configured threshold",
                () -> SlateConfig.FAILSAFE_BPS.get(),
                v -> {
                    SlateConfig.FAILSAFE_BPS.set(v);
                    SlateConfig.save();
                },
                List.of(group));
    }
}
