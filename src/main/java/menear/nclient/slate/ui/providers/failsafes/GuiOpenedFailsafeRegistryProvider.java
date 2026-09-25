package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.modules.failsafe.FailsafeCustomReplayManager.FailsafeReplayType;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;

import java.util.List;

public final class GuiOpenedFailsafeRegistryProvider extends AbstractFailsafesRegistryProvider {
    public GuiOpenedFailsafeRegistryProvider() {
        super(3);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        SettingGroup group = SettingGroup.alwaysOn(
                        "GUI Opened",
                        "Triggers when an inventory GUI opens during farming or cleaning")
                .add(FailsafeActionSettings.createActionDropdown("Action",
                        () -> SlateConfig.FAILSAFE_UNEXPECTED_INVENTORY_GUI_ACTION.get(),
                        value -> SlateConfig.FAILSAFE_UNEXPECTED_INVENTORY_GUI_ACTION.set(value)))
                .add(FailsafeActionSettings.createCustomReplayDropdown(FailsafeReplayType.GUI_OPENED,
                        () -> SlateConfig.FAILSAFE_UNEXPECTED_INVENTORY_GUI_ACTION.get()))
                .add(new SliderSetting("Trigger Delay", 0.0f, 5.0f,
                        () -> SlateConfig.FAILSAFE_UNEXPECTED_INVENTORY_GUI_DELAY_SECONDS.get(),
                        v -> {
                            SlateConfig.FAILSAFE_UNEXPECTED_INVENTORY_GUI_DELAY_SECONDS.set(v);
                            SlateConfig.save();
                        })
                        .withDecimals(1).withSuffix("s"));

        return MainGUIRegistry.toggleSubTab(
                "GUI Opened",
                "Triggers when an inventory GUI opens during farming or cleaning",
                () -> SlateConfig.FAILSAFE_UNEXPECTED_INVENTORY_GUI.get(),
                v -> {
                    SlateConfig.FAILSAFE_UNEXPECTED_INVENTORY_GUI.set(v);
                    SlateConfig.save();
                },
                List.of(group));
    }
}
