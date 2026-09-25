package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;

public final class ComposterSettingsBridge {
    private static final String SOURCE_SACKS = "SACKS";
    private static final String SOURCE_BAZAAR = "BAZAAR";

    private ComposterSettingsBridge() {
    }

    public static void setSourceModeIndex(int index) {
        SlateConfig.AUTO_COMPOSTER_SOURCE_MODE.set(index == 1 ? SOURCE_BAZAAR : SOURCE_SACKS);
        SlateConfig.save();
    }
}
