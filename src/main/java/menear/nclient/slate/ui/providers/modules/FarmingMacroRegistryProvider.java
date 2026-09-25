package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.config.FarmingMacroPresetManager;
import menear.nclient.slate.config.FarmType;
import menear.nclient.slate.ui.settings.ActionSetting;
import menear.nclient.slate.ui.settings.DropdownSetting;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;
import menear.nclient.slate.ui.settings.TextSetting;
import menear.nclient.slate.ui.settings.ToggleSetting;
import menear.nclient.slate.util.SlateLang;

import java.util.ArrayList;
import java.util.List;

public final class FarmingMacroRegistryProvider extends AbstractModulesRegistryProvider {
    public FarmingMacroRegistryProvider() {
        super(0);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        List<SettingGroup> groups = new ArrayList<>();
        groups.add(SettingGroup.alwaysOn(
                        "Presets",
                        "Save and load Farming Macro preset JSON files")
                .add(new TextSetting("Preset Name", "e.g. Wheat",
                        () -> SlateConfig.FARMING_MACRO_PRESET_NAME.get(),
                        v -> {
                            SlateConfig.FARMING_MACRO_PRESET_NAME.set(v);
                            SlateConfig.save();
                        }))
                .add(new ActionSetting("Save Preset", FarmingMacroPresetManager::saveCurrentPreset))
                .add(new DropdownSetting("Preset", FarmingMacroPresetManager.getPresetOptions(),
                        FarmingMacroPresetManager::getSelectedPresetIndex,
                        FarmingMacroPresetManager::applyPresetByIndex)
                        .addIconAction("/assets/slate/icons/folder.svg", FarmingMacroPresetManager::openPresetFolder)
                        .addIconAction("/assets/slate/icons/refresh.svg", FarmingMacroPresetManager::refreshPresetOptions)));

        groups.add(SettingGroup.alwaysOn(
                        "Farm Macro Settings",
                        "Configure Farming Macro behavior")
                .add(new DropdownSetting("Farm Type",
                        java.util.stream.Stream.of(FarmType.values()).map(FarmType::getLabel).toList(),
                        () -> {
                            try {
                                return FarmType.valueOf(SlateConfig.FARM_TYPE.get()).ordinal();
                            } catch (Exception e) {
                                return 0;
                            }
                        },
                        i -> {
                            if (i >= 0 && i < FarmType.values().length) {
                                SlateConfig.FARM_TYPE.set(FarmType.values()[i].name());
                                SlateConfig.save();
                            }
                        }))
                .add(new ToggleSetting("Hold W While Farming",
                        () -> SlateConfig.MACRO_HOLD_W_WHILE_FARMING.get(),
                        v -> {
                            SlateConfig.MACRO_HOLD_W_WHILE_FARMING.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting(SlateLang.localize("Disable /setspawn"),
                        () -> SlateConfig.MACRO_DISABLE_SETSPAWN.get(),
                        v -> {
                            SlateConfig.MACRO_DISABLE_SETSPAWN.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Rotate on Drop",
                        () -> SlateConfig.MACRO_ROTATE_ON_DROP.get(),
                        v -> {
                            SlateConfig.MACRO_ROTATE_ON_DROP.set(v);
                            SlateConfig.save();
                        }))
                .add(new SliderSetting("Drop Rotation", -180, 180,
                        () -> (float) SlateConfig.MACRO_DROP_ROTATION_DEGREES.get(),
                        v -> {
                            SlateConfig.MACRO_DROP_ROTATION_DEGREES.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix("\u00B0")
                        .visibleWhen(() -> SlateConfig.MACRO_ROTATE_ON_DROP.get()))
                .add(new ToggleSetting("Squeaky Mousemat",
                        () -> SlateConfig.SQUEAKY_MOUSEMAT.get(),
                        v -> {
                            SlateConfig.SQUEAKY_MOUSEMAT.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Custom Pitch",
                        () -> SlateConfig.MACRO_USE_CUSTOM_PITCH.get(),
                        v -> {
                            SlateConfig.MACRO_USE_CUSTOM_PITCH.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> !SlateConfig.SQUEAKY_MOUSEMAT.get()))
                .add(new SliderSetting("Pitch", -90, 90,
                        () -> SlateConfig.MACRO_CUSTOM_PITCH.get(),
                        v -> {
                            SlateConfig.MACRO_CUSTOM_PITCH.set(v);
                            SlateConfig.save();
                        })
                        .withDecimals(1).withSuffix("\u00B0")
                        .visibleWhen(() -> !SlateConfig.SQUEAKY_MOUSEMAT.get()
                                && SlateConfig.MACRO_USE_CUSTOM_PITCH.get()))
                .add(new ToggleSetting("Custom Yaw",
                        () -> SlateConfig.MACRO_USE_CUSTOM_YAW.get(),
                        v -> {
                            SlateConfig.MACRO_USE_CUSTOM_YAW.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> !SlateConfig.SQUEAKY_MOUSEMAT.get()))
                .add(new SliderSetting("Yaw", -180, 180,
                        () -> SlateConfig.MACRO_CUSTOM_YAW.get(),
                        v -> {
                            SlateConfig.MACRO_CUSTOM_YAW.set(v);
                            SlateConfig.save();
                        })
                        .withDecimals(1).withSuffix("\u00B0")
                        .visibleWhen(() -> !SlateConfig.SQUEAKY_MOUSEMAT.get()
                                && SlateConfig.MACRO_USE_CUSTOM_YAW.get()))
                .add(FarmingSettingsFactory.farmingPitchRangeSetting()
                        .visibleWhen(() -> !SlateConfig.SQUEAKY_MOUSEMAT.get()))
                .add(FarmingSettingsFactory.farmingYawRangeSetting()
                        .visibleWhen(() -> !SlateConfig.SQUEAKY_MOUSEMAT.get()))
                .add(FarmingSettingsFactory.bpsAverageWindowSetting()));

        groups.add(SettingGroup.of(
                        "Fast Lane Switch (Experimental)",
                        "Switches farming direction at configured plot lane boundaries",
                        () -> SlateConfig.MACRO_FAST_LANE_SWITCH.get(),
                        v -> {
                            SlateConfig.MACRO_FAST_LANE_SWITCH.set(v);
                            SlateConfig.save();
                        })
                .add(new DropdownSetting("Boundary Axis",
                        List.of("X", "Z"),
                        () -> "Z".equalsIgnoreCase(SlateConfig.MACRO_FAST_LANE_BOUNDARY_AXIS.get()) ? 1 : 0,
                        i -> {
                            SlateConfig.MACRO_FAST_LANE_BOUNDARY_AXIS.set(i == 1 ? "Z" : "X");
                            SlateConfig.save();
                        }))
                .add(new TextSetting("Left Boundary", "e.g. -48",
                        () -> String.valueOf(SlateConfig.MACRO_FAST_LANE_LEFT_BOUNDARY.get()),
                        v -> {
                            Integer parsed = parseBoundary(v);
                            if (parsed != null) {
                                SlateConfig.MACRO_FAST_LANE_LEFT_BOUNDARY.set(parsed);
                                SlateConfig.save();
                            }
                        }))
                .add(new TextSetting("Right Boundary", "e.g. 48 blocks",
                        () -> String.valueOf(SlateConfig.MACRO_FAST_LANE_RIGHT_BOUNDARY.get()),
                        v -> {
                            Integer parsed = parseBoundary(v);
                            if (parsed != null) {
                                SlateConfig.MACRO_FAST_LANE_RIGHT_BOUNDARY.set(parsed);
                                SlateConfig.save();
                            }
                        })));

        return MainGUIRegistry.subTab("Farming Macro", "Automatically farms crops", groups);
    }

    private static Integer parseBoundary(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
