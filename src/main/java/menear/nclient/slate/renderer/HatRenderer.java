package menear.nclient.slate.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.modules.visuals.FreecamManager;
import menear.nclient.slate.modules.visuals.StreamerModeManager;
import menear.nclient.slate.ui.theme.Theme;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;

public final class HatRenderer {
    private static final int LINE_ALPHA = 230;
    private static final int FILL_ALPHA = 90;

    private HatRenderer() {}

    public static boolean hasVisibleEffect() {
        if (StreamerModeManager.isEnabled()) {
            return false;
        }
        return SlateConfig.HAT_ENABLED.get() && shouldRender(Minecraft.getInstance());
    }

    public static void render(WorldRenderContext ctx) {
        if (StreamerModeManager.isEnabled()) return;
        if (!SlateConfig.HAT_ENABLED.get()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !shouldRender(mc)) return;
        if (ctx == null || ctx.matrixStack() == null || ctx.camera() == null || ctx.world() == null) return;

        MultiBufferSource consumers = ctx.consumers();
        if (consumers == null) {
            consumers = mc.renderBuffers().bufferSource();
        }
        MultiBufferSource.BufferSource bufferSource = consumers instanceof MultiBufferSource.BufferSource source
                ? source
                : null;
        int vertices = Math.max(3, Math.min(20, SlateConfig.HAT_VERTICES.get()));
        double radius = SlateConfig.HAT_RADIUS.get();
        double height = SlateConfig.HAT_HEIGHT.get();
        double yOffset = SlateConfig.HAT_Y_OFFSET.get();
        float partialTick = ctx.tickDelta();

        Vec3 playerPos = mc.player.getPosition(partialTick);
        double baseY = playerPos.y + mc.player.getBbHeight() + yOffset;
        Vec3 center = new Vec3(playerPos.x, baseY, playerPos.z);
        Vec3 apex = center.add(0.0, height, 0.0);
        Vec3 cameraPos = ctx.camera().getPosition();
        float time = (System.currentTimeMillis() % 10_000L) / 10_000.0f;

        Vec3 firstPoint = null;
        Vec3 previousPoint = null;
        for (int i = 0; i < vertices; i++) {
            double angle = (Math.PI * 2.0 * i) / vertices;
            Vec3 point = new Vec3(
                    center.x + Math.cos(angle) * radius,
                    center.y,
                    center.z + Math.sin(angle) * radius);

            if (previousPoint != null) {
                drawFace(ctx, consumers, cameraPos, apex, previousPoint, point, time, i / (float) vertices);
                drawLine(ctx, consumers, cameraPos, previousPoint, point, time, i / (float) vertices);
            } else {
                firstPoint = point;
            }

            drawLine(ctx, consumers, cameraPos, apex, point, time, (i + 0.5f) / vertices);
            previousPoint = point;
        }

        if (previousPoint != null && firstPoint != null) {
            drawFace(ctx, consumers, cameraPos, apex, previousPoint, firstPoint, time, 1.0f);
            drawLine(ctx, consumers, cameraPos, previousPoint, firstPoint, time, 1.0f);
        }

        if (bufferSource != null) {
            bufferSource.endBatch();
        }
    }

    private static void drawLine(WorldRenderContext ctx, MultiBufferSource consumers, Vec3 cameraPos, Vec3 from,
                                 Vec3 to, float time, float offset) {
        if (consumers == null || ctx.matrixStack() == null) return;

        int color = Theme.getRainbowColor(time, offset, LINE_ALPHA);
        Vec3 fromRelative = from.subtract(cameraPos);
        Vec3 toRelative = to.subtract(cameraPos);
        Vec3 direction = to.subtract(from);
        double length = direction.length();
        float normalX = length > 1.0E-6 ? (float) (direction.x / length) : 0.0f;
        float normalY = length > 1.0E-6 ? (float) (direction.y / length) : 1.0f;
        float normalZ = length > 1.0E-6 ? (float) (direction.z / length) : 0.0f;

        VertexConsumer buffer = consumers.getBuffer(RenderType.lines());
        addColoredLineVertex(buffer, ctx.matrixStack().last(), fromRelative, color, normalX, normalY, normalZ);
        addColoredLineVertex(buffer, ctx.matrixStack().last(), toRelative, color, normalX, normalY, normalZ);
    }

    private static void drawFace(WorldRenderContext ctx, MultiBufferSource consumers, Vec3 cameraPos, Vec3 apex,
                                 Vec3 left, Vec3 right, float time, float offset) {
        if (!SlateConfig.HAT_FILLED.get() || consumers == null || ctx.matrixStack() == null) return;

        int color = Theme.getRainbowColor(time, offset, FILL_ALPHA);
        Vec3 normal = left.subtract(apex).cross(right.subtract(apex));
        double length = normal.length();
        float normalX = length > 1.0E-6 ? (float) (normal.x / length) : 0.0f;
        float normalY = length > 1.0E-6 ? (float) (normal.y / length) : 1.0f;
        float normalZ = length > 1.0E-6 ? (float) (normal.z / length) : 0.0f;
        VertexConsumer buffer = consumers.getBuffer(RenderType.debugQuads());
        addColoredLineVertex(buffer, ctx.matrixStack().last(), apex.subtract(cameraPos), color, normalX, normalY, normalZ);
        addColoredLineVertex(buffer, ctx.matrixStack().last(), left.subtract(cameraPos), color, normalX, normalY, normalZ);
        addColoredLineVertex(buffer, ctx.matrixStack().last(), right.subtract(cameraPos), color, normalX, normalY, normalZ);
        addColoredLineVertex(buffer, ctx.matrixStack().last(), left.subtract(cameraPos), color, normalX, normalY, normalZ);
    }

    private static boolean shouldRender(Minecraft mc) {
        return FreecamManager.isEnabled()
                || !mc.options.getCameraType().isFirstPerson()
                || SlateConfig.HAT_RENDER_FIRST_PERSON.get();
    }

    private static void addColoredLineVertex(VertexConsumer buffer, PoseStack.Pose pose, Vec3 point, int color,
                                             float normalX, float normalY, float normalZ) {
        int alpha = (color >>> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        buffer.vertex(pose.pose(), (float) point.x, (float) point.y, (float) point.z)
                .color(red, green, blue, alpha)
                .normal(pose.normal(), normalX, normalY, normalZ)
                .endVertex();
    }
}
