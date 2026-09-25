package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.notification.NotificationManager;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.PositionSetting;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.util.SlateLang;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class AutoPestExchangeRegistryProvider extends AbstractModulesRegistryProvider {
    public AutoPestExchangeRegistryProvider() {
        super(7);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        SettingGroup group = SettingGroup.alwaysOn(
                        "Pest Exchange",
                        "Configure Auto Pest Exchange behavior")
                .add(new PositionSetting("Exchange Desk",
                        () -> (double) SlateConfig.PEST_EXCHANGE_DESK_X.get(),
                        v -> {
                            SlateConfig.PEST_EXCHANGE_DESK_X.set((int) Math.round(v));
                            SlateConfig.save();
                        },
                        () -> (double) SlateConfig.PEST_EXCHANGE_DESK_Y.get(),
                        v -> {
                            SlateConfig.PEST_EXCHANGE_DESK_Y.set((int) Math.round(v));
                            SlateConfig.save();
                        },
                        () -> (double) SlateConfig.PEST_EXCHANGE_DESK_Z.get(),
                        v -> {
                            SlateConfig.PEST_EXCHANGE_DESK_Z.set((int) Math.round(v));
                            SlateConfig.save();
                        },
                        () -> SlateConfig.PEST_HIGHLIGHT_DESK.get(),
                        v -> {
                            SlateConfig.PEST_HIGHLIGHT_DESK.set(v);
                            SlateConfig.save();
                        },
                        () -> {
                            var player = Minecraft.getInstance().player;
                            if (player != null) {
                                SlateConfig.PEST_EXCHANGE_DESK_X.set(player.getBlockX());
                                SlateConfig.PEST_EXCHANGE_DESK_Y.set(player.getBlockY());
                                SlateConfig.PEST_EXCHANGE_DESK_Z.set(player.getBlockZ());
                                SlateConfig.save();
                        NotificationManager.success(SlateLang.localize("Pest Exchange Desk Set"),
                                String.format("X: %d, Y: %d, Z: %d",
                                        SlateConfig.PEST_EXCHANGE_DESK_X.get(),
                                        SlateConfig.PEST_EXCHANGE_DESK_Y.get(),
                                                SlateConfig.PEST_EXCHANGE_DESK_Z.get()));
                            }
                        }))
                .add(FarmingSettingsFactory.pestExchangeFovRangeSetting()
                        .visibleWhen(() -> SlateConfig.AUTO_PEST_EXCHANGE.get()));

        group.add(new menear.nclient.slate.ui.settings.ToggleSetting("Use Abiphone",
                () -> SlateConfig.AUTO_PEST_USE_ABIPHONE.get(),
                v -> {
                    SlateConfig.AUTO_PEST_USE_ABIPHONE.set(v);
                    SlateConfig.save();
                })
                .visibleWhen(() -> SlateConfig.AUTO_PEST_EXCHANGE.get()));

        return MainGUIRegistry.toggleSubTab(
                "Auto Pest Exchange",
                "Automatically visits the pest exchange desk when ready",
                () -> SlateConfig.AUTO_PEST_EXCHANGE.get(),
                v -> {
                    SlateConfig.AUTO_PEST_EXCHANGE.set(v);
                    SlateConfig.save();
                },
                List.of(group));
    }
}
