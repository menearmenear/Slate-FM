package menear.nclient.slate.mixin;

import com.mojang.authlib.GameProfile;
import menear.nclient.slate.bootstrap.SlateBootstrapHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
public abstract class MixinPlayerInfo {

    @Shadow public abstract GameProfile getProfile();

    @Inject(method = "getSkinLocation", at = @At("HEAD"), cancellable = true)
    private void slate$getSkinLocation(CallbackInfoReturnable<ResourceLocation> cir) {
        if (SlateBootstrapHooks.shouldHidePlayerSkin(getProfile())) {
            if (getProfile().getName().equals(Minecraft.getInstance().getUser().getName())) {
                cir.setReturnValue(DefaultPlayerSkin.getDefaultSkin(getProfile().getId()));
            }
        }
    }

    @Inject(method = "getTabListDisplayName", at = @At("RETURN"), cancellable = true)
    private void slate$onGetTabListDisplayName(CallbackInfoReturnable<Component> cir) {
        Component original = cir.getReturnValue();
        if (original != null) {
            Component transformed = SlateBootstrapHooks.transformDisplayComponent(original);
            if (transformed != null && transformed != original) {
                cir.setReturnValue(transformed);
            }
        }
    }
}
