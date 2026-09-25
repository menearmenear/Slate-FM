package menear.nclient.slate.mixin;

import menear.nclient.slate.config.SlateConfig;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MixinKeepFocus {
    @Inject(method = "isWindowActive", at = @At("HEAD"), cancellable = true)
    private void keepFocusForWindowActive(CallbackInfoReturnable<Boolean> cir) {
        if (SlateConfig.KEEP_FOCUS.get()) {
            cir.setReturnValue(true);
        }
    }
}