package menear.nclient.slate.mixin;

import menear.nclient.slate.bootstrap.SlateBootstrapHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

@Mixin(ConfirmScreen.class)
public class MixinConfirmScreen {

    @Unique private BooleanConsumer slate$callback;
    @Unique private Component         slate$title;
    @Unique private Component         slate$message;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void slate$captureArgs(BooleanConsumer callback, Component title,
                                    Component message, CallbackInfo ci) {
        slate$callback = callback;
        slate$title    = title;
        slate$message  = message;
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void slate$redirectInit(CallbackInfo ci) {
        var replacement = SlateBootstrapHooks.maybeCreateConfirmScreen(slate$callback, slate$title, slate$message);
        if (replacement == null) {
            return;
        }
        ci.cancel();
        Minecraft.getInstance().setScreen(replacement);
    }

}

