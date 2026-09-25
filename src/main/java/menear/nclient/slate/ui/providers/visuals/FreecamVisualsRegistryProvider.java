package menear.nclient.slate.ui;

import menear.nclient.slate.bootstrap.SlateKeybindRegistry;
import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.modules.visuals.FreecamManager;
import menear.nclient.slate.ui.settings.KeybindSetting;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;

import java.util.List;

public final class FreecamVisualsRegistryProvider extends AbstractVisualsRegistryProvider {
    public FreecamVisualsRegistryProvider() {
        super(2);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        SettingGroup group = SettingGroup.alwaysOn(
                "Freecam Settings",
                "Detach the camera and fly around without moving your player"
        );
        group.add(new KeybindSetting("Toggle Keybind", SlateKeybindRegistry.getFreecamKey()));
        group.add(new KeybindSetting("Teleport To Player Keybind", SlateKeybindRegistry.getFreecamTeleportToPlayerKey()));
        group.add(new SliderSetting("Movement Speed", 0.1f, 2.5f,
                () -> SlateConfig.FREECAM_SPEED.get(),
                value -> {
                    SlateConfig.FREECAM_SPEED.set(value);
                    SlateConfig.save();
                })
                .withDecimals(2));

        return MainGUIRegistry.toggleSubTab(
                "Freecam",
                "Allow the freecam keybind to detach and move the camera freely",
                FreecamManager::isFeatureEnabled,
                FreecamManager::setFeatureEnabled,
                List.of(group)
        );
    }
}
