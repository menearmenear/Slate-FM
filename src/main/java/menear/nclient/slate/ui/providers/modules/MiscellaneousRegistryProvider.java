package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.config.ConfigHelpers;
import menear.nclient.slate.config.UnflyMode;
import menear.nclient.slate.ui.settings.DropdownSetting;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;
import menear.nclient.slate.ui.settings.ToggleSetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MiscellaneousRegistryProvider extends AbstractModulesRegistryProvider {
    public MiscellaneousRegistryProvider() {
        super(11);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        List<SettingGroup> groups = new ArrayList<>();

        groups.add(SettingGroup.alwaysOn(
                        "Miscellaneous",
                        "Miscellaneous settings")
                .add(new DropdownSetting("Unfly Mode",
                        List.of("Sneak", "2x Tap Space"),
                        () -> SlateConfig.UNFLY_MODE.get().length() > 0
                                ? Arrays.asList(UnflyMode.values()).indexOf(ConfigHelpers.getUnflyMode())
                                : 0,
                        i -> {
                            SlateConfig.UNFLY_MODE.set(UnflyMode.values()[i].name());
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Show Debug",
                        () -> SlateConfig.SHOW_DEBUG.get(),
                        v -> {
                            SlateConfig.SHOW_DEBUG.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Persist Session Timer",
                        () -> SlateConfig.PERSIST_SESSION_TIMER.get(),
                        v -> {
                            SlateConfig.PERSIST_SESSION_TIMER.set(v);
                            SlateConfig.save();
                        }))
                .add(new SliderSetting("Pathfinder Max Jump Height", 1, 6,
                        () -> (float) SlateConfig.PATHFINDER_MAX_JUMP_HEIGHT.get(),
                        v -> {
                            SlateConfig.PATHFINDER_MAX_JUMP_HEIGHT.set(Math.round(v));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix(" blocks")));

        groups.add(SettingGroup.alwaysOn(
                        "Macro Settings",
                        "Macro-specific client behavior")
                .add(new ToggleSetting("Ungrab Mouse",
                        () -> SlateConfig.MACRO_UNGRAB_MOUSE.get(),
                        v -> {
                            SlateConfig.MACRO_UNGRAB_MOUSE.set(v);
                            SlateConfig.save();
                        }))
                .add(new ToggleSetting("Mute Game",
                        () -> SlateConfig.MUTE_GAME.get(),
                        v -> {
                            SlateConfig.MUTE_GAME.set(v);
                            SlateConfig.save();
                        }))
                .add(new SliderSetting("Game Volume", 0, 100,
                        () -> SlateConfig.MUTE_GAME_VOLUME.get() * 100.0f,
                        v -> {
                            SlateConfig.MUTE_GAME_VOLUME.set(clamp01(v / 100.0f));
                            SlateConfig.save();
                        })
                        .withDecimals(0).withSuffix("%")
                        .visibleWhen(() -> SlateConfig.MUTE_GAME.get()))
                .add(new ToggleSetting("Keep Focus",
                        () -> SlateConfig.KEEP_FOCUS.get(),
                        v -> {
                            SlateConfig.KEEP_FOCUS.set(v);
                            SlateConfig.save();
                        })));

        SettingGroup performanceMode = SettingGroup.of(
                "Performance Mode",
                "Lowers render distance and limits FPS during macro execution",
                () -> SlateConfig.PERFORMANCE_MODE.get(),
                v -> {
                    SlateConfig.PERFORMANCE_MODE.set(v);
                    SlateConfig.save();
                });
        performanceMode.add(new ToggleSetting("Limit FPS",
                () -> SlateConfig.PERFORMANCE_LIMIT_FPS.get(),
                v -> {
                    SlateConfig.PERFORMANCE_LIMIT_FPS.set(v);
                    SlateConfig.save();
                }));
        performanceMode.add(new SliderSetting("Max FPS", 20, 60,
                () -> (float) SlateConfig.PERFORMANCE_MODE_MAX_FPS.get(),
                v -> {
                    SlateConfig.PERFORMANCE_MODE_MAX_FPS.set(Math.round(v));
                    SlateConfig.save();
                })
                .withDecimals(0).withSuffix(" fps")
                .visibleWhen(() -> SlateConfig.PERFORMANCE_LIMIT_FPS.get()));
        performanceMode.add(new ToggleSetting("Limit Chunk Distance",
                () -> SlateConfig.PERFORMANCE_LIMIT_CHUNK_DISTANCE.get(),
                v -> {
                    SlateConfig.PERFORMANCE_LIMIT_CHUNK_DISTANCE.set(v);
                    SlateConfig.save();
                }));
        performanceMode.add(new SliderSetting("Chunk Distance", 2, 8,
                () -> (float) SlateConfig.PERFORMANCE_CHUNK_DISTANCE.get(),
                v -> {
                    SlateConfig.PERFORMANCE_CHUNK_DISTANCE.set(Math.round(v));
                    SlateConfig.save();
                })
                .withDecimals(0).withSuffix(" chunks")
                .visibleWhen(() -> SlateConfig.PERFORMANCE_LIMIT_CHUNK_DISTANCE.get()));
        performanceMode.add(new ToggleSetting("Disable Block Breaking Particles",
                () -> SlateConfig.PERFORMANCE_DISABLE_PARTICLES.get(),
                v -> {
                    SlateConfig.PERFORMANCE_DISABLE_PARTICLES.set(v);
                    SlateConfig.save();
                }));
        groups.add(performanceMode);

        return MainGUIRegistry.subTab(
                "Miscellaneous",
                "Miscellaneous settings",
                groups);
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
