package menear.nclient.slate.mixin;

import menear.nclient.slate.bootstrap.SlateBootstrapHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class MixinKeyboardInput extends Input {
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(boolean slowDown, float f, CallbackInfo ci) {
        if (!SlateBootstrapHooks.isFreecamEnabled()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.options == null) {
            return;
        }

        boolean forward = SlateBootstrapHooks.isProgrammaticMovementKeyDown(client.options.keyUp)
            || SlateBootstrapHooks.isFreecamProgrammaticKeyDown(client, client.options.keyUp);
        boolean backward = SlateBootstrapHooks.isProgrammaticMovementKeyDown(client.options.keyDown)
            || SlateBootstrapHooks.isFreecamProgrammaticKeyDown(client, client.options.keyDown);
        boolean left = SlateBootstrapHooks.isProgrammaticMovementKeyDown(client.options.keyLeft)
            || SlateBootstrapHooks.isFreecamProgrammaticKeyDown(client, client.options.keyLeft);
        boolean right = SlateBootstrapHooks.isProgrammaticMovementKeyDown(client.options.keyRight)
            || SlateBootstrapHooks.isFreecamProgrammaticKeyDown(client, client.options.keyRight);
        boolean jump = SlateBootstrapHooks.isProgrammaticMovementKeyDown(client.options.keyJump)
            || SlateBootstrapHooks.isFreecamProgrammaticKeyDown(client, client.options.keyJump);
        boolean shift = SlateBootstrapHooks.isProgrammaticMovementKeyDown(client.options.keyShift)
            || SlateBootstrapHooks.isFreecamProgrammaticKeyDown(client, client.options.keyShift);

        this.up = forward;
        this.down = backward;
        this.left = left;
        this.right = right;
        this.forwardImpulse = calculateImpulse(forward, backward);
        this.leftImpulse = calculateImpulse(left, right);
        this.jumping = jump;
        this.shiftKeyDown = shift;
    }

    private static float calculateImpulse(boolean positive, boolean negative) {
        if (positive == negative) {
            return 0.0f;
        }
        return positive ? 1.0f : -1.0f;
    }
}
