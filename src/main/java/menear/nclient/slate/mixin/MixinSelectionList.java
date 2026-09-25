package menear.nclient.slate.mixin;

import menear.nclient.slate.bootstrap.SlateBootstrapHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSelectionList.class)
public class MixinSelectionList {

    @Shadow private boolean renderBackground;
    @Shadow private boolean renderTopAndBottom;

    @Unique private boolean slate$savedRenderBackground;
    @Unique private boolean slate$savedRenderTopAndBottom;
    @Unique private boolean slate$restoreBackground;

    @Inject(method = "render", at = @At("HEAD"))
    private void slate$disableListBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen != null && SlateBootstrapHooks.hasCustomScreenBackground(screen)) {
            slate$savedRenderBackground = renderBackground;
            slate$savedRenderTopAndBottom = renderTopAndBottom;
            renderBackground = false;
            renderTopAndBottom = false;
            slate$restoreBackground = true;
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void slate$restoreListBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (slate$restoreBackground) {
            renderBackground = slate$savedRenderBackground;
            renderTopAndBottom = slate$savedRenderTopAndBottom;
            slate$restoreBackground = false;
        }
    }

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void onRenderListBackground(GuiGraphics graphics, CallbackInfo ci) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen != null && SlateBootstrapHooks.hasCustomScreenBackground(screen)) {
            ci.cancel();
        }
    }
}

