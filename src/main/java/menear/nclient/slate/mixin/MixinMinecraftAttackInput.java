package menear.nclient.slate.mixin;

import menear.nclient.slate.util.ProgrammaticAttackTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public class MixinMinecraftAttackInput {
    @Redirect(
            method = "handleKeybinds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MouseHandler;isMouseGrabbed()Z"
            )
    )
    private boolean slate$useHeldAttackInput(MouseHandler mouseHandler) {
        return mouseHandler.isMouseGrabbed()
                || ProgrammaticAttackTracker.shouldTreatMouseAsGrabbed((Minecraft) (Object) this);
    }
}
