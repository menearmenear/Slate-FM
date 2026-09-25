package menear.nclient.slate.mixin;

import menear.nclient.slate.bootstrap.SlateBootstrapHooks;
import net.minecraft.client.Options;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Options.class)
public class MixinOptions {
    @Inject(method = "getSoundSourceVolume", at = @At("HEAD"), cancellable = true)
    private void onGetSoundSourceVolume(SoundSource soundSource, CallbackInfoReturnable<Float> ci) {
        if (SlateBootstrapHooks.isMuted() && soundSource == SoundSource.MASTER) {
            ci.setReturnValue(SlateBootstrapHooks.getMuteVolume());
        }
    }
}

