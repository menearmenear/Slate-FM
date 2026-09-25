package menear.nclient.slate.ui;

import menear.nclient.slate.ui.settings.ColorSetting;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.theme.Theme;

import java.util.ArrayList;
import java.util.List;

public final class MenuColorsRegistryProvider extends AbstractColorsRegistryProvider {
    public MenuColorsRegistryProvider() {
        super(1);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        List<SettingGroup> groups = new ArrayList<>();
        SettingGroup menuColors = SettingGroup.alwaysOn(
                "Menu Colors",
                "Customize config menu colors");
        for (Theme.ThemeEntry entry : Theme.ENTRIES) {
            menuColors.add(new ColorSetting(entry.label, entry.getter,
                    value -> {
                        entry.setter.accept(value);
                        Theme.saveTheme();
                    }));
        }
        groups.add(menuColors);
        return MainGUIRegistry.subTab("Menu Colors", "Customize config menu colors", groups);
    }
}
