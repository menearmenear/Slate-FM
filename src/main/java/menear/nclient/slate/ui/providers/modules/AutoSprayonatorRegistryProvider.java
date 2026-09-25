package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;
import menear.nclient.slate.ui.settings.ToggleSetting;

import java.util.List;

public final class AutoSprayonatorRegistryProvider extends AbstractModulesRegistryProvider {
    public AutoSprayonatorRegistryProvider() {
        super(5);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        SettingGroup group = SettingGroup.alwaysOn(
                        "Sprayonator Settings",
                        "Configure Auto Sprayonator behavior")
                .add(new ToggleSetting("Auto Buy Material",
                        () -> SlateConfig.AUTO_SPRAYONATOR_AUTO_BUY.get(),
                        v -> {
                            SlateConfig.AUTO_SPRAYONATOR_AUTO_BUY.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> SlateConfig.AUTO_SPRAYONATOR.get()))
                .add(new SliderSetting("Auto Buy Amount", 1, 64,
                        () -> (float) SlateConfig.AUTO_SPRAYONATOR_AUTO_BUY_AMOUNT.get(),
                        v -> {
                            SlateConfig.AUTO_SPRAYONATOR_AUTO_BUY_AMOUNT.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0)
                        .visibleWhen(() -> SlateConfig.AUTO_SPRAYONATOR.get()
                                && SlateConfig.AUTO_SPRAYONATOR_AUTO_BUY.get()))
                .add(new SliderSetting("Unsprayed Plot Detect Time", 5, 30,
                        () -> (float) SlateConfig.AUTO_SPRAYONATOR_DETECT_TIME.get(),
                        v -> {
                            SlateConfig.AUTO_SPRAYONATOR_DETECT_TIME.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix("s")
                        .visibleWhen(() -> SlateConfig.AUTO_SPRAYONATOR.get()));

        return MainGUIRegistry.toggleSubTab(
                "Auto Sprayonator",
                "Automatically detects and sprays unsprayed plots",
                () -> SlateConfig.AUTO_SPRAYONATOR.get(),
                v -> {
                    SlateConfig.AUTO_SPRAYONATOR.set(v);
                    SlateConfig.save();
                },
                List.of(group));
    }
}
