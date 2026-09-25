package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.modules.failsafe.FailsafeCustomReplayManager.FailsafeReplayType;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;

import java.util.List;

public final class DirtFailsafeRegistryProvider extends AbstractFailsafesRegistryProvider {
    public DirtFailsafeRegistryProvider() {
        super(5);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        SettingGroup group = SettingGroup.alwaysOn(
                        "Dirt Check Failsafe",
                        "Triggers when a suspicious solid block stays close to the player during farming")
                .add(FailsafeActionSettings.createActionDropdown("Action",
                        () -> SlateConfig.FAILSAFE_DIRT_CHECK_ACTION.get(),
                        value -> SlateConfig.FAILSAFE_DIRT_CHECK_ACTION.set(value)))
                .add(FailsafeActionSettings.createCustomReplayDropdown(FailsafeReplayType.DIRT_CHECK,
                        () -> SlateConfig.FAILSAFE_DIRT_CHECK_ACTION.get()))
                .add(new SliderSetting("Trigger Delay", 0, 10,
                        () -> SlateConfig.FAILSAFE_DIRT_CHECK_TRIGGER_DELAY_SECONDS.get(),
                        v -> {
                            SlateConfig.FAILSAFE_DIRT_CHECK_TRIGGER_DELAY_SECONDS.set(v);
                            SlateConfig.save();
                        })
                        .withDecimals(1).withSuffix("s"));

        return MainGUIRegistry.toggleSubTab(
                "Dirt Check",
                "Triggers when a suspicious solid block stays close to the player during farming",
                () -> SlateConfig.FAILSAFE_DIRT_CHECK.get(),
                v -> {
                    SlateConfig.FAILSAFE_DIRT_CHECK.set(v);
                    SlateConfig.save();
                },
                List.of(group));
    }
}
