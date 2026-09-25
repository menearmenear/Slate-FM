package menear.nclient.slate.modules.visuals;

import menear.nclient.slate.config.SlateConfig;

public final class StreamerModeManager {
    private StreamerModeManager() {
    }

    public static boolean isEnabled() {
        return SlateConfig.STREAMER_MODE.get();
    }

    public static void setEnabled(boolean enabled) {
        SlateConfig.STREAMER_MODE.set(enabled);
        if (enabled) {
            FreecamManager.setEnabled(false);
            PipManager.setEnabled(false);
            UngrabMouseManager.setEnabled(false);
        }
        SlateConfig.save();
    }
}
