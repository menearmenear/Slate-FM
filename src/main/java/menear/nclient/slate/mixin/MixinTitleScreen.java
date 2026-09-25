package menear.nclient.slate.mixin;

import menear.nclient.slate.bootstrap.SlateBootstrapHooks;
import menear.nclient.slate.proxy.SlateProxyManager;
import menear.nclient.slate.proxy.SlateProxyScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces {@link TitleScreen} with {@link SlateTitleScreen} with zero frame delay.
 *
 * Cancelling {@code init} means vanilla buttons/panorama are never set up.
 * Calling {@code setScreen} directly (not via {@code execute()}) means
 * {@code mc.screen} is already {@link SlateTitleScreen} before the first render tick,
 * so there is no one-frame TitleScreen flash.
 */
@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends Screen {
    private static final int SLATE_BUTTON_WIDTH = 200;
    private static final int SLATE_BUTTON_HEIGHT = 20;
    private static final int SLATE_BUTTON_GAP = 4;

    protected MixinTitleScreen(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void slate$redirectInit(CallbackInfo ci) {
        var replacement = slate$getReplacement();
        if (replacement == null) {
            return;
        }
        ci.cancel();
        Minecraft.getInstance().setScreen(replacement);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void slate$redirectRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        var replacement = slate$getReplacement();
        if (replacement == null) {
            return;
        }
        ci.cancel();
        Minecraft.getInstance().setScreen(replacement);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void slate$addProxyButton(CallbackInfo ci) {
        int buttonX = this.width / 2 - SLATE_BUTTON_WIDTH / 2;
        int buttonY = slate$nextInjectedButtonY();
        this.addRenderableWidget(Button.builder(
                        Component.literal(SlateProxyManager.selectedStatus()),
                        button -> Minecraft.getInstance().setScreen(new SlateProxyScreen(this)))
                .bounds(buttonX, buttonY, SLATE_BUTTON_WIDTH, SLATE_BUTTON_HEIGHT)
                .build());
    }

    private int slate$nextInjectedButtonY() {
        int minX = this.width / 2 - SLATE_BUTTON_WIDTH / 2 - 4;
        int maxX = this.width / 2 + SLATE_BUTTON_WIDTH / 2 + 4;
        int maxBottom = this.height / 4 + 96;
        for (var listener : this.children()) {
            if (!(listener instanceof Button button)) {
                continue;
            }
            if (button.getX() > maxX || button.getX() + button.getWidth() < minX) {
                continue;
            }
            if (button.getWidth() < 98 || button.getY() < this.height / 4 - 8) {
                continue;
            }
            maxBottom = Math.max(maxBottom, button.getY() + button.getHeight());
        }
        return maxBottom + SLATE_BUTTON_GAP;
    }

    private Screen slate$getReplacement() {
        Screen replacement = SlateBootstrapHooks.maybeCreateTitleScreen();
        return replacement == this ? null : replacement;
    }
}

