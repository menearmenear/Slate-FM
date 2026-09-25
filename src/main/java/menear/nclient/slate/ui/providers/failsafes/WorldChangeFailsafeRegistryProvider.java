package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.modules.failsafe.FailsafeCustomReplayManager.FailsafeReplayType;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;

import java.util.List;

public final class WorldChangeFailsafeRegistryProvider extends AbstractFailsafesRegistryProvider {
    public WorldChangeFailsafeRegistryProvider() {
        super(7);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        SettingGroup group = SettingGroup.alwaysOn(
                        "World Change Failsafe",
                        "Handles unexpected world changes while farming")
                .add(FailsafeActionSettings.createActionDropdown("Action",
                        () -> SlateConfig.FAILSAFE_WORLD_CHANGE_ACTION.get(),
                        value -> SlateConfig.FAILSAFE_WORLD_CHANGE_ACTION.set(value)))
                .add(FailsafeActionSettings.createCustomReplayDropdown(FailsafeReplayType.WORLD_CHANGE,
                        () -> SlateConfig.FAILSAFE_WORLD_CHANGE_ACTION.get()))
                .add(new SliderSetting("Recovery Wait", 0, 30,
                        () -> SlateConfig.FAILSAFE_WORLD_CHANGE_RECOVERY_WAIT_SECONDS.get(),
                        v -> {
                            SlateConfig.FAILSAFE_WORLD_CHANGE_RECOVERY_WAIT_SECONDS.set(v);
                            SlateConfig.save();
                        })
                        .withDecimals(1).withSuffix("s"));

        return MainGUIRegistry.toggleSubTab(
                "World Change",
                "Handles unexpected world changes while farming",
                () -> SlateConfig.FAILSAFE_WORLD_CHANGE.get(),
                v -> {
                    SlateConfig.FAILSAFE_WORLD_CHANGE.set(v);
                    SlateConfig.save();
                },
                List.of(group));
    }
}
