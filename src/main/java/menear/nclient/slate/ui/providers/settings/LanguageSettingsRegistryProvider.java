package menear.nclient.slate.ui;

import menear.nclient.slate.ui.settings.ActionSetting;
import menear.nclient.slate.ui.settings.DropdownSetting;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.util.SlateLanguageManager;

import java.util.List;

public final class LanguageSettingsRegistryProvider extends AbstractSettingsRegistryProvider {
    public LanguageSettingsRegistryProvider() {
        super(1);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        List<String> languageOptions = SlateLanguageManager.getAvailableLanguageCodes();

        SettingGroup group = SettingGroup.alwaysOn(
                "Language",
                "Switch the Slate UI language and refetch language packs");
        group.add(new DropdownSetting("Language",
                languageOptions,
                () -> SlateLanguageManager.getSelectedLanguageIndex(languageOptions),
                index -> {
                    if (index < 0 || index >= languageOptions.size()) {
                        return;
                    }
                    SlateLanguageManager.selectLanguage(languageOptions.get(index));
                }));
        group.add(new ActionSetting("Refresh Language Packs",
                () -> SlateLanguageManager.refreshFromRemoteAsync(true)));

        return MainGUIRegistry.subTab("Language", "Switch the Slate UI language", List.of(group));
    }
}
