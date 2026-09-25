package menear.nclient.slate.mixin;

import menear.nclient.slate.modules.visuals.PipManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinWindow {
    @Inject(
            method = "runTick",
            at = @At("RETURN")
    )
    private void slate$syncPipWindowMode(boolean tick, CallbackInfo ci) {
        PipManager.render(Minecraft.getInstance());
    }
}
