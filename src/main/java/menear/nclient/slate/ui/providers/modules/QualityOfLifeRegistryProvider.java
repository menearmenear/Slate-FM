package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.ui.settings.ListSetting;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;
import menear.nclient.slate.ui.settings.TextSetting;
import menear.nclient.slate.ui.settings.ToggleSetting;

import java.util.ArrayList;
import java.util.List;

public final class QualityOfLifeRegistryProvider extends AbstractModulesRegistryProvider {
    public QualityOfLifeRegistryProvider() {
        super(9);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        List<SettingGroup> groups = new ArrayList<>();

        groups.add(SettingGroup.of(
                        "Book Combine",
                        "Automatically combines books when the threshold is met",
                        () -> SlateConfig.AUTO_BOOK_COMBINE.get(),
                        v -> {
                            SlateConfig.AUTO_BOOK_COMBINE.set(v);
                            SlateConfig.save();
                        })
                .add(new ToggleSetting("Always Active",
                        () -> SlateConfig.ALWAYS_ACTIVE_COMBINE.get(),
                        v -> {
                            SlateConfig.ALWAYS_ACTIVE_COMBINE.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> SlateConfig.AUTO_BOOK_COMBINE.get()))
                .add(new SliderSetting("Book Threshold", 1, 20,
                        () -> (float) SlateConfig.BOOK_THRESHOLD.get(),
                        v -> {
                            SlateConfig.BOOK_THRESHOLD.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0)
                        .visibleWhen(() -> SlateConfig.AUTO_BOOK_COMBINE.get()))
                .add(new SliderSetting("Book Combine Delay", 50, 5000,
                        () -> (float) SlateConfig.BOOK_COMBINE_DELAY.get(),
                        v -> {
                            SlateConfig.BOOK_COMBINE_DELAY.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix("ms")
                        .visibleWhen(() -> SlateConfig.AUTO_BOOK_COMBINE.get()))
                .add(new ListSetting("Custom Enchantment Levels", "Add Name:Level entry",
                        () -> SlateConfig.CUSTOM_ENCHANTMENT_LEVELS.get(),
                        v -> {
                            SlateConfig.CUSTOM_ENCHANTMENT_LEVELS.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> SlateConfig.AUTO_BOOK_COMBINE.get())));

        groups.add(SettingGroup.of(
                        "Auto George Sell",
                        "Automatically sells pets through George",
                        () -> SlateConfig.AUTO_GEORGE_SELL.get(),
                        v -> {
                            SlateConfig.AUTO_GEORGE_SELL.set(v);
                            SlateConfig.save();
                        })
                .add(new SliderSetting("George Sell Threshold", 1, 10,
                        () -> (float) SlateConfig.GEORGE_SELL_THRESHOLD.get(),
                        v -> {
                            SlateConfig.GEORGE_SELL_THRESHOLD.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0)
                        .visibleWhen(() -> SlateConfig.AUTO_GEORGE_SELL.get()))
                .add(FarmingSettingsFactory.georgePostSellDelaySetting()
                        .visibleWhen(() -> SlateConfig.AUTO_GEORGE_SELL.get()))
                .add(FarmingSettingsFactory.farmWhileCallingGeorgeSetting()
                        .visibleWhen(() -> SlateConfig.AUTO_GEORGE_SELL.get())));

        groups.add(SettingGroup.of(
                        "Auto Sell",
                        "Automatically sells configured items",
                        QualityOfLifeRegistryProvider::isAutoSellCategoryEnabled,
                        QualityOfLifeRegistryProvider::setAutoSellCategoryEnabled)
                .add(new SliderSetting("Inventory Threshold", 1, 100,
                        () -> (float) SlateConfig.AUTO_SELL_THRESHOLD.get(),
                        v -> {
                            SlateConfig.AUTO_SELL_THRESHOLD.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix("%")
                        .visibleWhen(() -> SlateConfig.AUTO_SELL.get()))
                .add(new SliderSetting("Inventory Full Time", 1, 30,
                        () -> (float) SlateConfig.AUTO_SELL_TIME.get(),
                        v -> {
                            SlateConfig.AUTO_SELL_TIME.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix("s")
                        .visibleWhen(() -> SlateConfig.AUTO_SELL.get()))
                .add(new ToggleSetting("NPC Autosell",
                        () -> SlateConfig.AUTO_SELL_NPC.get(),
                        v -> {
                            SlateConfig.AUTO_SELL_NPC.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> SlateConfig.AUTO_SELL.get()))
                .add(new ToggleSetting("Bazaar Autosell",
                        () -> SlateConfig.AUTO_SELL_BAZAAR.get(),
                        v -> {
                            SlateConfig.AUTO_SELL_BAZAAR.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> SlateConfig.AUTO_SELL.get()))
                .add(new ToggleSetting("Sell Before Visitors",
                        () -> SlateConfig.AUTO_SELL_BEFORE_VISITORS.get(),
                        v -> {
                            SlateConfig.AUTO_SELL_BEFORE_VISITORS.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> SlateConfig.AUTO_SELL.get()))
                .add(new ToggleSetting("Sell Before Pest Traps",
                        () -> SlateConfig.AUTO_SELL_BEFORE_PEST_TRAPS.get(),
                        v -> {
                            SlateConfig.AUTO_SELL_BEFORE_PEST_TRAPS.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> SlateConfig.AUTO_SELL.get()))
                .add(new ToggleSetting("Auto Sell (Passive)",
                        () -> SlateConfig.AUTOSELL_PASSIVE.get(),
                        v -> {
                            SlateConfig.AUTOSELL_PASSIVE.set(v);
                            SlateConfig.save();
                        }))
                .add(new ListSetting("Auto Sell Items", "Add item name",
                        () -> SlateConfig.AUTO_SELL_ITEMS.get(),
                        v -> {
                            SlateConfig.AUTO_SELL_ITEMS.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> SlateConfig.AUTO_SELL.get() || SlateConfig.AUTOSELL_PASSIVE.get())));

        groups.add(SettingGroup.of(
                        "Stash Manager",
                        "Automatically picks items up from stash",
                        () -> SlateConfig.AUTO_STASH_MANAGER.get(),
                        v -> {
                            SlateConfig.AUTO_STASH_MANAGER.set(v);
                            SlateConfig.save();
                        })
                .add(FarmingSettingsFactory.pickUpStashDelaySetting()
                        .visibleWhen(() -> SlateConfig.AUTO_STASH_MANAGER.get())));

        groups.add(SettingGroup.of(
                        "Junk Manager",
                        "Drops configured junk items once the threshold is reached",
                        () -> SlateConfig.AUTO_DROP_JUNK.get(),
                        v -> {
                            SlateConfig.AUTO_DROP_JUNK.set(v);
                            SlateConfig.save();
                        })
                .add(new SliderSetting("Junk Threshold", 1, 10,
                        () -> (float) SlateConfig.JUNK_THRESHOLD.get(),
                        v -> {
                            SlateConfig.JUNK_THRESHOLD.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix(" items")
                        .visibleWhen(() -> SlateConfig.AUTO_DROP_JUNK.get()))
                .add(FarmingSettingsFactory.junkDropDelaySetting()
                        .visibleWhen(() -> SlateConfig.AUTO_DROP_JUNK.get()))
                .add(new TextSetting("Drop at Plot TP", "Plot number (e.g. 5)",
                        () -> SlateConfig.DROP_JUNK_PLOT_TP.get(),
                        v -> {
                            SlateConfig.DROP_JUNK_PLOT_TP.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> SlateConfig.AUTO_DROP_JUNK.get()))
                .add(new ListSetting("Junk Items", "Add item name",
                        () -> SlateConfig.JUNK_ITEMS.get(),
                        v -> {
                            SlateConfig.JUNK_ITEMS.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> SlateConfig.AUTO_DROP_JUNK.get())));

        groups.add(SettingGroup.alwaysOn(
                        "Chat Filtering",
                        "Controls chat cleanup for farming-related messages")
                .add(new ToggleSetting("Hide Pest Drops",
                        () -> SlateConfig.HIDE_FILTERED_CHAT.get(),
                        v -> {
                            SlateConfig.HIDE_FILTERED_CHAT.set(v);
                            SlateConfig.save();
                        })));

        return MainGUIRegistry.subTab(
                "Farming QOL",
                "Various quality-of-life features for farming",
                groups);
    }

    private static boolean isAutoSellCategoryEnabled() {
        return SlateConfig.AUTO_SELL.get() || SlateConfig.AUTOSELL_PASSIVE.get();
    }

    private static void setAutoSellCategoryEnabled(boolean enabled) {
        SlateConfig.AUTO_SELL.set(enabled);
        if (!enabled) {
            SlateConfig.AUTOSELL_PASSIVE.set(false);
        }
        SlateConfig.save();
    }
}
