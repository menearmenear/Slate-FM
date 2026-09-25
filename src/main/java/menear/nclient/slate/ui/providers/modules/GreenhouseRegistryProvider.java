package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.ui.settings.ActionSetting;
import menear.nclient.slate.ui.settings.ListSetting;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;
import menear.nclient.slate.ui.settings.TextSetting;
import menear.nclient.slate.ui.settings.ToggleSetting;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class GreenhouseRegistryProvider extends AbstractModulesRegistryProvider {
    public GreenhouseRegistryProvider() {
        super(7);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        SettingGroup group = SettingGroup.alwaysOn(
                        "Greenhouse Settings",
                        "Configure Auto Greenhouse behavior")
                .add(new SliderSetting("Run Interval", 1, 1440,
                        () -> (float) SlateConfig.AUTO_GREENHOUSE_INTERVAL_MINUTES.get(),
                        v -> {
                            SlateConfig.AUTO_GREENHOUSE_INTERVAL_MINUTES.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix(" min"))
                .add(new ListSetting("Plots", "Add plot number",
                        () -> SlateConfig.GREENHOUSE_PLOTS.get(),
                        v -> {
                            SlateConfig.GREENHOUSE_PLOTS.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Equip Custom Item",
                        () -> SlateConfig.EQUIP_GREENHOUSE_CUSTOM_ITEM.get(),
                        v -> {
                            SlateConfig.EQUIP_GREENHOUSE_CUSTOM_ITEM.set(v);
                            SlateConfig.save();
                        }))
                .add(new TextSetting("Custom Item", "e.g. Nether Wart Hoe",
                        () -> SlateConfig.GREENHOUSE_CUSTOM_ITEM.get(),
                        v -> {
                            SlateConfig.GREENHOUSE_CUSTOM_ITEM.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> SlateConfig.EQUIP_GREENHOUSE_CUSTOM_ITEM.get()))
                .add(new ToggleSetting("Harvest Ashwreath",
                        () -> SlateConfig.HARVEST_ASHWREATH.get(),
                        v -> {
                            SlateConfig.HARVEST_ASHWREATH.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Harvest Turtellini",
                        () -> SlateConfig.HARVEST_TURTELLINI.get(),
                        v -> {
                            SlateConfig.HARVEST_TURTELLINI.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Harvest Glasscorn",
                        () -> SlateConfig.HARVEST_GLASSCORN.get(),
                        v -> {
                            SlateConfig.HARVEST_GLASSCORN.set(v);
                            SlateConfig.save();
                        }))
                .add(new ActionSetting("Harvest Now",
                        () -> menear.nclient.slate.modules.GreenhouseManager.harvest(Minecraft.getInstance())));

        return MainGUIRegistry.toggleSubTab(
                "Auto Greenhouse",
                "Automatically harvests your greenhouse",
                () -> SlateConfig.AUTO_GREENHOUSE.get(),
                v -> {
                    SlateConfig.AUTO_GREENHOUSE.set(v);
                    SlateConfig.save();
                },
                List.of(group));
    }
}
