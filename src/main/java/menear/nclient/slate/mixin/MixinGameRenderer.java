package menear.nclient.slate.mixin;

import menear.nclient.slate.bootstrap.SlateBootstrapHooks;
import menear.nclient.slate.renderer.SlateRenderQueue;
import menear.nclient.slate.ui.MainGUI;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        SlateBootstrapHooks.onGameRenderStart(Minecraft.getInstance());
    }

    /** Fires after GUI render-state extraction. */
    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        SlateBootstrapHooks.onGameRenderEnd();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void afterRender(float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        SlateRenderQueue.flush();
        if (Minecraft.getInstance().screen instanceof MainGUI mainGUI) {
            mainGUI.renderAfterGameRenderer(partialTicks);
        }
    }

    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void onRenderItemInHand(PoseStack poseStack, Camera camera, float partialTick, CallbackInfo ci) {
        if (SlateBootstrapHooks.isFreecamEnabled()) {
            ci.cancel();
        }
    }
}

