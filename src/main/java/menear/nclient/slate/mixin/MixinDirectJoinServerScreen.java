package menear.nclient.slate.mixin;

import menear.nclient.slate.bootstrap.SlateBootstrapHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.DirectJoinServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

@Mixin(DirectJoinServerScreen.class)
public class MixinDirectJoinServerScreen {

    @Shadow private EditBox ipEdit;

    @Unique private Screen          slate$lastScreen;
    @Unique private BooleanConsumer slate$callback;
    @Unique private ServerData      slate$serverData;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void slate$captureArgs(Screen lastScreen, BooleanConsumer callback,
                                    ServerData serverData, CallbackInfo ci) {
        slate$lastScreen = lastScreen;
        slate$callback   = callback;
        slate$serverData = serverData;
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void slate$redirectInit(CallbackInfo ci) {
        var replacement = SlateBootstrapHooks.maybeCreateDirectJoinScreen(slate$lastScreen, slate$callback, slate$serverData);
        if (replacement == null) {
            return;
        }
        ci.cancel();
        Minecraft.getInstance().setScreen(replacement);
    }

    @Inject(method = "removed", at = @At("HEAD"), cancellable = true)
    private void slate$guardRemoved(CallbackInfo ci) {
        if (ipEdit == null) ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void slate$guardTick(CallbackInfo ci) {
        if (ipEdit == null) ci.cancel();
    }

    @Inject(method = "resize", at = @At("HEAD"), cancellable = true)
    private void slate$guardResize(Minecraft minecraft, int width, int height, CallbackInfo ci) {
        if (ipEdit == null) ci.cancel();
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void slate$guardRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (ipEdit == null) ci.cancel();
    }
}

