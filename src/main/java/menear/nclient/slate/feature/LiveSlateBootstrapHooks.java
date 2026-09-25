package menear.nclient.slate.feature;

import com.mojang.authlib.GameProfile;
import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.hud.HudEditScreen;
import menear.nclient.slate.hud.HudRegistry;
import menear.nclient.slate.bootstrap.SlateBootstrapHooks;
import menear.nclient.slate.macro.MacroStateManager;
import menear.nclient.slate.macro.ReconnectScheduler;
import menear.nclient.slate.modules.failsafe.FailsafeManager;
import menear.nclient.slate.modules.farming.UngrabMouse;
import menear.nclient.slate.modules.pathfinding.rotation.RotationExecutor;
import menear.nclient.slate.modules.performance.MuteManager;
import menear.nclient.slate.modules.performance.PerformanceModeManager;
import menear.nclient.slate.modules.pest.helpers.PestDestroyer;
import menear.nclient.slate.modules.pest.helpers.VacuumParticleDebug;
import menear.nclient.slate.modules.rotation.RotationManager;
import menear.nclient.slate.modules.visuals.FreecamManager;
import menear.nclient.slate.modules.visuals.FreelookManager;
import menear.nclient.slate.modules.visuals.StreamerModeManager;
import menear.nclient.slate.renderer.SlateBackground;
import menear.nclient.slate.renderer.SlateBackgroundScreens;
import menear.nclient.slate.renderer.NVGRenderer;
import menear.nclient.slate.ui.SlateConfirmScreen;
import menear.nclient.slate.ui.SlateDirectJoinScreen;
import menear.nclient.slate.ui.SlateManageServerScreen;
import menear.nclient.slate.ui.SlateMultiplayerScreen;
import menear.nclient.slate.ui.SlateTitleScreen;
import menear.nclient.slate.ui.MainGUI;
import menear.nclient.slate.util.BpsTracker;
import menear.nclient.slate.util.DelayedBlockBreakTracker;
import menear.nclient.slate.util.NickHiderUtils;
import menear.nclient.slate.util.ProgrammaticMovementTracker;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;

import java.io.File;

public final class LiveSlateBootstrapHooks implements SlateBootstrapHooks.FeatureHooks {
    @Override
    public void onConfigProfileLoaded(File profileFile) {
        ClientFeatureBootstrap.onConfigProfileLoaded(profileFile);
    }

    @Override
    public void onUnexpectedDisconnect() {
        if (MacroStateManager.isMacroRunning() && !MacroStateManager.isIntentionalDisconnect()) {
            long delay = 30 + (long) (Math.random() * 30);
            ReconnectScheduler.scheduleReconnect(delay, true);
        }
    }

    @Override
    public Screen maybeCreateConfirmScreen(BooleanConsumer callback, Component title, Component message) {
        if (StreamerModeManager.isEnabled() || !SlateConfig.CUSTOM_UI_ENABLED.get()) {
            return null;
        }
        return new SlateConfirmScreen(callback, title, message);
    }

    @Override
    public Screen maybeCreateDirectJoinScreen(Screen lastScreen, BooleanConsumer callback, ServerData serverData) {
        if (StreamerModeManager.isEnabled() || !SlateConfig.CUSTOM_UI_ENABLED.get()) {
            return null;
        }
        return new SlateDirectJoinScreen(lastScreen, callback, serverData);
    }

    @Override
    public Screen maybeCreateMultiplayerScreen(Screen lastScreen) {
        if (StreamerModeManager.isEnabled() || !SlateConfig.CUSTOM_UI_ENABLED.get()) {
            return null;
        }
        return new SlateMultiplayerScreen(lastScreen);
    }

    @Override
    public Screen maybeCreateManageServerScreen(Screen lastScreen, Component title, BooleanConsumer callback, ServerData serverData) {
        if (StreamerModeManager.isEnabled() || !SlateConfig.CUSTOM_UI_ENABLED.get()) {
            return null;
        }
        return new SlateManageServerScreen(lastScreen, title, callback, serverData);
    }

    @Override
    public Screen maybeCreateTitleScreen() {
        if (StreamerModeManager.isEnabled() || !SlateConfig.CUSTOM_UI_ENABLED.get()) {
            return null;
        }
        return new SlateTitleScreen();
    }

    @Override
    public Screen maybeCreateHudEditScreen() {
        return new HudEditScreen();
    }

    @Override
    public void onGameRenderStart(Minecraft minecraft) {
        if (minecraft.player == null) {
            return;
        }
        RotationManager.update(minecraft);
        RotationExecutor.update(minecraft);
    }

    @Override
    public void onGameRenderEnd() {
        HudRegistry.onGuiGraphicsClosed();
    }

    @Override
    public boolean shouldSuppressVanillaHud(Screen screen) {
        return SlateBootstrapHooks.isBootstrapConfigScreen(screen) || screen instanceof MainGUI || screen instanceof HudEditScreen;
    }

    @Override
    public void renderConfigScreenOverlay(NVGRenderer renderer, float width, float height, float deltaTime) {
        HudRegistry.renderConfigTransition(renderer);
    }

    @Override
    public Component transformOverlayMessage(Component component) {
        FailsafeManager.observeGhostBlockOverlayMessage(component);
        menear.nclient.slate.modules.profit.helpers.FarmingXpTracker.onActionBar(component);
        return transformDisplayComponent(component);
    }

    @Override
    public Component transformDisplayComponent(Component component) {
        if (component == null) {
            return null;
        }
        if (!SlateConfig.NICK_HIDER_ENABLED.get() && !SlateConfig.COOP_HIDER_ENABLED.get() && !SlateConfig.HIDE_SERVER_ID.get()) {
            return component;
        }
        Component transformed = NickHiderUtils.transformComponent(component);
        return transformed != null ? transformed : component;
    }

    @Override
    public String transformDisplayString(String text) {
        if (!SlateConfig.NICK_HIDER_ENABLED.get() && !SlateConfig.COOP_HIDER_ENABLED.get() && !SlateConfig.HIDE_SERVER_ID.get()) {
            return text;
        }
        return NickHiderUtils.transformString(text);
    }

    @Override
    public boolean shouldHidePlayerSkin(GameProfile profile) {
        return profile != null
                && SlateConfig.NICK_HIDER_ENABLED.get()
                && SlateConfig.HIDE_SKIN.get()
                && profile.getName().equals(Minecraft.getInstance().getUser().getName());
    }

    @Override
    public boolean shouldHideFilteredChatMessage(Component message) {
        if (!SlateConfig.HIDE_FILTERED_CHAT.get() || message == null) {
            return false;
        }
        return message.getString().contains("for killing a");
    }

    @Override
    public boolean isFreecamEnabled() {
        return FreecamManager.isEnabled();
    }

    @Override
    public boolean isFreecamProgrammaticKeyDown(Minecraft client, KeyMapping keyMapping) {
        return FreecamManager.isProgrammaticKeyDown(client, keyMapping);
    }

    @Override
    public boolean isProgrammaticMovementKeyDown(KeyMapping keyMapping) {
        return ProgrammaticMovementTracker.isDown(keyMapping);
    }

    @Override
    public boolean turnFreecamCamera(double yRot, double xRot) {
        return FreecamManager.turnCamera(yRot, xRot);
    }

    @Override
    public boolean isFreelookActive() {
        return FreelookManager.isActive();
    }

    @Override
    public boolean turnFreelookCamera(double yRot, double xRot) {
        return FreelookManager.turn(yRot, xRot);
    }

    @Override
    public float getFreelookYaw() {
        return FreelookManager.getYaw();
    }

    @Override
    public float getFreelookPitch() {
        return FreelookManager.getPitch();
    }

    @Override
    public boolean shouldCancelMouseTurn() {
        return RotationManager.isRotating() && !FreecamManager.isEnabled() && !FreelookManager.isActive();
    }

    @Override
    public boolean isMouseUngrabbed() {
        return UngrabMouse.isMouseUngrabbed();
    }

    @Override
    public boolean hasCustomScreenBackground(Screen screen) {
        return !StreamerModeManager.isEnabled()
                && SlateConfig.CUSTOM_UI_ENABLED.get()
                && screen != null
                && SlateBackgroundScreens.matches(screen);
    }

    @Override
    public void renderCustomScreenBackground(int width, int height, int mouseX, int mouseY) {
        SlateBackground.INSTANCE.render(width, height, mouseX, mouseY);
    }

    @Override
    public void onBackgroundLeftClick(Minecraft minecraft, Screen screen, double mouseX, double mouseY) {
        if (hasCustomScreenBackground(screen)) {
            SlateBackground.INSTANCE.addRipple((float) mouseX, (float) mouseY);
        }
    }

    @Override
    public void onBlockBreak() {
        BpsTracker.onBlockBreak();
        FailsafeManager.onBlockBreak();
    }

    @Override
    public void onBlockBreak(net.minecraft.core.BlockPos pos) {
        DelayedBlockBreakTracker.onImmediateBlockBreak(pos);
        BpsTracker.onBlockBreak();
        FailsafeManager.onBlockBreak(pos);
    }

    @Override
    public void onBlockBreakClick(Minecraft minecraft, net.minecraft.core.BlockPos pos) {
        DelayedBlockBreakTracker.onBlockBreakClick(minecraft, pos);
    }

    @Override
    public void onBlockChanged(Minecraft minecraft, net.minecraft.core.BlockPos pos,
                               net.minecraft.world.level.block.state.BlockState oldState,
                               net.minecraft.world.level.block.state.BlockState newState) {
        DelayedBlockBreakTracker.onBlockChanged(minecraft, pos, newState);
        FailsafeManager.onBlockChanged(minecraft, pos, oldState, newState);
    }

    @Override
    public void tickFailsafes(Minecraft minecraft) {
        DelayedBlockBreakTracker.tick(minecraft);
        FailsafeManager.tick(minecraft);
    }

    @Override
    public void resetFailsafes() {
        DelayedBlockBreakTracker.reset();
        FailsafeManager.reset();
    }

    @Override
    public void resetFailsafeRuntimeState() {
        DelayedBlockBreakTracker.reset();
        FailsafeManager.resetRuntimeState();
    }

    @Override
    public void addRotationGracePeriod(long durationMs) {
        FailsafeManager.addRotationGracePeriod(durationMs);
    }

    @Override
    public void selectHotbarSlot(Minecraft minecraft, int slot) {
        FailsafeManager.selectHotbarSlot(minecraft, slot);
    }

    @Override
    public boolean isMuted() {
        return MuteManager.isMuted();
    }

    @Override
    public float getMuteVolume() {
        return MuteManager.getVolume();
    }

    @Override
    public boolean areParticlesDisabled() {
        return PerformanceModeManager.isParticlesDisabled();
    }

    @Override
    public void onParticlePacket(Minecraft minecraft, ClientboundLevelParticlesPacket packet) {
        VacuumParticleDebug.onParticlePacket(minecraft, packet);
        if (packet.getParticle().getType() == ParticleTypes.ANGRY_VILLAGER) {
            PestDestroyer.onFireworkParticle(packet.getX(), packet.getY(), packet.getZ());
        }
    }
}

