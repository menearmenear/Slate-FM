package menear.nclient.slate.renderer;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

public final class FunRenderer {
    private FunRenderer() {}

    public static boolean hasVisibleEffects() {
        return HatRenderer.hasVisibleEffect();
    }

    public static void renderWorld(WorldRenderContext ctx) {
        HatRenderer.render(ctx);
    }
}
