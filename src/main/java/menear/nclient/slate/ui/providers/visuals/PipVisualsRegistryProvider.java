package menear.nclient.slate.ui;

import menear.nclient.slate.bootstrap.SlateKeybindRegistry;
import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.modules.visuals.PipManager;
import menear.nclient.slate.ui.settings.KeybindSetting;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;
import menear.nclient.slate.ui.settings.ToggleSetting;

import java.util.List;

public final class PipVisualsRegistryProvider extends AbstractVisualsRegistryProvider {
    public PipVisualsRegistryProvider() {
        super(3);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        SettingGroup group = SettingGroup.alwaysOn(
                "PiP Settings",
                "Open a picture-in-picture view of the game"
        );
        group.add(new KeybindSetting("Toggle Keybind", SlateKeybindRegistry.getPipKey()));
        group.add(new ToggleSetting("Start Floating",
                () -> SlateConfig.PIP_START_FLOATING.get(),
                value -> {
                    SlateConfig.PIP_START_FLOATING.set(value);
                    SlateConfig.save();
                }));
        group.add(new ToggleSetting("Window Decorations",
                () -> SlateConfig.PIP_START_DECORATED.get(),
                value -> {
                    SlateConfig.PIP_START_DECORATED.set(value);
                    SlateConfig.save();
                }));
        group.add(new SliderSetting("Window Width", 240.0f, 1920.0f,
                () -> (float) SlateConfig.PIP_WINDOW_WIDTH.get(),
                value -> {
                    SlateConfig.PIP_WINDOW_WIDTH.set(Math.round(value));
                    SlateConfig.save();
                })
                .withDecimals(0)
                .withSuffix(" px"));
        group.add(new SliderSetting("Window Height", 135.0f, 1080.0f,
                () -> (float) SlateConfig.PIP_WINDOW_HEIGHT.get(),
                value -> {
                    SlateConfig.PIP_WINDOW_HEIGHT.set(Math.round(value));
                    SlateConfig.save();
                })
                .withDecimals(0)
                .withSuffix(" px"));

        return MainGUIRegistry.toggleSubTab(
                "PiP",
                "Open a picture-in-picture view of the game",
                PipManager::isEnabled,
                PipManager::setEnabled,
                List.of(group)
        );
    }
}
