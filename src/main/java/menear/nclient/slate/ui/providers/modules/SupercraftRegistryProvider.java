package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.modules.SupercraftManager;
import menear.nclient.slate.ui.settings.ActionSetting;
import menear.nclient.slate.ui.settings.ListSetting;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class SupercraftRegistryProvider extends AbstractModulesRegistryProvider {
    public SupercraftRegistryProvider() {
        super(9);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        SettingGroup group = SettingGroup.alwaysOn(
                        "Supercraft Settings",
                        "Configure Auto Supercraft behavior")
                .add(new SliderSetting("Run Interval", 1, 1440,
                        () -> (float) SlateConfig.AUTO_SUPERCRAFT_INTERVAL_MINUTES.get(),
                        v -> {
                            SlateConfig.AUTO_SUPERCRAFT_INTERVAL_MINUTES.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix(" min"))
                .add(new ListSetting("Items", "Add item name",
                        () -> SlateConfig.AUTO_SUPERCRAFT_ITEMS.get(),
                        v -> {
                            SlateConfig.AUTO_SUPERCRAFT_ITEMS.set(v);
                            SlateConfig.save();
                        }))
                .add(new ActionSetting("Run Now",
                        () -> SupercraftManager.manualTrigger(Minecraft.getInstance())));

        return MainGUIRegistry.toggleSubTab(
                "Auto Supercraft",
                "Automatically crafts configured items with Supercraft",
                () -> SlateConfig.AUTO_SUPERCRAFT.get(),
                v -> {
                    SlateConfig.AUTO_SUPERCRAFT.set(v);
                    SlateConfig.save();
                },
                List.of(group));
    }
}
