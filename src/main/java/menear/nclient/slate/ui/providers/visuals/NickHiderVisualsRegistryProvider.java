package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.ui.settings.ListSetting;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.TextSetting;
import menear.nclient.slate.ui.settings.ToggleSetting;

import java.util.ArrayList;
import java.util.List;

public final class NickHiderVisualsRegistryProvider extends AbstractVisualsRegistryProvider {
    public NickHiderVisualsRegistryProvider() {
        super(1);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        List<SettingGroup> groups = new ArrayList<>();

        groups.add(SettingGroup.alwaysOn(
                        "Username",
                        "Spoofs your username in chat and overlays")
                .add(new ToggleSetting("Enable Username Spoof",
                        () -> SlateConfig.NICK_HIDER_ENABLED.get(),
                        v -> {
                            SlateConfig.NICK_HIDER_ENABLED.set(v);
                            SlateConfig.save();
                        }))
                .add(new TextSetting("Custom Username", "SlateUser",
                        () -> SlateConfig.CUSTOM_USERNAME.get(),
                        v -> {
                            SlateConfig.CUSTOM_USERNAME.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> SlateConfig.NICK_HIDER_ENABLED.get()))
                .add(new ToggleSetting("Hide Skin",
                        () -> SlateConfig.HIDE_SKIN.get(),
                        v -> {
                            SlateConfig.HIDE_SKIN.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> SlateConfig.NICK_HIDER_ENABLED.get())));

        groups.add(SettingGroup.of(
                        "Hide Server ID",
                        "Replaces the server identifier shown in tablist and scoreboard",
                        () -> SlateConfig.HIDE_SERVER_ID.get(),
                        v -> {
                            SlateConfig.HIDE_SERVER_ID.set(v);
                            SlateConfig.save();
                        })
                .add(new TextSetting("Custom Server ID", "slate.cat",
                        () -> SlateConfig.CUSTOM_SERVER_ID.get(),
                        v -> {
                            SlateConfig.CUSTOM_SERVER_ID.set(v);
                            SlateConfig.save();
                        })));

        groups.add(SettingGroup.of(
                        "Coop Name Hider",
                        "Obfuscates configured coop names",
                        () -> SlateConfig.COOP_HIDER_ENABLED.get(),
                        v -> {
                            SlateConfig.COOP_HIDER_ENABLED.set(v);
                            SlateConfig.save();
                        })
                .add(new ListSetting("Coop Names", "Add coop name",
                        () -> SlateConfig.COOP_NAMES.get(),
                        v -> {
                            SlateConfig.COOP_NAMES.set(v);
                            SlateConfig.save();
                        })));

        SettingGroup spoofValues = SettingGroup.of(
                "Spoof Values",
                "Customise SkyBlock level and identifiable values",
                () -> SlateConfig.SPOOF_VALUES_ENABLED.get(),
                v -> {
                    SlateConfig.SPOOF_VALUES_ENABLED.set(v);
                    SlateConfig.save();
                });
        spoofValues.add(new ToggleSetting("Custom Skyblock Level",
                () -> SlateConfig.CUSTOM_SB_LEVEL_ENABLED.get(),
                v -> {
                    SlateConfig.CUSTOM_SB_LEVEL_ENABLED.set(v);
                    SlateConfig.save();
                }).visibleWhen(() -> SlateConfig.SPOOF_VALUES_ENABLED.get()));
        spoofValues.add(new TextSetting("SkyBlock Level Override", "0",
                () -> Integer.toString(SlateConfig.CUSTOM_SB_LEVEL.get()),
                v -> saveIntValue(v, SlateConfig.CUSTOM_SB_LEVEL::set))
                .visibleWhen(() -> SlateConfig.SPOOF_VALUES_ENABLED.get() && SlateConfig.CUSTOM_SB_LEVEL_ENABLED.get()));
        spoofValues.add(new TextSetting("Purse Offset", "0",
                () -> formatDecimal(SlateConfig.PURSE_OFFSET.get()),
                v -> saveDoubleValue(v, SlateConfig.PURSE_OFFSET::set))
                .visibleWhen(() -> SlateConfig.SPOOF_VALUES_ENABLED.get()));
        spoofValues.add(new TextSetting("Bits Offset", "0",
                () -> formatDecimal(SlateConfig.BITS_OFFSET.get()),
                v -> saveDoubleValue(v, SlateConfig.BITS_OFFSET::set))
                .visibleWhen(() -> SlateConfig.SPOOF_VALUES_ENABLED.get()));
        spoofValues.add(new TextSetting("Copper Offset", "0",
                () -> formatDecimal(SlateConfig.COPPER_OFFSET.get()),
                v -> saveDoubleValue(v, SlateConfig.COPPER_OFFSET::set))
                .visibleWhen(() -> SlateConfig.SPOOF_VALUES_ENABLED.get()));
        spoofValues.add(new TextSetting("Sawdust Offset", "0",
                () -> formatDecimal(SlateConfig.SAWDUST_OFFSET.get()),
                v -> saveDoubleValue(v, SlateConfig.SAWDUST_OFFSET::set))
                .visibleWhen(() -> SlateConfig.SPOOF_VALUES_ENABLED.get()));
        spoofValues.add(new TextSetting("Farming XP Offset", "0",
                () -> formatDecimal(SlateConfig.FARMING_EXP_OFFSET.get()),
                v -> saveDoubleValue(v, SlateConfig.FARMING_EXP_OFFSET::set))
                .visibleWhen(() -> SlateConfig.SPOOF_VALUES_ENABLED.get()));
        groups.add(spoofValues);

        return MainGUIRegistry.toggleSubTab(
                "Nick Hider",
                "Spoofs names, server IDs, coop names, and identifiable values",
                () -> SlateConfig.NICK_HIDER_MASTER_ENABLED.get(),
                v -> {
                    SlateConfig.NICK_HIDER_MASTER_ENABLED.set(v);
                    SlateConfig.save();
                },
                groups);
    }

    private static void saveIntValue(String value, java.util.function.IntConsumer setter) {
        try {
            setter.accept(Integer.parseInt(value.trim()));
            SlateConfig.save();
        } catch (NumberFormatException ignored) {
        }
    }

    private static void saveDoubleValue(String value, java.util.function.Consumer<Double> setter) {
        try {
            setter.accept(Double.parseDouble(value.trim()));
            SlateConfig.save();
        } catch (NumberFormatException ignored) {
        }
    }

    private static String formatDecimal(double value) {
        if (value == Math.rint(value)) {
            return Long.toString(Math.round(value));
        }
        return Double.toString(value);
    }
}
