package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.ui.settings.RangeSliderSetting;
import menear.nclient.slate.ui.settings.SliderSetting;
import menear.nclient.slate.ui.settings.ToggleSetting;

final class FarmingSettingsFactory {
    private FarmingSettingsFactory() {}

    static java.util.List<String> sprayMaterials() {
        // Include an explicit "Use selected" option as the first choice so users can
        // opt to let the sprayonator use whatever material is currently selected.
        return java.util.List.of("Use Selected", "Compost", "Honey Jar", "Dung", "Plant Matter",
                "Tasty Cheese", "Jelly");
    }

    private static RangeSliderSetting intDelayRangeSetting(String name,
                                                           float minBound,
                                                           float maxBound,
                                                           java.util.function.Supplier<Integer> minGetter,
                                                           java.util.function.Supplier<Integer> maxGetter,
                                                           java.util.function.BiConsumer<Integer, Integer> setter) {
        return new RangeSliderSetting(name, minBound, maxBound,
                () -> minGetter.get().floatValue(),
                () -> maxGetter.get().floatValue(),
                (lower, upper) -> {
                    setter.accept(Math.round(lower), Math.round(upper));
                    SlateConfig.save();
                })
                .withDecimals(0).withSuffix("ms");
    }

    static RangeSliderSetting laneSwitchDelaySetting() {
        return intDelayRangeSetting("Lane Switch Delay", 0f, 1000f,
                () -> SlateConfig.MACRO_LANE_SWITCH_DELAY_MIN.get(),
                () -> SlateConfig.MACRO_LANE_SWITCH_DELAY_MAX.get(),
                (min, max) -> {
                    SlateConfig.MACRO_LANE_SWITCH_DELAY_MIN.set(min);
                    SlateConfig.MACRO_LANE_SWITCH_DELAY_MAX.set(max);
                });
    }

    static RangeSliderSetting rewarpDelaySetting() {
        return intDelayRangeSetting("Rewarp Delay", 0f, 1000f,
                () -> SlateConfig.REWARP_DELAY_MIN.get(),
                () -> SlateConfig.REWARP_DELAY_MAX.get(),
                (min, max) -> {
                    SlateConfig.REWARP_DELAY_MIN.set(min);
                    SlateConfig.REWARP_DELAY_MAX.set(max);
                });
    }

    static RangeSliderSetting pestDestroyerTriggerDelaySetting() {
        return intDelayRangeSetting("Pest Destroyer Trigger Delay", 0f, 5000f,
                () -> SlateConfig.PEST_CHAT_TRIGGER_DELAY_MIN.get(),
                () -> SlateConfig.PEST_CHAT_TRIGGER_DELAY_MAX.get(),
                (min, max) -> {
                    SlateConfig.PEST_CHAT_TRIGGER_DELAY_MIN.set(min);
                    SlateConfig.PEST_CHAT_TRIGGER_DELAY_MAX.set(max);
                });
    }

    static RangeSliderSetting pestExchangeDelaySetting() {
        return intDelayRangeSetting("Pest Exchange Delay", 0f, 5000f,
                () -> SlateConfig.PEST_EXCHANGE_DELAY_MIN.get(),
                () -> SlateConfig.PEST_EXCHANGE_DELAY_MAX.get(),
                (min, max) -> {
                    SlateConfig.PEST_EXCHANGE_DELAY_MIN.set(min);
                    SlateConfig.PEST_EXCHANGE_DELAY_MAX.set(max);
                });
    }

    static RangeSliderSetting aotvBetweenPestsDelaySetting() {
        return intDelayRangeSetting("AOTV Between Pests Delay", 100f, 250f,
                () -> SlateConfig.PEST_AOTV_DELAY_MIN.get(),
                () -> SlateConfig.PEST_AOTV_DELAY_MAX.get(),
                (min, max) -> {
                    SlateConfig.PEST_AOTV_DELAY_MIN.set(min);
                    SlateConfig.PEST_AOTV_DELAY_MAX.set(max);
                });
    }

    static RangeSliderSetting rodSwapDelaySetting() {
        return intDelayRangeSetting("Rod Swap Delay", 0f, 1000f,
                () -> SlateConfig.ROD_SWAP_DELAY_MIN.get(),
                () -> SlateConfig.ROD_SWAP_DELAY_MAX.get(),
                (min, max) -> {
                    SlateConfig.ROD_SWAP_DELAY_MIN.set(min);
                    SlateConfig.ROD_SWAP_DELAY_MAX.set(max);
                });
    }

    static RangeSliderSetting guiFirstClickDelaySetting() {
        return intDelayRangeSetting("GUI First Click Delay", 0f, 1000f,
                () -> SlateConfig.GUI_FIRST_CLICK_DELAY_MIN.get(),
                () -> SlateConfig.GUI_FIRST_CLICK_DELAY_MAX.get(),
                (min, max) -> {
                    SlateConfig.GUI_FIRST_CLICK_DELAY_MIN.set(min);
                    SlateConfig.GUI_FIRST_CLICK_DELAY_MAX.set(max);
                });
    }

    static RangeSliderSetting guiClickDelaySetting() {
        return intDelayRangeSetting("Gear Swap GUI Delay", 0f, 1000f,
                () -> SlateConfig.GUI_CLICK_DELAY_MIN.get(),
                () -> SlateConfig.GUI_CLICK_DELAY_MAX.get(),
                (min, max) -> {
                    SlateConfig.GUI_CLICK_DELAY_MIN.set(min);
                    SlateConfig.GUI_CLICK_DELAY_MAX.set(max);
                });
    }

    static RangeSliderSetting pickUpStashDelaySetting() {
        return intDelayRangeSetting("Pick Up Stash Delay", 0f, 5000f,
                () -> SlateConfig.PICK_UP_STASH_DELAY_MIN.get(),
                () -> SlateConfig.PICK_UP_STASH_DELAY_MAX.get(),
                (min, max) -> {
                    SlateConfig.PICK_UP_STASH_DELAY_MIN.set(min);
                    SlateConfig.PICK_UP_STASH_DELAY_MAX.set(max);
                });
    }

    static RangeSliderSetting junkDropDelaySetting() {
        return intDelayRangeSetting("Junk Drop Delay", 0f, 1000f,
                () -> SlateConfig.JUNK_ITEM_DROP_DELAY_MIN.get(),
                () -> SlateConfig.JUNK_ITEM_DROP_DELAY_MAX.get(),
                (min, max) -> {
                    SlateConfig.JUNK_ITEM_DROP_DELAY_MIN.set(min);
                    SlateConfig.JUNK_ITEM_DROP_DELAY_MAX.set(max);
                });
    }

    static RangeSliderSetting georgePostSellDelaySetting() {
        return intDelayRangeSetting("George Sell Delay Between Pets", 0f, 5000f,
                () -> SlateConfig.GEORGE_POST_SELL_DELAY_MIN_MS.get(),
                () -> SlateConfig.GEORGE_POST_SELL_DELAY_MAX_MS.get(),
                (min, max) -> {
                    SlateConfig.GEORGE_POST_SELL_DELAY_MIN_MS.set(min);
                    SlateConfig.GEORGE_POST_SELL_DELAY_MAX_MS.set(max);
                });
    }

    static ToggleSetting farmWhileCallingGeorgeSetting() {
        return new ToggleSetting("Farm while calling George",
                () -> SlateConfig.FARM_WHILE_CALLING_GEORGE.get(),
                v -> {
                    SlateConfig.FARM_WHILE_CALLING_GEORGE.set(v);
                    SlateConfig.save();
                });
    }

    static RangeSliderSetting bazaarGuiDelaySetting() {
        return intDelayRangeSetting("Bazaar GUI Delay", 0f, 1000f,
                () -> SlateConfig.BAZAAR_DELAY_MIN.get(),
                () -> SlateConfig.BAZAAR_DELAY_MAX.get(),
                (min, max) -> {
                    SlateConfig.BAZAAR_DELAY_MIN.set(min);
                    SlateConfig.BAZAAR_DELAY_MAX.set(max);
                });
    }

    static SliderSetting farmingPitchRangeSetting() {
        return new SliderSetting("Farming Pitch Range", 0, 10,
                () -> SlateConfig.MACRO_CUSTOM_PITCH_HUMANIZATION.get(),
                v -> {
                    SlateConfig.MACRO_CUSTOM_PITCH_HUMANIZATION.set(v);
                    SlateConfig.save();
                })
                .withDecimals(1).withSuffix("\u00B0");
    }

    static SliderSetting farmingYawRangeSetting() {
        return new SliderSetting("Farming Yaw Range", 0, 10,
                () -> SlateConfig.MACRO_CUSTOM_YAW_HUMANIZATION.get(),
                v -> {
                    SlateConfig.MACRO_CUSTOM_YAW_HUMANIZATION.set(v);
                    SlateConfig.save();
                })
                .withDecimals(1).withSuffix("\u00B0");
    }

    static SliderSetting bpsAverageWindowSetting() {
        return new SliderSetting("BPS Average Window", 5, 60,
                () -> (float) SlateConfig.BPS_AVERAGE_WINDOW.get(),
                v -> {
                    SlateConfig.BPS_AVERAGE_WINDOW.set(Math.round(v));
                    SlateConfig.save();
                })
                .withDecimals(0).withSuffix("s");
    }

    static SliderSetting aotvToRoofPitchRangeSetting() {
        return new SliderSetting("AOTV to Roof Pitch Range", 0, 15,
                () -> (float) SlateConfig.AOTV_ROOF_PITCH_HUMANIZATION.get(),
                v -> {
                    SlateConfig.AOTV_ROOF_PITCH_HUMANIZATION.set(Math.round(v));
                    SlateConfig.save();
                })
                .withDecimals(0).withSuffix("\u00B0");
    }

    static SliderSetting pestFovRangeSetting() {
        return new SliderSetting("Pest FOV Range", 0, 90,
                () -> SlateConfig.PEST_FOV_RANGE.get(),
                v -> {
                    SlateConfig.PEST_FOV_RANGE.set(v);
                    SlateConfig.save();
                })
                .withDecimals(1).withSuffix("\u00B0");
    }

    static SliderSetting visitorFovRangeSetting() {
        return new SliderSetting("Visitor FOV Range", 0, 30,
                () -> SlateConfig.VISITOR_FOV_RANGE.get(),
                v -> {
                    SlateConfig.VISITOR_FOV_RANGE.set(v);
                    SlateConfig.save();
                })
                .withDecimals(1).withSuffix("\u00B0");
    }

    static SliderSetting pestExchangeFovRangeSetting() {
        return new SliderSetting("Phillip FOV Range", 0, 15,
                () -> SlateConfig.PEST_EXCHANGE_FOV_RANGE.get(),
                v -> {
                    SlateConfig.PEST_EXCHANGE_FOV_RANGE.set(v);
                    SlateConfig.save();
                })
                .withDecimals(1).withSuffix("\u00B0");
    }

    static RangeSliderSetting pestAboveAimPitchRangeSetting() {
        return new RangeSliderSetting("Pest Above Aim Pitch", 10, 90,
                () -> SlateConfig.PEST_ABOVE_TARGET_PITCH_MIN.get(),
                () -> SlateConfig.PEST_ABOVE_TARGET_PITCH_MAX.get(),
                (lower, upper) -> {
                    SlateConfig.PEST_ABOVE_TARGET_PITCH_MIN.set(lower);
                    SlateConfig.PEST_ABOVE_TARGET_PITCH_MAX.set(upper);
                    SlateConfig.save();
                })
                .withDecimals(0).withSuffix("\u00B0");
    }
}
