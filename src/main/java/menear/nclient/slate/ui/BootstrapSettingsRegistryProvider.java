package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.ui.settings.ToggleSetting;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;

import java.util.List;

public final class BootstrapSettingsRegistryProvider implements MainGUIRegistryProvider {
    private static final int ORDER = 1;

    @Override
    public void register(MainGUIRegistry.Registrar registrar) {
        registrar.registerSettings(ORDER, createSubTab());
    }

    private ModulesTab.SubTab createSubTab() {
        SettingGroup group = SettingGroup.alwaysOn(
                "Bootstrap",
                "Settings that apply before premium modules are loaded");
        group.add(new ToggleSetting("Custom UI",
                () -> SlateConfig.CUSTOM_UI_ENABLED.get(),
                value -> {
                    SlateConfig.CUSTOM_UI_ENABLED.set(value);
                    SlateConfig.save();
                }));
        return MainGUIRegistry.subTab("Bootstrap", "Pre-login bootstrap settings", List.of(group));
    }
}
