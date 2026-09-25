package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.ui.settings.InfoSetting;
import menear.nclient.slate.ui.settings.ListSetting;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;

import java.util.List;

public final class MiningRegistryProvider extends AbstractMiningRegistryProvider {
    public MiningRegistryProvider() {
        super(3);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        SettingGroup metalDetector = SettingGroup.alwaysOn(
                        "Metal Detector",
                        "Crystal Hollows metal detector automation and backpack filling")
                .add(new InfoSetting("Trigger",
                        () -> "Run /slate metaldetector to start or stop the solver. This menu only edits backpack options.")
                        .multiline())
                .add(new ListSetting("Blacklist Backpacks", "Add backpack number (1-18)",
                        () -> SlateConfig.METAL_DETECTOR_BACKPACK_BLACKLIST.get(),
                        values -> {
                            SlateConfig.METAL_DETECTOR_BACKPACK_BLACKLIST.set(values);
                            SlateConfig.save();
                        }));

        return MainGUIRegistry.subTab(
                "Metal Detector",
                "Crystal Hollows metal detector automation settings",
                List.of(metalDetector));
    }
}
