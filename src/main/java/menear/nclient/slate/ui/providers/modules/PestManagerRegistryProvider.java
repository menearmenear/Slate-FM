package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.ui.settings.DropdownSetting;
import menear.nclient.slate.ui.settings.ListSetting;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;
import menear.nclient.slate.ui.settings.TextSetting;
import menear.nclient.slate.ui.settings.ToggleSetting;

import java.util.ArrayList;
import java.util.List;

public final class PestManagerRegistryProvider extends AbstractModulesRegistryProvider {
    public PestManagerRegistryProvider() {
        super(2);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        List<String> sprayMaterials = FarmingSettingsFactory.sprayMaterials();
        List<SettingGroup> groups = new ArrayList<>();

        groups.add(SettingGroup.of(
                        "Pest Destroyer",
                        "Cleans pests once past the threshold",
                        () -> SlateConfig.TRIGGER_PEST_ON_CHAT.get(),
                        v -> {
                            SlateConfig.TRIGGER_PEST_ON_CHAT.set(v);
                            SlateConfig.save();
                        })
                .add(new SliderSetting("Pest Threshold", 1, 8,
                        () -> (float) SlateConfig.PEST_THRESHOLD.get(),
                        v -> {
                            SlateConfig.PEST_THRESHOLD.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0))
                .add(new ToggleSetting("Skip while Crop Fever Active",
                        () -> SlateConfig.DELAY_PEST_FOR_CROP_FEVER.get(),
                        v -> {
                            SlateConfig.DELAY_PEST_FOR_CROP_FEVER.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Trigger Only After Rewarp",
                        () -> SlateConfig.PEST_TRIGGER_ONLY_AFTER_REWARP.get(),
                        v -> {
                            SlateConfig.PEST_TRIGGER_ONLY_AFTER_REWARP.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Plot TP for Current Plot",
                        () -> SlateConfig.PEST_PLOT_TP_FOR_CURRENT_PLOT.get(),
                        v -> {
                            SlateConfig.PEST_PLOT_TP_FOR_CURRENT_PLOT.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Leave One Pest Alive",
                        () -> SlateConfig.LEAVE_ONE_PEST_ALIVE.get(),
                        v -> {
                            SlateConfig.LEAVE_ONE_PEST_ALIVE.set(v);
                            SlateConfig.save();
                        }))
                .add(new ListSetting("Leave One Pest Plots", "Add plot number",
                        () -> SlateConfig.LEAVE_ONE_PEST_PLOTS.get(),
                        v -> {
                            SlateConfig.LEAVE_ONE_PEST_PLOTS.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> SlateConfig.LEAVE_ONE_PEST_ALIVE.get()))
                .add(new ToggleSetting("AOTV Between Distant Pests",
                        () -> SlateConfig.PEST_AOTV_BETWEEN.get(),
                        v -> {
                            SlateConfig.PEST_AOTV_BETWEEN.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Confirm AOTV Between Pests",
                        () -> SlateConfig.PEST_AOTV_CONFIRM_BETWEEN.get(),
                        v -> {
                            SlateConfig.PEST_AOTV_CONFIRM_BETWEEN.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> SlateConfig.PEST_AOTV_BETWEEN.get()))
                .add(FarmingSettingsFactory.aotvBetweenPestsDelaySetting()
                        .visibleWhen(() -> SlateConfig.PEST_AOTV_BETWEEN.get()))
                .add(FarmingSettingsFactory.pestFovRangeSetting())
                .add(FarmingSettingsFactory.pestAboveAimPitchRangeSetting()));

        groups.add(SettingGroup.of(
                        "Disco Destination",
                        "Prioritizes a selected plot and holds position for disco pests",
                        () -> SlateConfig.PEST_DISCO_DESTINATION_MODE.get(),
                        v -> {
                            SlateConfig.PEST_DISCO_DESTINATION_MODE.set(v);
                            SlateConfig.save();
                        })
                .add(new TextSetting("Disco Destination Plot", "Plot number (e.g. 5)",
                        () -> SlateConfig.PEST_DISCO_DESTINATION_PLOT.get(),
                        v -> {
                            SlateConfig.PEST_DISCO_DESTINATION_PLOT.set(v);
                            SlateConfig.save();
                        })));

        groups.add(SettingGroup.of(
                        "AOTV to Roof",
                        "Teleports to the roof before cleaning pests on selected plots",
                        () -> SlateConfig.AOTV_TO_ROOF.get(),
                        v -> {
                            SlateConfig.AOTV_TO_ROOF.set(v);
                            SlateConfig.save();
                        })
                .add(new ListSetting("AOTV Roof Plots", "Add plot number",
                        () -> SlateConfig.AOTV_ROOF_PLOTS.get(),
                        v -> {
                            SlateConfig.AOTV_ROOF_PLOTS.set(v);
                            SlateConfig.save();
                        }))
                .add(new SliderSetting("AOTV to Roof Pitch", 20, 90,
                        () -> (float) SlateConfig.AOTV_ROOF_PITCH.get(),
                        v -> {
                            SlateConfig.AOTV_ROOF_PITCH.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix("\u00B0"))
                .add(FarmingSettingsFactory.aotvToRoofPitchRangeSetting())
                .add(new ToggleSetting("Break Blocks Before AOTV",
                        () -> SlateConfig.BREAK_BLOCKS_BEFORE_AOTV.get(),
                        v -> {
                            SlateConfig.BREAK_BLOCKS_BEFORE_AOTV.set(v);
                            SlateConfig.save();
                        })));

        groups.add(SettingGroup.of(
                        "Pest Traps",
                        "Clears and refills pest traps",
                        () -> SlateConfig.ENABLE_PEST_TRAPS.get(),
                        v -> {
                            SlateConfig.ENABLE_PEST_TRAPS.set(v);
                            SlateConfig.save();
                        })
                .add(new ToggleSetting("Clear Pest Traps",
                        () -> SlateConfig.AUTO_CLEAR_PEST_TRAPS.get(),
                        v -> {
                            SlateConfig.AUTO_CLEAR_PEST_TRAPS.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Pre-equip Mosquito for Pest Traps",
                        () -> SlateConfig.AUTO_MOSQUITO_FOR_PEST_TRAPS.get(),
                        v -> {
                            SlateConfig.AUTO_MOSQUITO_FOR_PEST_TRAPS.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> SlateConfig.AUTO_CLEAR_PEST_TRAPS.get()))
                .add(new ToggleSetting("Equip Pet After Trap Open",
                        () -> SlateConfig.AUTO_PET_AFTER_TRAP_OPEN.get(),
                        v -> {
                            SlateConfig.AUTO_PET_AFTER_TRAP_OPEN.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> SlateConfig.AUTO_CLEAR_PEST_TRAPS.get()))
                .add(new TextSetting("Trap Open Pet", "e.g Rose Dragon",
                        () -> SlateConfig.AUTO_PET_AFTER_TRAP_OPEN_PET.get(),
                        v -> {
                            SlateConfig.AUTO_PET_AFTER_TRAP_OPEN_PET.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> SlateConfig.AUTO_CLEAR_PEST_TRAPS.get()
                                && SlateConfig.AUTO_PET_AFTER_TRAP_OPEN.get()))
                .add(new ToggleSetting("Refill Pest Traps",
                        () -> SlateConfig.AUTO_REFILL_PEST_TRAPS.get(),
                        v -> {
                            SlateConfig.AUTO_REFILL_PEST_TRAPS.set(v);
                            SlateConfig.save();
                        }))
                .add(new DropdownSetting("Bait Material", sprayMaterials,
                        () -> {
                            String current = SlateConfig.PEST_TRAPS_BAIT_MATERIAL.get();
                            int idx = sprayMaterials.indexOf(current);
                            return idx >= 0 ? idx : 5;
                        },
                        i -> {
                            if (i >= 0 && i < sprayMaterials.size()) {
                                SlateConfig.PEST_TRAPS_BAIT_MATERIAL.set(sprayMaterials.get(i));
                                SlateConfig.save();
                            }
                        })
                        .visibleWhen(() -> SlateConfig.AUTO_REFILL_PEST_TRAPS.get()))
                .add(new SliderSetting("Bait Amount", 1, 64,
                        () -> (float) SlateConfig.PEST_TRAPS_BAIT_AMOUNT.get(),
                        v -> {
                            SlateConfig.PEST_TRAPS_BAIT_AMOUNT.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0)
                        .visibleWhen(() -> SlateConfig.AUTO_REFILL_PEST_TRAPS.get()))
                .add(new TextSetting("Pest Traps Plot", "Plot number (e.g. 5)",
                        () -> SlateConfig.PEST_TRAPS_PLOT.get(),
                        v -> {
                            SlateConfig.PEST_TRAPS_PLOT.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> SlateConfig.AUTO_CLEAR_PEST_TRAPS.get()
                                || SlateConfig.AUTO_REFILL_PEST_TRAPS.get())));

        return MainGUIRegistry.toggleSubTab(
                "Pest Manager",
                "Automatically cleans pests, and manage your pest traps",
                () -> SlateConfig.TRIGGER_PEST_ON_CHAT.get(),
                v -> {
                    SlateConfig.TRIGGER_PEST_ON_CHAT.set(v);
                    SlateConfig.save();
                },
                groups);
    }

}
