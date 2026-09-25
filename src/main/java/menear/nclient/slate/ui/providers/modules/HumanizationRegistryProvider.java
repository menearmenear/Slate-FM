package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.config.HumanizationPresetManager;
import menear.nclient.slate.ui.settings.DropdownSetting;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;
import menear.nclient.slate.ui.settings.ToggleSetting;

import java.util.ArrayList;
import java.util.List;

public final class HumanizationRegistryProvider extends AbstractModulesRegistryProvider {
    public HumanizationRegistryProvider() {
        super(10);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        List<SettingGroup> groups = new ArrayList<>();

        groups.add(SettingGroup.alwaysOn(
                        "Presets",
                        "Apply editable humanization preset JSON files")
                .add(new DropdownSetting("Preset", HumanizationPresetManager.getPresetOptions(),
                        HumanizationPresetManager::getSelectedPresetIndex,
                        HumanizationPresetManager::applyPresetByIndex)
                        .addIconAction("/assets/slate/icons/folder.svg", HumanizationPresetManager::openPresetFolder)
                        .addIconAction("/assets/slate/icons/refresh.svg", HumanizationPresetManager::reapplySelectedPreset)));

        groups.add(SettingGroup.alwaysOn(
                        "Delays",
                        "Timing controls for macro and gear actions")
                .add(FarmingSettingsFactory.laneSwitchDelaySetting())
                .add(FarmingSettingsFactory.pestDestroyerTriggerDelaySetting())
                .add(FarmingSettingsFactory.pestExchangeDelaySetting())
                .add(FarmingSettingsFactory.aotvBetweenPestsDelaySetting())
                .add(FarmingSettingsFactory.rodSwapDelaySetting())
                .add(FarmingSettingsFactory.guiFirstClickDelaySetting())
                .add(FarmingSettingsFactory.guiClickDelaySetting())
                .add(FarmingSettingsFactory.pickUpStashDelaySetting())
                .add(FarmingSettingsFactory.junkDropDelaySetting())
                .add(FarmingSettingsFactory.georgePostSellDelaySetting())
                .add(FarmingSettingsFactory.bazaarGuiDelaySetting()));

        groups.add(SettingGroup.alwaysOn(
                        "Rotation Settings",
                        "Configure how your character rotates while macroing")
                .add(new SliderSetting("Rotation Time", 100, 2000,
                        () -> (float) SlateConfig.ROTATION_TIME.get(),
                        v -> {
                            SlateConfig.ROTATION_TIME.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix("ms"))
                .add(new SliderSetting("Dynamic Duration", 0, 20,
                        () -> SlateConfig.ROTATION_DYNAMIC_DURATION_MS_PER_DEGREE.get(),
                        v -> {
                            SlateConfig.ROTATION_DYNAMIC_DURATION_MS_PER_DEGREE.set(v);
                            SlateConfig.save();
                        })
                        .withDecimals(1).withSuffix(" ms/\u00B0"))
                .add(new ToggleSetting("Rotation Ease In",
                        () -> SlateConfig.ROTATION_EASE_IN.get(),
                        v -> {
                            SlateConfig.ROTATION_EASE_IN.set(v);
                            SlateConfig.save();
                        }))
                .add(new SliderSetting("Ease In Factor", 1, 5,
                        () -> SlateConfig.ROTATION_EASE_IN_FACTOR.get(),
                        v -> {
                            SlateConfig.ROTATION_EASE_IN_FACTOR.set(v);
                            SlateConfig.save();
                        })
                        .withDecimals(1)
                        .visibleWhen(() -> SlateConfig.ROTATION_EASE_IN.get()))
                .add(new ToggleSetting("Rotation Ease Out",
                        () -> SlateConfig.ROTATION_EASE_OUT.get(),
                        v -> {
                            SlateConfig.ROTATION_EASE_OUT.set(v);
                            SlateConfig.save();
                        }))
                .add(new SliderSetting("Ease Out Factor", 1, 5,
                        () -> SlateConfig.ROTATION_EASE_OUT_FACTOR.get(),
                        v -> {
                            SlateConfig.ROTATION_EASE_OUT_FACTOR.set(v);
                            SlateConfig.save();
                        })
                        .withDecimals(1)
                        .visibleWhen(() -> SlateConfig.ROTATION_EASE_OUT.get()))
                .add(new SliderSetting("Tracking Noise Min", 0, 10,
                        () -> SlateConfig.ROTATION_TRACKING_NOISE_MIN.get(),
                        v -> {
                            SlateConfig.ROTATION_TRACKING_NOISE_MIN.set(v);
                            if (SlateConfig.ROTATION_TRACKING_NOISE_MAX.get() < v) {
                                SlateConfig.ROTATION_TRACKING_NOISE_MAX.set(v);
                            }
                            SlateConfig.save();
                        })
                        .withDecimals(1).withSuffix("%"))
                .add(new SliderSetting("Tracking Noise Max", 0, 10,
                        () -> SlateConfig.ROTATION_TRACKING_NOISE_MAX.get(),
                        v -> {
                            SlateConfig.ROTATION_TRACKING_NOISE_MAX.set(v);
                            if (SlateConfig.ROTATION_TRACKING_NOISE_MIN.get() > v) {
                                SlateConfig.ROTATION_TRACKING_NOISE_MIN.set(v);
                            }
                            SlateConfig.save();
                        })
                        .withDecimals(1).withSuffix("%")));

        groups.add(SettingGroup.alwaysOn(
                        "FOV/Rotation",
                        "Angle ranges and target tolerance settings")
                .add(FarmingSettingsFactory.farmingPitchRangeSetting())
                .add(FarmingSettingsFactory.farmingYawRangeSetting())
                .add(FarmingSettingsFactory.aotvToRoofPitchRangeSetting())
                .add(FarmingSettingsFactory.pestFovRangeSetting())
                .add(FarmingSettingsFactory.pestAboveAimPitchRangeSetting())
                .add(FarmingSettingsFactory.visitorFovRangeSetting())
                .add(FarmingSettingsFactory.pestExchangeFovRangeSetting()));

        groups.add(SettingGroup.alwaysOn(
                        "Miscellaneous",
                        "Additional behavior toggles for humanization-sensitive actions")
                .add(FarmingSettingsFactory.farmWhileCallingGeorgeSetting()));

        return MainGUIRegistry.subTab(
                "Humanization",
                "Humanization controls for pitch, yaw, and easing",
                groups);
    }
}
