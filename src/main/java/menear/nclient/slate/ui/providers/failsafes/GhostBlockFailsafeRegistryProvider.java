package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.modules.failsafe.FailsafeCustomReplayManager.FailsafeReplayType;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;

import java.util.List;

public final class GhostBlockFailsafeRegistryProvider extends AbstractFailsafesRegistryProvider {
    public GhostBlockFailsafeRegistryProvider() {
        super(4);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        SettingGroup group = SettingGroup.alwaysOn(
                        "Ghost Block Failsafe",
                        "Triggers when the farming exp text disappears while the macro is actively farming")
                .add(FailsafeActionSettings.createActionDropdown("Action",
                        () -> SlateConfig.FAILSAFE_GHOST_BLOCK_ACTION.get(),
                        value -> SlateConfig.FAILSAFE_GHOST_BLOCK_ACTION.set(value)))
                .add(FailsafeActionSettings.createCustomReplayDropdown(FailsafeReplayType.GHOST_BLOCK,
                        () -> SlateConfig.FAILSAFE_GHOST_BLOCK_ACTION.get()))
                .add(new SliderSetting("Window", 1, 30,
                        () -> (float) SlateConfig.FAILSAFE_GHOST_BLOCK_WINDOW_SECONDS.get(),
                        v -> {
                            SlateConfig.FAILSAFE_GHOST_BLOCK_WINDOW_SECONDS.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix("s"))
                .add(new SliderSetting("Trigger Delay", 0, 5,
                        () -> SlateConfig.FAILSAFE_GHOST_BLOCK_TRIGGER_DELAY_SECONDS.get(),
                        v -> {
                            SlateConfig.FAILSAFE_GHOST_BLOCK_TRIGGER_DELAY_SECONDS.set(v);
                            SlateConfig.save();
                        })
                        .withDecimals(1).withSuffix("s"));

        return MainGUIRegistry.toggleSubTab(
                "Ghost Block",
                "Triggers when the farming exp text disappears while the macro is actively farming",
                () -> SlateConfig.FAILSAFE_GHOST_BLOCK.get(),
                v -> {
                    SlateConfig.FAILSAFE_GHOST_BLOCK.set(v);
                    SlateConfig.save();
                },
                List.of(group));
    }
}
