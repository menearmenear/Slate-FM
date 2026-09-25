package menear.nclient.slate.config;

import menear.nclient.slate.bootstrap.SlateBootstrapHooks;
import menear.nclient.slate.util.ClientUtils;
import net.minecraft.client.Minecraft;

import java.util.concurrent.ThreadLocalRandom;

public final class ConfigHelpers {
    private ConfigHelpers() {}

    public static int getRandomizedDelay(int baseDelay) {
        return Math.max(0, baseDelay);
    }

    public static int getRandomizedDelay(int minDelay, int maxDelay) {
        int lower = Math.max(0, Math.min(minDelay, maxDelay));
        int upper = Math.max(0, Math.max(minDelay, maxDelay));
        if (lower == upper) {
            return lower;
        }
        return ThreadLocalRandom.current().nextInt(lower, upper + 1);
    }

    public static void executePlotTpRewarp(Minecraft client) {
        if (SlateConfig.ENABLE_PLOT_TP_REWARP.get()) {
            executePlotTpRewarp(client, SlateConfig.PLOT_TP_NUMBER.get());
        }
    }

    public static void executePlotTpRewarp(Minecraft client, String plotNumber) {
        executeRewarpCommand(client, RewarpMode.PLOT_TP, plotNumber);
    }

    public static void executeRewarpCommand(Minecraft client, RewarpMode mode, String plotNumber) {
        RewarpMode resolvedMode = mode == null ? RewarpMode.FLY : mode;
        if (!resolvedMode.usesCommand()) {
            return;
        }

        String sanitizedPlotNumber = plotNumber == null || plotNumber.isBlank() ? "0" : plotNumber.trim();
        SlateBootstrapHooks.addRotationGracePeriod(SlateConfig.FAILSAFE_ROTATION_WARP_GRACE_MS.get());
        if (resolvedMode == RewarpMode.WARP_GARDEN) {
            ClientUtils.sendCommand(client, "/warp garden");
        } else {
            ClientUtils.sendCommand(client, "/plottp " + sanitizedPlotNumber);
        }
    }

    public static UnflyMode getUnflyMode() {
        try { return UnflyMode.valueOf(SlateConfig.UNFLY_MODE.get()); }
        catch (IllegalArgumentException e) { return UnflyMode.DOUBLE_TAP_SPACE; }
    }

    public static FreelookMode getFreelookMode() {
        try { return FreelookMode.valueOf(SlateConfig.FREELOOK_MODE.get()); }
        catch (IllegalArgumentException e) { return FreelookMode.HOLD; }
    }
}

