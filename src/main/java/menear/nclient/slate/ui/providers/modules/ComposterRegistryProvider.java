package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.modules.ComposterManager;
import menear.nclient.slate.notification.NotificationManager;
import menear.nclient.slate.ui.settings.ActionSetting;
import menear.nclient.slate.ui.settings.DropdownSetting;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.PositionSetting;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;
import menear.nclient.slate.ui.settings.TextSetting;
import menear.nclient.slate.util.SlateLang;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class ComposterRegistryProvider extends AbstractModulesRegistryProvider {
    public ComposterRegistryProvider() {
        super(8);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        SettingGroup group = SettingGroup.alwaysOn(
                        "Composter Settings",
                        "Configure Auto Composter behavior")
                .add(new PositionSetting("Composter Position",
                        () -> (double) SlateConfig.AUTO_COMPOSTER_X.get(),
                        v -> {
                            SlateConfig.AUTO_COMPOSTER_X.set((int) Math.round(v));
                            SlateConfig.save();
                        },
                        () -> (double) SlateConfig.AUTO_COMPOSTER_Y.get(),
                        v -> {
                            SlateConfig.AUTO_COMPOSTER_Y.set((int) Math.round(v));
                            SlateConfig.save();
                        },
                        () -> (double) SlateConfig.AUTO_COMPOSTER_Z.get(),
                        v -> {
                            SlateConfig.AUTO_COMPOSTER_Z.set((int) Math.round(v));
                            SlateConfig.save();
                        },
                        () -> SlateConfig.AUTO_COMPOSTER_HIGHLIGHT.get(),
                        v -> {
                            SlateConfig.AUTO_COMPOSTER_HIGHLIGHT.set(v);
                            SlateConfig.save();
                        },
                        () -> {
                            var player = Minecraft.getInstance().player;
                            if (player != null) {
                                SlateConfig.AUTO_COMPOSTER_X.set(player.getBlockX());
                                SlateConfig.AUTO_COMPOSTER_Y.set(player.getBlockY());
                                SlateConfig.AUTO_COMPOSTER_Z.set(player.getBlockZ());
                                SlateConfig.save();
                                NotificationManager.success(SlateLang.localize("Composter Position Set"),
                                        String.format("X: %d, Y: %d, Z: %d",
                                                SlateConfig.AUTO_COMPOSTER_X.get(),
                                                SlateConfig.AUTO_COMPOSTER_Y.get(),
                                                SlateConfig.AUTO_COMPOSTER_Z.get()));
                            }
                        }))
                .add(new SliderSetting("Run Interval", 1, 1440,
                        () -> (float) SlateConfig.AUTO_COMPOSTER_INTERVAL_MINUTES.get(),
                        v -> {
                            SlateConfig.AUTO_COMPOSTER_INTERVAL_MINUTES.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix(" min"))
                .add(new DropdownSetting("Source Mode", List.of("Sacks", "Bazaar"),
                        ComposterManager::getSourceModeIndex,
                        ComposterSettingsBridge::setSourceModeIndex))
                .add(new SliderSetting("Minimum Purse", 0, 2000000000,
                        () -> (float) SlateConfig.AUTO_COMPOSTER_MIN_PURSE.get(),
                        v -> {
                            SlateConfig.AUTO_COMPOSTER_MIN_PURSE.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix(" coins")
                        .visibleWhen(ComposterManager::isBazaarMode))
                .add(new TextSetting("Crop Material", "e.g. Box of Seeds",
                        () -> SlateConfig.AUTO_COMPOSTER_CROP_MATERIAL.get(),
                        v -> {
                            SlateConfig.AUTO_COMPOSTER_CROP_MATERIAL.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(ComposterManager::isBazaarMode))
                .add(new SliderSetting("Crop Amount", 1, 2000000,
                        () -> (float) SlateConfig.AUTO_COMPOSTER_CROP_AMOUNT.get(),
                        v -> {
                            SlateConfig.AUTO_COMPOSTER_CROP_AMOUNT.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0)
                        .visibleWhen(ComposterManager::isBazaarMode))
                .add(new TextSetting("Fuel Material", "e.g. Volta",
                        () -> SlateConfig.AUTO_COMPOSTER_FUEL_MATERIAL.get(),
                        v -> {
                            SlateConfig.AUTO_COMPOSTER_FUEL_MATERIAL.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(ComposterManager::isBazaarMode))
                .add(new SliderSetting("Fuel Amount", 1, 2000000,
                        () -> (float) SlateConfig.AUTO_COMPOSTER_FUEL_AMOUNT.get(),
                        v -> {
                            SlateConfig.AUTO_COMPOSTER_FUEL_AMOUNT.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0)
                        .visibleWhen(ComposterManager::isBazaarMode))
                .add(new ActionSetting("Run Now",
                        () -> ComposterManager.manualTrigger(Minecraft.getInstance())));

        return MainGUIRegistry.toggleSubTab(
                "Auto Composter",
                "Automatically refills the Garden composter",
                () -> SlateConfig.AUTO_COMPOSTER.get(),
                v -> {
                    SlateConfig.AUTO_COMPOSTER.set(v);
                    SlateConfig.save();
                },
                List.of(group));
    }
}
