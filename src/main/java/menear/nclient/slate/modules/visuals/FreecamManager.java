package menear.nclient.slate.modules.visuals;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.InputConstants;
import menear.nclient.slate.bootstrap.SlateKeybindRegistry;
import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.mixin.AccessorKeyMapping;
import menear.nclient.slate.modules.farming.UngrabMouse;
import menear.nclient.slate.util.ClientUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public final class FreecamManager {
    private static final int OBSERVED_PLAYER_UPDATE_INTERVAL = 2;
    private static final int OBSERVED_PLAYER_INTERPOLATION_STEPS = 3;
    private static final long MACRO_MOVEMENT_GRACE_MS = 200L;
    private static boolean registered;
    private static boolean enabled;
    private static boolean renderingAnchoredPlayer;
    private static boolean toggleKeyWasDown;
    private static boolean teleportKeyWasDown;

    private static RemotePlayer cameraEntity;
    private static Entity previousCameraEntity;
    private static CameraType previousCameraType;
    private static Vec3 observedRenderPos;
    private static Vec3 observedRenderPrevPos;
    private static Vec3 observedRenderTargetPos;
    private static int observedRenderInterpolationSteps;
    private static int observedRenderTargetTick;
    private static long macroMovementGraceUntilMs;

    private FreecamManager() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        ClientTickEvents.END_CLIENT_TICK.register(FreecamManager::tick);
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(FreecamManager::renderAnchoredPlayer);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Whether the freecam feature is switched on in the UI. Turning this on does not detach
     * the camera by itself - it only lets the freecam keybind toggle the camera on/off, in
     * the same spirit as {@link FreelookManager}. Turning it off also stops any active freecam.
     */
    public static boolean isFeatureEnabled() {
        return SlateConfig.FREECAM_ENABLED.get();
    }

    public static void setFeatureEnabled(boolean shouldEnable) {
        setFeatureEnabled(Minecraft.getInstance(), shouldEnable);
    }

    public static void setFeatureEnabled(Minecraft client, boolean shouldEnable) {
        SlateConfig.FREECAM_ENABLED.set(shouldEnable);
        SlateConfig.save();
        if (!shouldEnable) {
            setEnabled(client, false);
        }
    }

    public static boolean turnCamera(double yRot, double xRot) {
        if (!enabled || cameraEntity == null) {
            return false;
        }

        cameraEntity.turn(yRot, xRot);
        syncRotationState(cameraEntity, cameraEntity.getYRot(), cameraEntity.getXRot());
        return true;
    }

    public static boolean shouldSuppressMacroMovementDetection(LocalPlayer player) {
        return player != null && System.currentTimeMillis() < macroMovementGraceUntilMs;
    }

    public static boolean isProgrammaticKeyDown(Minecraft client, KeyMapping mapping) {
        return mapping != null && mapping.isDown() && !isKeyDown(client, mapping);
    }

    public static void toggle(Minecraft client) {
        if (!isFeatureEnabled()) {
            if (client != null) {
                ClientUtils.sendMessage(client, "Turn the Freecam module on to use the freecam keybind.", false);
            }
            return;
        }
        setEnabled(client, !enabled);
    }

    public static void teleportCameraToPlayer(Minecraft client) {
        if (!enabled || client == null || client.player == null || cameraEntity == null) {
            return;
        }

        LocalPlayer player = client.player;
        cameraEntity.setOldPosAndRot();
        cameraEntity.setPos(player.position());
        syncRotationState(cameraEntity, player.getYRot(), player.getXRot());
        cameraEntity.setDeltaMovement(Vec3.ZERO);
        ClientUtils.sendMessage(client, "Teleported to player!", false);
    }

    public static void setEnabled(boolean shouldEnable) {
        setEnabled(Minecraft.getInstance(), shouldEnable);
    }

    public static void setEnabled(Minecraft client, boolean shouldEnable) {
        if (shouldEnable && StreamerModeManager.isEnabled()) {
            return;
        }
        if (client == null) {
            enabled = false;
            previousCameraType = null;
            macroMovementGraceUntilMs = 0L;
            return;
        }
        if (!client.isSameThread()) {
            client.execute(() -> setEnabled(client, shouldEnable));
            return;
        }
        if (shouldEnable) {
            enable(client);
            return;
        }
        disable(client, false);
    }

    private static void enable(Minecraft client) {
        if (enabled || client.player == null || client.level == null) {
            return;
        }

        LocalPlayer player = client.player;
        enabled = true;
        previousCameraEntity = client.getCameraEntity();
        previousCameraType = client.options.getCameraType();
        UngrabMouse.suspendForFreecam();

        cameraEntity = createCameraEntity(client.level, player);
        observedRenderPos = player.position();
        observedRenderPrevPos = observedRenderPos;
        observedRenderTargetPos = observedRenderPos;
        observedRenderInterpolationSteps = 0;
        observedRenderTargetTick = player.tickCount;
        startMacroMovementGrace();
        client.setCameraEntity(cameraEntity);
        enforceFirstPerson(client);
        clearLatchedInputState(client, player);
        ClientUtils.sendMessage(client, "Freecam enabled!", false);
    }

    private static void disable(Minecraft client, boolean dueToMissingWorld) {
        if (!enabled && cameraEntity == null) {
            return;
        }

        enabled = false;
        if (client != null && client.player != null) {
            startMacroMovementGrace();
            clearLatchedInputState(client, client.player);
            Entity restore = previousCameraEntity;
            if (restore == null || restore.isRemoved()) {
                restore = client.player;
            }
            client.setCameraEntity(restore);
            if (!dueToMissingWorld) {
                ClientUtils.sendMessage(client, "Freecam disabled!", false);
            }
        }

        restoreCameraType(client);
        UngrabMouse.resumeAfterFreecam();
        cameraEntity = null;
        observedRenderPos = null;
        observedRenderPrevPos = null;
        observedRenderTargetPos = null;
        observedRenderInterpolationSteps = 0;
        observedRenderTargetTick = 0;
        previousCameraEntity = null;
        previousCameraType = null;
    }

    private static void startMacroMovementGrace() {
        macroMovementGraceUntilMs = System.currentTimeMillis() + MACRO_MOVEMENT_GRACE_MS;
    }

    /**
     * Rising-edge poll of the freecam keybinds straight from the physical key state, the same
     * way {@link FreelookManager} reads its bind. This avoids relying on {@code consumeClick()},
     * which never fires when the key mapping ends up in the detached fallback path.
     */
    private static void pollKeybinds(Minecraft client) {
        if (client == null || client.player == null || client.level == null || client.screen != null) {
            toggleKeyWasDown = false;
            teleportKeyWasDown = false;
            return;
        }

        boolean toggleDown = isKeyDown(client, SlateKeybindRegistry.getFreecamKey());
        if (toggleDown && !toggleKeyWasDown) {
            toggle(client);
        }
        toggleKeyWasDown = toggleDown;

        boolean teleportDown = isKeyDown(client, SlateKeybindRegistry.getFreecamTeleportToPlayerKey());
        if (teleportDown && !teleportKeyWasDown && enabled) {
            teleportCameraToPlayer(client);
        }
        teleportKeyWasDown = teleportDown;
    }

    private static void tick(Minecraft client) {
        pollKeybinds(client);
        if (!enabled) {
            return;
        }
        if (StreamerModeManager.isEnabled()) {
            disable(client, true);
            return;
        }
        if (client.player == null || client.level == null) {
            disable(client, true);
            return;
        }
        if (cameraEntity == null || client.getCameraEntity() != cameraEntity) {
            cameraEntity = createCameraEntity(client.level, client.player);
            client.setCameraEntity(cameraEntity);
        }

        enforceFirstPerson(client);
        moveCamera(client);
        updateObservedPlayer(client.player);
    }

    private static void renderAnchoredPlayer(WorldRenderContext context) {
        if (!enabled || StreamerModeManager.isEnabled() || renderingAnchoredPlayer
                || context == null || context.matrixStack() == null || context.camera() == null
                || context.world() == null) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || player.level() == null || context.world() != player.level()
                || observedRenderPos == null || observedRenderPrevPos == null) {
            return;
        }

        MultiBufferSource consumers = context.consumers();
        MultiBufferSource.BufferSource fallback = null;
        if (consumers == null) {
            fallback = client.renderBuffers().bufferSource();
            consumers = fallback;
        }

        float partialTick = context.tickDelta();
        Vec3 anchorPos = new Vec3(
                Mth.lerp(partialTick, observedRenderPrevPos.x, observedRenderPos.x),
                Mth.lerp(partialTick, observedRenderPrevPos.y, observedRenderPos.y),
                Mth.lerp(partialTick, observedRenderPrevPos.z, observedRenderPos.z));
        Vec3 cameraPos = context.camera().getPosition();
        float yaw = (float) Mth.lerp(partialTick, player.yRotO, player.getYRot());

        renderingAnchoredPlayer = true;
        try {
            client.getEntityRenderDispatcher().render(
                    player,
                    anchorPos.x - cameraPos.x,
                    anchorPos.y - cameraPos.y,
                    anchorPos.z - cameraPos.z,
                    yaw,
                    partialTick,
                    context.matrixStack(),
                    consumers,
                    client.getEntityRenderDispatcher().getPackedLightCoords(player, partialTick));
            if (fallback != null) {
                fallback.endBatch();
            }
        } finally {
            renderingAnchoredPlayer = false;
        }
    }

    private static RemotePlayer createCameraEntity(ClientLevel level, LocalPlayer player) {
        GameProfile profile = new GameProfile(player.getUUID(), player.getName().getString());
        RemotePlayer camera = new RemotePlayer(level, profile);
        camera.setInvisible(true);
        camera.noPhysics = true;
        camera.setNoGravity(true);
        camera.setOldPosAndRot();
        camera.setPos(player.position());
        syncRotationState(camera, player.getYRot(), player.getXRot());
        camera.setDeltaMovement(Vec3.ZERO);
        return camera;
    }

    private static void clearLatchedInputState(Minecraft client, LocalPlayer player) {
        if (client.options != null) {
            ClientUtils.setKeyMappingState(client.options.keyUp, false);
            ClientUtils.setKeyMappingState(client.options.keyDown, false);
            ClientUtils.setKeyMappingState(client.options.keyLeft, false);
            ClientUtils.setKeyMappingState(client.options.keyRight, false);
            ClientUtils.setKeyMappingState(client.options.keyJump, false);
            ClientUtils.setKeyMappingState(client.options.keyShift, false);
            ClientUtils.setKeyMappingState(client.options.keySprint, false);
            ClientUtils.setKeyMappingState(client.options.keyAttack, false);
            ClientUtils.setKeyMappingState(client.options.keyUse, false);
        }
        // Do NOT force horizontal velocity to zero. The real player keeps ticking while
        // the camera is detached, so once the movement keys are released above, friction
        // decelerates it exactly like the server does. Snapping velocity to zero on the
        // client (even when grounded) stops it dead in a single tick while the server keeps
        // decelerating over several, and that divergence is what trips the "simulation"
        // check when entering / leaving freecam. Let friction do the work on both sides.
        player.setJumping(false);
        player.setShiftKeyDown(false);
        player.setSprinting(false);
        player.input.up = false;
        player.input.down = false;
        player.input.left = false;
        player.input.right = false;
        player.input.forwardImpulse = 0.0f;
        player.input.leftImpulse = 0.0f;
        player.input.jumping = false;
        player.input.shiftKeyDown = false;
    }

    private static void updateObservedPlayer(LocalPlayer player) {
        Vec3 currentPos = player.position();
        if (observedRenderPos == null || observedRenderPrevPos == null || observedRenderTargetPos == null) {
            observedRenderPos = currentPos;
            observedRenderPrevPos = currentPos;
            observedRenderTargetPos = currentPos;
            observedRenderInterpolationSteps = 0;
            observedRenderTargetTick = player.tickCount;
            return;
        }

        observedRenderPrevPos = observedRenderPos;
        if (observedRenderTargetPos.distanceToSqr(currentPos) > 1.0E-6
            && player.tickCount - observedRenderTargetTick >= OBSERVED_PLAYER_UPDATE_INTERVAL) {
            observedRenderTargetPos = currentPos;
            observedRenderInterpolationSteps = OBSERVED_PLAYER_INTERPOLATION_STEPS;
            observedRenderTargetTick = player.tickCount;
        }

        if (observedRenderInterpolationSteps > 0) {
            double progress = 1.0 / observedRenderInterpolationSteps;
            observedRenderPos = new Vec3(
                Mth.lerp(progress, observedRenderPos.x, observedRenderTargetPos.x),
                Mth.lerp(progress, observedRenderPos.y, observedRenderTargetPos.y),
                Mth.lerp(progress, observedRenderPos.z, observedRenderTargetPos.z)
            );
            observedRenderInterpolationSteps--;
            return;
        }

        observedRenderPos = observedRenderTargetPos;
    }

    private static void moveCamera(Minecraft client) {
        if (cameraEntity == null) {
            return;
        }

        float speed = SlateConfig.FREECAM_SPEED.get();
        if (isKeyDown(client, client.options.keySprint)) {
            speed *= 2.0f;
        }

        Vec3 look = cameraEntity.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0.0, look.z);
        if (forward.lengthSqr() < 1.0E-6) {
            double yawRad = Math.toRadians(cameraEntity.getYRot());
            forward = new Vec3(-Math.sin(yawRad), 0.0, Math.cos(yawRad));
        }
        forward = forward.normalize();
        Vec3 right = new Vec3(-forward.z, 0.0, forward.x);

        Vec3 motion = Vec3.ZERO;
        if (isKeyDown(client, client.options.keyUp)) {
            motion = motion.add(forward);
        }
        if (isKeyDown(client, client.options.keyDown)) {
            motion = motion.subtract(forward);
        }
        if (isKeyDown(client, client.options.keyLeft)) {
            motion = motion.subtract(right);
        }
        if (isKeyDown(client, client.options.keyRight)) {
            motion = motion.add(right);
        }
        if (isKeyDown(client, client.options.keyJump)) {
            motion = motion.add(0.0, 1.0, 0.0);
        }
        if (isKeyDown(client, client.options.keyShift)) {
            motion = motion.add(0.0, -1.0, 0.0);
        }

        cameraEntity.setOldPosAndRot();
        if (motion.lengthSqr() > 1.0E-6) {
            cameraEntity.setPos(cameraEntity.position().add(motion.normalize().scale(speed)));
        }
        syncRotationState(cameraEntity, cameraEntity.getYRot(), cameraEntity.getXRot());
        cameraEntity.setDeltaMovement(Vec3.ZERO);
    }

    private static void syncRotationState(LivingEntity entity, float yaw, float pitch) {
        entity.setYRot(yaw);
        entity.setXRot(pitch);
        entity.yRotO = yaw;
        entity.xRotO = pitch;
        entity.setYHeadRot(yaw);
        entity.yHeadRotO = yaw;
        entity.setYBodyRot(yaw);
        entity.yBodyRotO = yaw;
    }

    private static boolean isKeyDown(Minecraft client, KeyMapping mapping) {
        InputConstants.Key key = ((AccessorKeyMapping) mapping).getKey();
        if (key == null || key == InputConstants.UNKNOWN) {
            return false;
        }
        return switch (key.getType()) {
            case KEYSYM, SCANCODE -> InputConstants.isKeyDown(client.getWindow().getWindow(), key.getValue());
            case MOUSE -> GLFW.glfwGetMouseButton(client.getWindow().getWindow(), key.getValue()) == GLFW.GLFW_PRESS;
        };
    }

    private static void enforceFirstPerson(Minecraft client) {
        if (client.options.getCameraType() != CameraType.FIRST_PERSON) {
            client.options.setCameraType(CameraType.FIRST_PERSON);
        }
    }

    private static void restoreCameraType(Minecraft client) {
        if (client == null || previousCameraType == null) {
            return;
        }
        client.options.setCameraType(previousCameraType);
    }
}
