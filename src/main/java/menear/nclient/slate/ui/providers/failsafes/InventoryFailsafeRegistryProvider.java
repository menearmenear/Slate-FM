package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.modules.failsafe.FailsafeCustomReplayManager.FailsafeReplayType;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;

import java.util.List;

public final class InventoryFailsafeRegistryProvider extends AbstractFailsafesRegistryProvider {
    public InventoryFailsafeRegistryProvider() {
        super(2);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        SettingGroup slotGroup = SettingGroup.alwaysOn(
                        "Inventory Slot Changed",
                        "Triggers when the selected hotbar slot changes unexpectedly")
                .add(FailsafeActionSettings.createActionDropdown("Action",
                        () -> SlateConfig.FAILSAFE_INVENTORY_SLOT_CHANGED_ACTION.get(),
                        value -> SlateConfig.FAILSAFE_INVENTORY_SLOT_CHANGED_ACTION.set(value)))
                .add(FailsafeActionSettings.createCustomReplayDropdown(FailsafeReplayType.INVENTORY_SLOT,
                        () -> SlateConfig.FAILSAFE_INVENTORY_SLOT_CHANGED_ACTION.get()))
                .add(new SliderSetting("Trigger Delay", 0, 5,
                        () -> SlateConfig.FAILSAFE_INVENTORY_SLOT_CHANGED_DELAY_SECONDS.get(),
                        v -> {
                            SlateConfig.FAILSAFE_INVENTORY_SLOT_CHANGED_DELAY_SECONDS.set(v);
                            SlateConfig.save();
                        })
                        .withDecimals(1).withSuffix("s"));

        return MainGUIRegistry.toggleSubTab(
                "Inventory Slot Changed",
                "Triggers when the selected hotbar slot changes unexpectedly",
                () -> SlateConfig.FAILSAFE_INVENTORY_SLOT_CHANGED.get(),
                v -> {
                    SlateConfig.FAILSAFE_INVENTORY_SLOT_CHANGED.set(v);
                    SlateConfig.save();
                },
                List.of(slotGroup));
    }
}
