package menear.nclient.slate.ui;

import menear.nclient.slate.bootstrap.SlateKeybindRegistry;
import menear.nclient.slate.modules.visuals.UngrabMouseManager;
import menear.nclient.slate.ui.settings.KeybindSetting;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;

import java.util.List;

public final class UngrabMouseVisualsRegistryProvider extends AbstractVisualsRegistryProvider {
    public UngrabMouseVisualsRegistryProvider() {
        super(4);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        SettingGroup group = SettingGroup.alwaysOn(
                "Ungrab Mouse Settings",
                "Release the mouse cursor so you can move it outside the game window"
        );
        group.add(new KeybindSetting("Toggle Keybind", SlateKeybindRegistry.getUngrabMouseKey()));

        return MainGUIRegistry.toggleSubTab(
                "Ungrab Mouse",
                "Release the mouse cursor so you can move it outside the game window",
                UngrabMouseManager::isEnabled,
                UngrabMouseManager::setEnabled,
                List.of(group)
        );
    }
}
