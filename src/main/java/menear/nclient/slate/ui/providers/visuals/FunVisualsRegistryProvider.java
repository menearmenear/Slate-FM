package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;
import menear.nclient.slate.ui.settings.ToggleSetting;

import java.util.ArrayList;
import java.util.List;

public final class FunVisualsRegistryProvider extends AbstractVisualsRegistryProvider {
    public FunVisualsRegistryProvider() {
        super(3);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        List<SettingGroup> groups = new ArrayList<>();
        groups.add(SettingGroup.of(
                        "Hat",
                        "Renders a chroma pyramid above your head",
                        () -> SlateConfig.HAT_ENABLED.get(),
                        v -> {
                            SlateConfig.HAT_ENABLED.set(v);
                            SlateConfig.save();
                        })
                .add(new ToggleSetting("Filled Sides",
                        () -> SlateConfig.HAT_FILLED.get(),
                        v -> {
                            SlateConfig.HAT_FILLED.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Render In First Person",
                        () -> SlateConfig.HAT_RENDER_FIRST_PERSON.get(),
                        v -> {
                            SlateConfig.HAT_RENDER_FIRST_PERSON.set(v);
                            SlateConfig.save();
                        }))
                .add(new SliderSetting("Pyramid Height", 0.1f, 3.0f,
                        () -> SlateConfig.HAT_HEIGHT.get(),
                        v -> {
                            SlateConfig.HAT_HEIGHT.set(v);
                            SlateConfig.save();
                        })
                        .withDecimals(1))
                .add(new SliderSetting("Radius", 0.1f, 3.0f,
                        () -> SlateConfig.HAT_RADIUS.get(),
                        v -> {
                            SlateConfig.HAT_RADIUS.set(v);
                            SlateConfig.save();
                        })
                        .withDecimals(1))
                .add(new SliderSetting("Y Offset", 0.1f, 3.0f,
                        () -> SlateConfig.HAT_Y_OFFSET.get(),
                        v -> {
                            SlateConfig.HAT_Y_OFFSET.set(v);
                            SlateConfig.save();
                        })
                        .withDecimals(1))
                .add(new SliderSetting("Vertices", 3.0f, 30.0f,
                        () -> (float) SlateConfig.HAT_VERTICES.get(),
                        v -> {
                            SlateConfig.HAT_VERTICES.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0)));
        groups.add(SettingGroup.of(
                        "Funny Dynamic Rest",
                        "Uses a FakePixel-style ban screen during dynamic rest",
                        () -> SlateConfig.FUNNY_DYNAMIC_REST.get(),
                        v -> {
                            SlateConfig.FUNNY_DYNAMIC_REST.set(v);
                            SlateConfig.save();
                        }));
        return MainGUIRegistry.subTab("Fun", "Cosmetic world effects", groups);
    }
}
