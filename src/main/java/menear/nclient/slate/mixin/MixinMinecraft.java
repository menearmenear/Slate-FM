package menear.nclient.slate.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MixinMinecraft {
    @Invoker("startAttack")
    boolean slate$startAttack();

    @Invoker("continueAttack")
    void slate$continueAttack(boolean attacking);

    @Invoker("startUseItem")
    void slate$startUseItem();

    @Invoker("pickBlockOrEntity")
    void slate$pickBlockOrEntity();

    /**
     * {@code Minecraft.missTime} - the block-break suppression counter. Vanilla
     * {@code MouseHandler#grabMouse()} slams this to 10000 so the click that re-grabs the
     * cursor doesn't attack, and it only decrements by 1/tick. Exposed so freelook can clear
     * it after grabbing the mouse: otherwise the macro's continuous (leftClick=true) attack
     * never resets it and block-breaking stays suppressed for ~500s.
     */
    @Accessor("missTime")
    void slate$setMissTime(int missTime);
}
