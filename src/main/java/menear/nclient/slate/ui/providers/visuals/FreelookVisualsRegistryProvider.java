package menear.nclient.slate.ui;

import menear.nclient.slate.bootstrap.SlateKeybindRegistry;
import menear.nclient.slate.config.ConfigHelpers;
import menear.nclient.slate.config.FreelookMode;
import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.ui.settings.DropdownSetting;
import menear.nclient.slate.ui.settings.KeybindSetting;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;

import java.util.Arrays;
import java.util.List;

public final class FreelookVisualsRegistryProvider extends AbstractVisualsRegistryProvider {
    public FreelookVisualsRegistryProvider() {
        super(3);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        SettingGroup group = SettingGroup.alwaysOn(
                "Freelook Settings",
                "Orbit the camera around your player while your body keeps facing forward"
        );
        group.add(new KeybindSetting("Freelook Keybind", SlateKeybindRegistry.getFreelookKey()));
        group.add(new DropdownSetting("Activation Mode",
                List.of("Hold", "Toggle"),
                () -> Arrays.asList(FreelookMode.values()).indexOf(ConfigHelpers.getFreelookMode()),
                i -> {
                    SlateConfig.FREELOOK_MODE.set(FreelookMode.values()[i].name());
                    SlateConfig.save();
                }));

        return MainGUIRegistry.toggleSubTab(
                "Freelook",
                "Orbit the camera freely without turning your player",
                () -> SlateConfig.FREELOOK_ENABLED.get(),
                enabled -> {
                    SlateConfig.FREELOOK_ENABLED.set(enabled);
                    SlateConfig.save();
                },
                List.of(group)
        );
    }
}
