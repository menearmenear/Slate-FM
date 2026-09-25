package menear.nclient.slate.mixin;

import menear.nclient.slate.bootstrap.SlateBootstrapHooks;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class ChatHudMixin {


    @Inject(
            method = "addMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void filterMessage(Component message, CallbackInfo ci) {
        if (SlateBootstrapHooks.shouldHideFilteredChatMessage(message)) {
            ci.cancel();
        }
    }
}
