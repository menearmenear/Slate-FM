package menear.nclient.slate.renderer;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.config.RewarpPointPair;
import menear.nclient.slate.config.RewarpPointPairs;
import menear.nclient.slate.macro.MacroState;
import menear.nclient.slate.modules.visuals.StreamerModeManager;
import menear.nclient.slate.util.ClientUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

public final class PositionHighlighter {
    private PositionHighlighter() {}

    private static int color(int alpha, int red, int green, int blue) {
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public static boolean hasVisibleHighlights() {
        if (StreamerModeManager.isEnabled()) {
            return false;
        }
        if (hasVisibleGardenHighlights()) {
            return true;
        }
        return menear.nclient.slate.modules.metaldetector.MetalDetectorSolver.hasVisibleHighlights();
    }

    private static boolean hasVisibleGardenHighlights() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || ClientUtils.getCurrentLocation(mc) != MacroState.Location.GARDEN) {
            return false;
        }
        if (SlateConfig.PEST_HIGHLIGHT_DESK.get()) {
            return true;
        }
        if (SlateConfig.AUTO_COMPOSTER_HIGHLIGHT.get()) {
            return true;
        }
        for (RewarpPointPair pair : RewarpPointPairs.get()) {
            if ((pair.highlightStart && pair.hasStart()) || (pair.highlightEnd && pair.hasEnd())) {
                return true;
            }
        }
        if (!menear.nclient.slate.modules.GreenhouseManager.highlightedSkulls.isEmpty()) {
            return true;
        }
        List<Vec3> path = menear.nclient.slate.modules.GreenhouseManager.currentPath;
        if (path != null && !path.isEmpty()) {
            return true;
        }
        if (menear.nclient.slate.modules.GreenhouseManager.getCurrentTarget() != null) {
            return true;
        }
        if (menear.nclient.slate.modules.GreenhouseManager.getCurrentAimTarget() != null) {
            return true;
        }
        if (menear.nclient.slate.modules.farming.FastLaneSwitchManager.hasVisibleHighlights()) {
            return true;
        }
        return false;
    }

    public static void renderWorld(WorldRenderContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (StreamerModeManager.isEnabled()) return;
        if (mc.level == null || mc.player == null || ctx == null || ctx.matrixStack() == null
                || ctx.camera() == null || ctx.world() == null) return;
        if (!hasVisibleHighlights()) return;
        boolean inGarden = ClientUtils.getCurrentLocation(mc) == MacroState.Location.GARDEN;

        MultiBufferSource consumers = ctx.consumers();
        if (consumers == null) {
            consumers = mc.renderBuffers().bufferSource();
        }
        MultiBufferSource.BufferSource textBuffer = consumers instanceof MultiBufferSource.BufferSource bufferSource
                ? bufferSource
                : null;

        if (inGarden) {
            // Desk Position (Block highlight)
            if (SlateConfig.PEST_HIGHLIGHT_DESK.get()) {
                int x = SlateConfig.PEST_EXCHANGE_DESK_X.get();
                int y = SlateConfig.PEST_EXCHANGE_DESK_Y.get();
                int z = SlateConfig.PEST_EXCHANGE_DESK_Z.get();

                renderBlockHighlight(ctx, mc, consumers,
                        new AABB(x, y, z, x + 1, y + 1, z + 1),
                        "Pest Desk",
                        color(200, 255, 210, 0),
                        color(40, 255, 210, 0),
                        2.0f);
            }

            if (SlateConfig.AUTO_COMPOSTER_HIGHLIGHT.get()) {
                int x = SlateConfig.AUTO_COMPOSTER_X.get();
                int y = SlateConfig.AUTO_COMPOSTER_Y.get();
                int z = SlateConfig.AUTO_COMPOSTER_Z.get();

                renderBlockHighlight(ctx, mc, consumers,
                        new AABB(x, y, z, x + 1, y + 1, z + 1),
                        "Composter",
                        color(200, 90, 230, 120),
                        color(40, 90, 230, 120),
                        2.0f);
            }

            for (RewarpPointPair pair : RewarpPointPairs.get()) {
                renderRewarpPair(ctx, mc, consumers, pair);
            }

            // Greenhouse Skulls
            for (net.minecraft.world.phys.AABB box : menear.nclient.slate.modules.GreenhouseManager.highlightedSkulls) {
                renderBlockHighlight(ctx, mc, consumers,
                        box,
                        "Greenhouse Skull",
                        color(200, 255, 100, 255),
                        color(40, 255, 100, 255),
                        2.0f);
            }

            // Greenhouse Path
            var path = menear.nclient.slate.modules.GreenhouseManager.currentPath;
            if (path != null && !path.isEmpty()) {
                int lineArgb = color(217, 255, 100, 255);
                for (int i = 0; i < path.size() - 1; i++) {
                    drawLine(ctx, consumers, path.get(i), path.get(i + 1), lineArgb);
                }
            }

            // Greenhouse Current Target
            var target = menear.nclient.slate.modules.GreenhouseManager.getCurrentTarget();
            if (target != null) {
                renderBlockHighlight(ctx, mc, consumers,
                        new AABB(target.x - 0.3, target.y - 0.3, target.z - 0.3,
                                target.x + 0.3, target.y + 0.3, target.z + 0.3),
                        "Greenhouse Target",
                        color(255, 0, 255, 0),
                        color(60, 0, 255, 0),
                        2.5f);
            }

            // Greenhouse Aim Target
            var aimTarget = menear.nclient.slate.modules.GreenhouseManager.getCurrentAimTarget();
            if (aimTarget != null) {
                renderBlockHighlight(ctx, mc, consumers,
                        new AABB(aimTarget.x - 0.05, aimTarget.y - 0.05, aimTarget.z - 0.05,
                                aimTarget.x + 0.05, aimTarget.y + 0.05, aimTarget.z + 0.05),
                        "Greenhouse Aim",
                        color(255, 255, 255, 0),
                        color(90, 255, 255, 0),
                        2.0f);
            }

            menear.nclient.slate.modules.farming.FastLaneSwitchManager.renderWorld();
        }

        menear.nclient.slate.modules.metaldetector.MetalDetectorSolver.renderWorld(ctx);
        if (textBuffer != null) {
            textBuffer.endBatch();
        }
    }

    private static void renderRewarpPair(WorldRenderContext ctx, Minecraft mc,
                                         MultiBufferSource consumers,
                                         RewarpPointPair pair) {
        if (pair.highlightStart && pair.hasStart()) {
            renderRewarpPoint(ctx, mc, consumers, pair.startX, pair.startY, pair.startZ,
                    pair.displayName() + " Start",
                    color(200, 50, 255, 100),
                    color(40, 50, 255, 100));
        }
        if (pair.highlightEnd && pair.hasEnd()) {
            renderRewarpPoint(ctx, mc, consumers, pair.endX, pair.endY, pair.endZ,
                    pair.displayName() + " End",
                    color(200, 255, 50, 50),
                    color(40, 255, 50, 50));
        }
    }

    private static void renderRewarpPoint(WorldRenderContext ctx, Minecraft mc,
                                          MultiBufferSource consumers,
                                          double x, double y, double z,
                                          String label, int strokeColor, int fillColor) {
        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        int iz = (int) Math.floor(z);

        renderBlockHighlight(ctx, mc, consumers,
                new AABB(ix, iy, iz, ix + 1, iy + 1, iz + 1),
                label,
                strokeColor,
                fillColor,
                2.0f);
    }

    private static void renderBlockHighlight(WorldRenderContext ctx, Minecraft mc,
                                             MultiBufferSource consumers,
                                             AABB box, String label, int strokeColor,
                                             int fillColor, float lineWidth) {
        drawBox(ctx, consumers, box, strokeColor, fillColor);

        if (label != null && !label.isBlank()) {
            renderBillboardText(ctx, mc, consumers, label,
                    (box.minX + box.maxX) * 0.5,
                    box.maxY + 0.35,
                    (box.minZ + box.maxZ) * 0.5);
        }
    }

    private static void drawBox(WorldRenderContext ctx, MultiBufferSource consumers,
                                AABB box, int strokeColor, int fillColor) {
        if (ctx == null || ctx.camera() == null || ctx.matrixStack() == null || consumers == null) {
            return;
        }

        Vec3 cameraPos = ctx.camera().getPosition();
        drawFilledBox(ctx, consumers, box, fillColor, cameraPos);
        Vec3 min = new Vec3(box.minX, box.minY, box.minZ);
        Vec3 max = new Vec3(box.maxX, box.maxY, box.maxZ);

        drawLine(ctx, consumers, min, new Vec3(max.x, min.y, min.z), strokeColor);
        drawLine(ctx, consumers, new Vec3(max.x, min.y, min.z), new Vec3(max.x, min.y, max.z), strokeColor);
        drawLine(ctx, consumers, new Vec3(max.x, min.y, max.z), new Vec3(max.x, max.y, max.z), strokeColor);
        drawLine(ctx, consumers, new Vec3(max.x, max.y, max.z), new Vec3(min.x, max.y, max.z), strokeColor);
        drawLine(ctx, consumers, new Vec3(min.x, max.y, max.z), new Vec3(min.x, max.y, min.z), strokeColor);
        drawLine(ctx, consumers, new Vec3(min.x, max.y, min.z), min, strokeColor);

        drawLine(ctx, consumers, new Vec3(min.x, min.y, max.z), new Vec3(max.x, min.y, max.z), strokeColor);
        drawLine(ctx, consumers, new Vec3(max.x, min.y, max.z), new Vec3(max.x, max.y, max.z), strokeColor);
        drawLine(ctx, consumers, new Vec3(max.x, max.y, max.z), new Vec3(min.x, max.y, max.z), strokeColor);
        drawLine(ctx, consumers, new Vec3(min.x, max.y, max.z), new Vec3(min.x, min.y, max.z), strokeColor);
        drawLine(ctx, consumers, new Vec3(min.x, min.y, max.z), new Vec3(min.x, max.y, max.z), strokeColor);
    }

    private static void drawFilledBox(WorldRenderContext ctx, MultiBufferSource consumers,
                                       AABB box, int color, Vec3 cameraPos) {
        if (color == 0 || ctx == null || ctx.matrixStack() == null || consumers == null) {
            return;
        }

        PoseStack.Pose pose = ctx.matrixStack().last();
        Vec3 min = new Vec3(box.minX - cameraPos.x, box.minY - cameraPos.y, box.minZ - cameraPos.z);
        Vec3 max = new Vec3(box.maxX - cameraPos.x, box.maxY - cameraPos.y, box.maxZ - cameraPos.z);
        VertexConsumer buffer = consumers.getBuffer(RenderType.debugQuads());
        int alpha = (color >>> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        addFilledFace(buffer, pose, min, new Vec3(max.x, min.y, min.z),
                new Vec3(max.x, min.y, max.z), new Vec3(min.x, min.y, max.z),
                0.0f, -1.0f, 0.0f, red, green, blue, alpha);
        addFilledFace(buffer, pose, new Vec3(min.x, max.y, min.z), new Vec3(min.x, max.y, max.z),
                new Vec3(max.x, max.y, max.z), new Vec3(max.x, max.y, min.z),
                0.0f, 1.0f, 0.0f, red, green, blue, alpha);
        addFilledFace(buffer, pose, new Vec3(min.x, min.y, min.z), new Vec3(min.x, max.y, min.z),
                new Vec3(min.x, max.y, max.z), new Vec3(min.x, min.y, max.z),
                -1.0f, 0.0f, 0.0f, red, green, blue, alpha);
        addFilledFace(buffer, pose, new Vec3(max.x, min.y, max.z), new Vec3(max.x, max.y, max.z),
                new Vec3(max.x, max.y, min.z), new Vec3(max.x, min.y, min.z),
                1.0f, 0.0f, 0.0f, red, green, blue, alpha);
        addFilledFace(buffer, pose, new Vec3(max.x, min.y, min.z), new Vec3(max.x, max.y, min.z),
                new Vec3(min.x, max.y, min.z), new Vec3(min.x, min.y, min.z),
                0.0f, 0.0f, -1.0f, red, green, blue, alpha);
        addFilledFace(buffer, pose, new Vec3(min.x, min.y, max.z), new Vec3(min.x, max.y, max.z),
                new Vec3(max.x, max.y, max.z), new Vec3(max.x, min.y, max.z),
                0.0f, 0.0f, 1.0f, red, green, blue, alpha);
    }

    private static void addFilledFace(VertexConsumer buffer, PoseStack.Pose pose,
                                      Vec3 a, Vec3 b, Vec3 c, Vec3 d,
                                      float normalX, float normalY, float normalZ,
                                      int red, int green, int blue, int alpha) {
        addFilledVertex(buffer, pose, a, normalX, normalY, normalZ, red, green, blue, alpha);
        addFilledVertex(buffer, pose, b, normalX, normalY, normalZ, red, green, blue, alpha);
        addFilledVertex(buffer, pose, c, normalX, normalY, normalZ, red, green, blue, alpha);
        addFilledVertex(buffer, pose, a, normalX, normalY, normalZ, red, green, blue, alpha);
    }

    private static void addFilledVertex(VertexConsumer buffer, PoseStack.Pose pose, Vec3 point,
                                        float normalX, float normalY, float normalZ,
                                        int red, int green, int blue, int alpha) {
        buffer.vertex(pose.pose(), (float) point.x, (float) point.y, (float) point.z)
                .color(red, green, blue, alpha)
                .normal(pose.normal(), normalX, normalY, normalZ)
                .endVertex();
    }

    private static void drawLine(WorldRenderContext ctx, MultiBufferSource consumers,
                                 Vec3 from, Vec3 to, int color) {
        if (ctx == null || ctx.camera() == null || ctx.matrixStack() == null || consumers == null) {
            return;
        }

        PoseStack.Pose pose = ctx.matrixStack().last();
        Vec3 cameraPos = ctx.camera().getPosition();
        double fromX = from.x - cameraPos.x;
        double fromY = from.y - cameraPos.y;
        double fromZ = from.z - cameraPos.z;
        double toX = to.x - cameraPos.x;
        double toY = to.y - cameraPos.y;
        double toZ = to.z - cameraPos.z;
        double directionX = toX - fromX;
        double directionY = toY - fromY;
        double directionZ = toZ - fromZ;
        double length = Math.sqrt(directionX * directionX + directionY * directionY + directionZ * directionZ);
        if (length < 1.0E-6) {
            directionY = 1.0;
        } else {
            directionX /= length;
            directionY /= length;
            directionZ /= length;
        }

        int alpha = (color >>> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        VertexConsumer consumer = consumers.getBuffer(RenderType.lines());
        consumer.vertex(pose.pose(), (float) fromX, (float) fromY, (float) fromZ)
                .color(red, green, blue, alpha)
                .normal(pose.normal(), (float) directionX, (float) directionY, (float) directionZ)
                .endVertex();
        consumer.vertex(pose.pose(), (float) toX, (float) toY, (float) toZ)
                .color(red, green, blue, alpha)
                .normal(pose.normal(), (float) directionX, (float) directionY, (float) directionZ)
                .endVertex();
    }

    private static void renderBillboardText(WorldRenderContext ctx, Minecraft mc,
                                            MultiBufferSource consumers,
                                            String text, double x, double y, double z) {
        if (ctx == null || ctx.matrixStack() == null || ctx.camera() == null || consumers == null) {
            return;
        }

        Camera camera = ctx.camera();
        PoseStack poseStack = ctx.matrixStack();
        Vec3 cameraPos = camera.getPosition();
        Font font = mc.font;
        float scale = 0.025f;

        poseStack.pushPose();
        Matrix4f matrix = poseStack.last().pose();
        matrix.translate((float) x, (float) y, (float) z)
                .translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z)
                .rotate(camera.rotation())
                .scale(scale, -scale, scale);
        float textX = -font.width(text) / 2.0f;
        font.drawInBatch(text, textX, 0.0f, 0xFFFFFFFF, true, matrix, consumers,
                Font.DisplayMode.SEE_THROUGH, 0, 0x00F000F0);
        poseStack.popPose();
    }
}
