package menear.nclient.slate.mixin;

import menear.nclient.slate.bootstrap.SlateBootstrapHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouseHandler {
    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void onTurnPlayer(CallbackInfo ci) {
        if (SlateBootstrapHooks.shouldCancelMouseTurn()) {
            ci.cancel();
        }
    }

    @Redirect(
        method = "turnPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;turn(DD)V"
        )
    )
    private void redirectTurnPlayer(Entity player, double yRot, double xRot) {
        if (SlateBootstrapHooks.turnFreecamCamera(yRot, xRot)) {
            return;
        }
        if (SlateBootstrapHooks.turnFreelookCamera(yRot, xRot)) {
            return;
        }
        player.turn(yRot, xRot);
    }

    /** Block vanilla from re-grabbing the cursor while the macro has released it. */
    @Inject(method = "grabMouse", at = @At("HEAD"), cancellable = true)
    private void onGrabMouse(CallbackInfo ci) {
        if (SlateBootstrapHooks.isMouseUngrabbed()) {
            ci.cancel();
        }
    }

    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    private void onMouseClick(long l, int button, int i, int k, CallbackInfo ci) {
        if (SlateBootstrapHooks.isMouseUngrabbed()) {
            ci.cancel();
            return;
        }
        if (button == 0 && i == 1) {
            Minecraft mc = Minecraft.getInstance();
            Screen screen = mc.screen;
            if (screen != null && SlateBootstrapHooks.hasCustomScreenBackground(screen)) {
                double mouseX = mc.mouseHandler.xpos() * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
                double mouseY = mc.mouseHandler.ypos() * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
                SlateBootstrapHooks.onBackgroundLeftClick(mc, screen, mouseX, mouseY);
            }
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long l, double d, double e, CallbackInfo ci) {
        if (SlateBootstrapHooks.isMouseUngrabbed()) {
            ci.cancel();
        }
    }
}
