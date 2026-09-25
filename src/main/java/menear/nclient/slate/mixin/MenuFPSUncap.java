package menear.nclient.slate.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MenuFPSUncap {
    @Inject(method = "getFramerateLimit", at = @At(value = "CONSTANT", args = "intValue=60", shift = At.Shift.AFTER), cancellable = true)
    private void getFramerateLimit(CallbackInfoReturnable<Integer> ci) {
        ci.setReturnValue(144);
    }
}
