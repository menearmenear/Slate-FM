package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;
import menear.nclient.slate.ui.settings.ToggleSetting;

import java.util.ArrayList;
import java.util.List;

public final class GearRegistryProvider extends AbstractModulesRegistryProvider {
    public GearRegistryProvider() {
        super(3);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        List<SettingGroup> groups = new ArrayList<>();

        groups.add(SettingGroup.of(
                        "Auto Loadout",
                        "Handles loadout swaps for pests and visitors",
                        GearRegistryProvider::isAutoLoadoutEnabled,
                        GearRegistryProvider::setAutoLoadoutEnabled)
                .add(new ToggleSetting("Auto Loadout Pest",
                        () -> SlateConfig.AUTO_LOADOUT_PEST.get(),
                        v -> {
                            SlateConfig.AUTO_LOADOUT_PEST.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Auto Loadout Visitor",
                        () -> SlateConfig.AUTO_LOADOUT_VISITOR.get(),
                        v -> {
                            SlateConfig.AUTO_LOADOUT_VISITOR.set(v);
                            SlateConfig.save();
                        }))
                .add(new SliderSetting("Farming Loadout Slot", 1, 12,
                        () -> (float) SlateConfig.LOADOUT_SLOT_FARMING.get(),
                        v -> {
                            SlateConfig.LOADOUT_SLOT_FARMING.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0)
                        .visibleWhen(() -> SlateConfig.AUTO_LOADOUT_PEST.get()
                                || SlateConfig.AUTO_LOADOUT_VISITOR.get()))
                .add(new SliderSetting("Pest Loadout Slot", 1, 12,
                        () -> (float) SlateConfig.LOADOUT_SLOT_PEST.get(),
                        v -> {
                            SlateConfig.LOADOUT_SLOT_PEST.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0)
                        .visibleWhen(() -> SlateConfig.AUTO_LOADOUT_PEST.get()))
                .add(new SliderSetting("Pest Loadout Swap Time", 0, 180,
                        () -> (float) SlateConfig.LOADOUT_PEST_SWAP_TIME_SECONDS.get(),
                        v -> {
                            SlateConfig.LOADOUT_PEST_SWAP_TIME_SECONDS.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0)
                        .withSuffix("s")
                        .visibleWhen(() -> SlateConfig.AUTO_LOADOUT_PEST.get()))
                .add(new SliderSetting("Visitor Loadout Slot", 1, 12,
                        () -> (float) SlateConfig.LOADOUT_SLOT_VISITOR.get(),
                        v -> {
                            SlateConfig.LOADOUT_SLOT_VISITOR.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0)
                        .visibleWhen(() -> SlateConfig.AUTO_LOADOUT_VISITOR.get())));

        return MainGUIRegistry.toggleSubTab(
                "Auto Loadout",
                "Automatically swaps loadouts",
                GearRegistryProvider::isAutoLoadoutEnabled,
                GearRegistryProvider::setAutoLoadoutEnabled,
                groups);
    }

    private static boolean isAutoLoadoutEnabled() {
        return SlateConfig.AUTO_LOADOUT_PEST.get() || SlateConfig.AUTO_LOADOUT_VISITOR.get();
    }

    private static void setAutoLoadoutEnabled(boolean enabled) {
        SlateConfig.AUTO_LOADOUT_PEST.set(enabled);
        SlateConfig.AUTO_LOADOUT_VISITOR.set(enabled);
        SlateConfig.save();
    }
}
