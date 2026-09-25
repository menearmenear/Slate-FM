package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.hud.HudEditScreen;
import menear.nclient.slate.modules.visuals.StreamerModeManager;
import menear.nclient.slate.ui.settings.ActionSetting;
import menear.nclient.slate.ui.settings.ColorSetting;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.MultiDropdownSetting;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;
import menear.nclient.slate.ui.settings.TextSetting;
import menear.nclient.slate.ui.settings.ToggleSetting;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public final class HudVisualsRegistryProvider extends AbstractVisualsRegistryProvider {
    public HudVisualsRegistryProvider() {
        super(0);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        List<SettingGroup> groups = new ArrayList<>();

        groups.add(SettingGroup.alwaysOn(
                        "HUD Settings",
                        "Configure overlay style and layout")
                .add(new MultiDropdownSetting("HUD Themes",
                        List.of("Main", "Watermark"),
                        () -> SlateConfig.HUD_THEME.get(),
                        i -> {
                            SlateConfig.HUD_THEME.set(i);
                            SlateConfig.save();
                        }))
                .add(new ActionSetting("Edit HUD Layout", () -> Minecraft.getInstance().setScreen(new HudEditScreen()))));

        groups.add(SettingGroup.alwaysOn(
                        "HUD Visibility",
                        "Controls when HUD overlays are shown")
                .add(new ToggleSetting("Only Show While Macro Running",
                        () -> SlateConfig.HUD_ONLY_WHILE_MACRO_RUNNING.get(),
                        v -> {
                            SlateConfig.HUD_ONLY_WHILE_MACRO_RUNNING.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Only Show HUDs In Garden",
                        () -> SlateConfig.GUI_ONLY_IN_GARDEN.get(),
                        v -> {
                            SlateConfig.GUI_ONLY_IN_GARDEN.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Show Task HUDs Outside Garden",
                        () -> SlateConfig.SHOW_HUD_OUTSIDE_GARDEN.get(),
                        v -> {
                            SlateConfig.SHOW_HUD_OUTSIDE_GARDEN.set(v);
                            SlateConfig.save();
                        })
                        .visibleWhen(() -> !SlateConfig.GUI_ONLY_IN_GARDEN.get())));

        groups.add(SettingGroup.of(
                        "Streamer Mode",
                        "Hides Slate chat, notifications, overlays, HUDs, and world visuals",
                        StreamerModeManager::isEnabled,
                        StreamerModeManager::setEnabled));

        groups.add(SettingGroup.alwaysOn(
                        "Main Status HUD",
                        "Settings for the all-in-one main status card")
                .add(new ToggleSetting("Gradient",
                        () -> SlateConfig.MAIN_STATUS_GRADIENT.get(),
                        v -> { SlateConfig.MAIN_STATUS_GRADIENT.set(v); SlateConfig.save(); }))
                .add(new ColorSetting("Gradient Left",
                        () -> SlateConfig.MAIN_STATUS_GRADIENT_LEFT.get(),
                        v -> { SlateConfig.MAIN_STATUS_GRADIENT_LEFT.set(v); SlateConfig.save(); })
                        .visibleWhen(() -> SlateConfig.MAIN_STATUS_GRADIENT.get()))
                .add(new ColorSetting("Gradient Right",
                        () -> SlateConfig.MAIN_STATUS_GRADIENT_RIGHT.get(),
                        v -> { SlateConfig.MAIN_STATUS_GRADIENT_RIGHT.set(v); SlateConfig.save(); })
                        .visibleWhen(() -> SlateConfig.MAIN_STATUS_GRADIENT.get())));

        groups.add(SettingGroup.alwaysOn(
                        "Debug HUD",
                        "Developer overlays for macro state and task tracking")
                .add(new ToggleSetting("Macro HUD",
                        () -> SlateConfig.SHOW_HUD.get(),
                        v -> {
                            SlateConfig.SHOW_HUD.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Intermediaries HUD",
                        () -> SlateConfig.SHOW_INTERMEDIARIES_HUD.get(),
                        v -> {
                            SlateConfig.SHOW_INTERMEDIARIES_HUD.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Mid Farming HUD",
                        () -> SlateConfig.SHOW_MID_FARMING_HUD.get(),
                        v -> {
                            SlateConfig.SHOW_MID_FARMING_HUD.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Failsafes HUD",
                        () -> SlateConfig.SHOW_FAILSAFES_HUD.get(),
                        v -> {
                            SlateConfig.SHOW_FAILSAFES_HUD.set(v);
                            SlateConfig.save();
                        })));

        groups.add(SettingGroup.alwaysOn(
                        "Inventory HUD",
                        "Configure the inventory preview overlay")
                .add(new ToggleSetting("Inventory HUD",
                        () -> SlateConfig.SHOW_INVENTORY_HUD.get(),
                        v -> {
                            SlateConfig.SHOW_INVENTORY_HUD.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Show Player Model",
                        () -> SlateConfig.INVENTORY_HUD_SHOW_PLAYER_MODEL.get(),
                        v -> {
                            SlateConfig.INVENTORY_HUD_SHOW_PLAYER_MODEL.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Show Armor",
                        () -> SlateConfig.INVENTORY_HUD_SHOW_ARMOR.get(),
                        v -> {
                            SlateConfig.INVENTORY_HUD_SHOW_ARMOR.set(v);
                            SlateConfig.save();
                        })));

        SettingGroup discordStatus = SettingGroup.of(
                "Discord Status",
                "Sends macro status updates to a Discord webhook",
                () -> SlateConfig.SEND_DISCORD_STATUS.get(),
                v -> {
                    SlateConfig.SEND_DISCORD_STATUS.set(v);
                    SlateConfig.save();
                });
        discordStatus.add(new TextSetting("Webhook URL", "https://discord.com/api/webhooks/...",
                () -> SlateConfig.DISCORD_WEBHOOK_URL.get(),
                v -> {
                    SlateConfig.DISCORD_WEBHOOK_URL.set(v);
                    SlateConfig.save();
                })
                .visibleWhen(() -> SlateConfig.SEND_DISCORD_STATUS.get()));
        discordStatus.add(new SliderSetting("Update Interval", 1, 60,
                () -> (float) SlateConfig.DISCORD_STATUS_UPDATE_TIME.get(),
                v -> {
                    SlateConfig.DISCORD_STATUS_UPDATE_TIME.set(Math.round(v));
                    SlateConfig.save();
                })
                .withDecimals(0).withSuffix(" min")
                .visibleWhen(() -> SlateConfig.SEND_DISCORD_STATUS.get()));
        groups.add(discordStatus);

        SettingGroup watermark = SettingGroup.alwaysOn(
                "Watermark",
                "Displays mod name, username, FPS, ping and time");
        watermark.add(new ToggleSetting("Show Macro Status",
                () -> SlateConfig.WATERMARK_SHOW_MACRO_STATUS.get(),
                v -> { SlateConfig.WATERMARK_SHOW_MACRO_STATUS.set(v); SlateConfig.save(); }));
        watermark.add(new ToggleSetting("Show Logo",
                () -> SlateConfig.WATERMARK_SHOW_LOGO.get(),
                v -> {
                    if (!v && !SlateConfig.WATERMARK_SHOW_NAME.get()) return;
                    SlateConfig.WATERMARK_SHOW_LOGO.set(v);
                    SlateConfig.save();
                }));
        watermark.add(new ToggleSetting("Show Name",
                () -> SlateConfig.WATERMARK_SHOW_NAME.get(),
                v -> {
                    if (!v && !SlateConfig.WATERMARK_SHOW_LOGO.get()) return;
                    SlateConfig.WATERMARK_SHOW_NAME.set(v);
                    SlateConfig.save();
                }));
        watermark.add(new TextSetting("Custom Username", "leave empty to use your real name",
                () -> SlateConfig.WATERMARK_CUSTOM_USERNAME.get(),
                v -> {
                    SlateConfig.WATERMARK_CUSTOM_USERNAME.set(v);
                    SlateConfig.save();
                }));
        watermark.add(new ToggleSetting("Show Username",
                () -> SlateConfig.WATERMARK_SHOW_USERNAME.get(),
                v -> { SlateConfig.WATERMARK_SHOW_USERNAME.set(v); SlateConfig.save(); }));
        watermark.add(new ToggleSetting("Show FPS",
                () -> SlateConfig.WATERMARK_SHOW_FPS.get(),
                v -> { SlateConfig.WATERMARK_SHOW_FPS.set(v); SlateConfig.save(); }));
        watermark.add(new ToggleSetting("Show Ping",
                () -> SlateConfig.WATERMARK_SHOW_PING.get(),
                v -> { SlateConfig.WATERMARK_SHOW_PING.set(v); SlateConfig.save(); }));
        watermark.add(new ToggleSetting("Show Time",
                () -> SlateConfig.WATERMARK_SHOW_TIME.get(),
                v -> { SlateConfig.WATERMARK_SHOW_TIME.set(v); SlateConfig.save(); }));
        watermark.add(new ToggleSetting("Gradient",
                () -> SlateConfig.WATERMARK_GRADIENT.get(),
                v -> { SlateConfig.WATERMARK_GRADIENT.set(v); SlateConfig.save(); }));
        watermark.add(new ColorSetting("Gradient Left",
                () -> SlateConfig.WATERMARK_GRADIENT_LEFT.get(),
                v -> { SlateConfig.WATERMARK_GRADIENT_LEFT.set(v); SlateConfig.save(); })
                .visibleWhen(() -> SlateConfig.WATERMARK_GRADIENT.get()));
        watermark.add(new ColorSetting("Gradient Right",
                () -> SlateConfig.WATERMARK_GRADIENT_COLOR.get(),
                v -> { SlateConfig.WATERMARK_GRADIENT_COLOR.set(v); SlateConfig.save(); })
                .visibleWhen(() -> SlateConfig.WATERMARK_GRADIENT.get()));
        groups.add(watermark);

        return MainGUIRegistry.subTab("HUD", "Controls which HUD overlays are visible", groups);
    }
}
