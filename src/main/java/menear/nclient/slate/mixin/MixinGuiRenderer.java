package menear.nclient.slate.mixin;

import menear.nclient.slate.renderer.SlateRenderQueue;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGuiRenderer {
    @Inject(method = "render", at = @At("TAIL"))
    private void slate$flushQueuedNvg(float partialTick, long finishTime, boolean renderWorld, CallbackInfo ci) {
        SlateRenderQueue.flush();
    }
}
