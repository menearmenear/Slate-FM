package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.modules.failsafe.FailsafeCustomReplayManager.FailsafeReplayType;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;
import menear.nclient.slate.ui.settings.ToggleSetting;

import java.util.List;

public final class RotationFailsafeRegistryProvider extends AbstractFailsafesRegistryProvider {
    public RotationFailsafeRegistryProvider() {
        super(6);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        SettingGroup rotationGroup = SettingGroup.alwaysOn(
                        "Rotation Failsafe",
                        "Triggers when player rotation deviates beyond the configured thresholds")
                .add(FailsafeActionSettings.createActionDropdown("Rotation Action",
                        () -> SlateConfig.FAILSAFE_ROTATION_ACTION.get(),
                        value -> SlateConfig.FAILSAFE_ROTATION_ACTION.set(value)))
                .add(FailsafeActionSettings.createCustomReplayDropdown(FailsafeReplayType.ROTATION,
                        () -> SlateConfig.FAILSAFE_ROTATION_ACTION.get()))
                .add(new SliderSetting("Pitch Threshold", 5, 30,
                        () -> (float) SlateConfig.FAILSAFE_ROTATION_PITCH_THRESHOLD.get(),
                        v -> {
                            SlateConfig.FAILSAFE_ROTATION_PITCH_THRESHOLD.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix("\u00B0"))
                .add(new SliderSetting("Yaw Threshold", 5, 30,
                        () -> (float) SlateConfig.FAILSAFE_ROTATION_YAW_THRESHOLD.get(),
                        v -> {
                            SlateConfig.FAILSAFE_ROTATION_YAW_THRESHOLD.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix("\u00B0"))
                .add(new SliderSetting("Trigger Delay", 0, 5,
                        () -> SlateConfig.FAILSAFE_ROTATION_TRIGGER_DELAY_SECONDS.get(),
                        v -> {
                            SlateConfig.FAILSAFE_ROTATION_TRIGGER_DELAY_SECONDS.set(v);
                            SlateConfig.save();
                        })
                        .withDecimals(1).withSuffix("s"))
                .add(new SliderSetting("Warp Grace Period", 1000, 5000,
                        () -> (float) SlateConfig.FAILSAFE_ROTATION_WARP_GRACE_MS.get(),
                        v -> {
                            SlateConfig.FAILSAFE_ROTATION_WARP_GRACE_MS.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix("ms"));

        SettingGroup pestRotationGroup = SettingGroup.alwaysOn(
                        "Pest Cleaner Rotation",
                        "Controls how rotation failsafes behave while the pest cleaner is rotating")
                .add(new ToggleSetting("Trigger During Pest Cleaner",
                        () -> SlateConfig.FAILSAFE_ROTATION_TRIGGER_DURING_PEST_CLEANER.get(),
                        v -> {
                            SlateConfig.FAILSAFE_ROTATION_TRIGGER_DURING_PEST_CLEANER.set(v);
                            SlateConfig.save();
                        }))
                .add(FailsafeActionSettings.createActionDropdown("Pest Rotation Action",
                        () -> SlateConfig.FAILSAFE_PEST_ROTATION_ACTION.get(),
                        value -> SlateConfig.FAILSAFE_PEST_ROTATION_ACTION.set(value)))
                .add(FailsafeActionSettings.createCustomReplayDropdown(FailsafeReplayType.PEST_ROTATION,
                        () -> SlateConfig.FAILSAFE_PEST_ROTATION_ACTION.get()))
                .add(new SliderSetting("Pest Cleaner Rotation Failsafe Delay", 0, 5000,
                        () -> (float) SlateConfig.FAILSAFE_ROTATION_PEST_CLEANER_DELAY_MS.get(),
                        v -> {
                            SlateConfig.FAILSAFE_ROTATION_PEST_CLEANER_DELAY_MS.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix("ms")
                        .visibleWhen(() -> SlateConfig.FAILSAFE_ROTATION_TRIGGER_DURING_PEST_CLEANER.get()));

        return MainGUIRegistry.toggleSubTab(
                "Rotation",
                "Triggers when player rotation deviates beyond the configured thresholds",
                () -> SlateConfig.FAILSAFE_ROTATION.get(),
                v -> {
                    SlateConfig.FAILSAFE_ROTATION.set(v);
                    SlateConfig.save();
                },
                List.of(rotationGroup, pestRotationGroup));
    }
}
