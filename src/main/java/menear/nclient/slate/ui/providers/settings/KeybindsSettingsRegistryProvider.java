package menear.nclient.slate.ui;

import menear.nclient.slate.bootstrap.SlateKeybindRegistry;
import menear.nclient.slate.ui.settings.KeybindSetting;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;

import java.util.ArrayList;
import java.util.List;

public final class KeybindsSettingsRegistryProvider extends AbstractKeybindsRegistryProvider {
    public KeybindsSettingsRegistryProvider() {
        super(0);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        List<SettingGroup> groups = new ArrayList<>();
        SettingGroup keybinds = SettingGroup.alwaysOn(
                "Slate Keybinds",
                "These bindings stay synced with Minecraft's Controls screen"
        );

        for (SlateKeybindRegistry.RegisteredKeybind registeredKeybind : SlateKeybindRegistry.getRegisteredKeybinds()) {
            keybinds.add(new KeybindSetting(registeredKeybind.name(), registeredKeybind.mapping()));
        }

        groups.add(keybinds);
        return MainGUIRegistry.subTab("Slate", "Keyboard shortcuts for the client", groups);
    }
}
