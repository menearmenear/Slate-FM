package menear.nclient.slate.modules.pathfinding.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import menear.nclient.slate.modules.pathfinding.Node;
import menear.nclient.slate.modules.pathfinding.util.BlockPosUtil;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public final class PathVisualizer {

    private static final int MAX_EXPLORED      = 3000;
    private static final int MAX_RENDER        = 300;
    private static final int MAX_EXPLORED_DRAW = 1000;
    private static final double BEZIER_TANGENT = 0.4;
    private static final int    BEZIER_STEPS   = 8;
    private static final int MAX_CAMERA_RENDER  = 480;

    private static boolean    enabled                = false;
    private static boolean    registered             = false;
    private static boolean    transientSessionActive = false;
    private static List<Node> currentPath           = Collections.emptyList();
    private static int        currentWaypointIndex  = 0;
    private static List<Vec3> cameraPath             = Collections.emptyList();
    private static int        currentCameraRailIndex = -1;

    private static final Set<Long> exploredNodes =
            Collections.synchronizedSet(new LinkedHashSet<>());

    private static Vec3[][] bezierCache = null;

    private PathVisualizer() {}

    private static int color(int alpha, int red, int green, int blue) {
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public static void register() {
        if (registered) return;
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(PathVisualizer::renderWorld);
        registered = true;
    }

    public static void captureCamera(Minecraft mc) {
        if (mc == null || mc.player == null || !mc.isWindowActive()) return;
        if (cameraPath.isEmpty() || currentCameraRailIndex < 0 || currentCameraRailIndex >= cameraPath.size()) return;
        if (mc.gui.getGuiTicks() % 2 != 0) return;
        Vec3 eye = mc.player.getEyePosition();
        cameraPath.add(eye);
        if (cameraPath.size() > 512) {
            cameraPath = new ArrayList<>(cameraPath.subList(cameraPath.size() - 512, cameraPath.size()));
            if (currentCameraRailIndex >= 0) currentCameraRailIndex--;
        }
    }

    public static void beginTransientSession() {
        transientSessionActive = true;
        clear();
    }

    public static void endTransientSession() {
        transientSessionActive = false;
    }

    public static void setPath(List<Node> path, int wpIndex) {
        currentPath          = (path != null) ? path : Collections.emptyList();
        currentWaypointIndex = wpIndex;
        bezierCache          = null;
    }

    public static void setCameraPath(List<Vec3> path) {
        cameraPath = (path != null) ? path : Collections.emptyList();
        if (currentCameraRailIndex >= cameraPath.size()) currentCameraRailIndex = -1;
    }

    public static void addExplored(int x, int y, int z) {
        if (exploredNodes.size() < MAX_EXPLORED) {
            exploredNodes.add(BlockPosUtil.pack(x, y, z));
        }
    }

    public static void toggle() {
        enabled = !enabled;
        if (!enabled) clear();
    }

    public static void updateExecution(int wpIndex, int camTargetIdx) {
        currentWaypointIndex  = wpIndex;
        if (camTargetIdx >= 0) {
            currentCameraRailIndex = camTargetIdx;
        }
    }

    public static void updateCameraExecution(int camRailIdx) {
        currentCameraRailIndex = camRailIdx;
    }

    public static void clear() {
        currentPath           = Collections.emptyList();
        cameraPath            = Collections.emptyList();
        currentWaypointIndex  = 0;
        currentCameraRailIndex = -1;
        exploredNodes.clear();
        bezierCache = null;
    }

    public static boolean isEnabled() { return enabled; }

    public static boolean isTransientSessionActive() {
        return transientSessionActive;
    }

    public static boolean shouldRender() {
        return enabled || transientSessionActive;
    }

    public static boolean shouldCaptureExploredNodes() {
        return shouldRender();
    }

    public static void renderWorld() {}

    public static void renderWorld(WorldRenderContext context) {
        if (!shouldRender() || context == null || context.matrixStack() == null
                || context.camera() == null || context.world() == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        MultiBufferSource consumers = context.consumers();
        if (consumers == null) {
            consumers = mc.renderBuffers().bufferSource();
        }
        if (consumers == null) return;
        MultiBufferSource.BufferSource bufferSource = consumers instanceof MultiBufferSource.BufferSource source
                ? source
                : null;
        Vec3 cameraPos = context.camera().getPosition();

        List<Node> path  = currentPath;
        int        limit = Math.min(path.size(), MAX_RENDER);
        int        last  = path.size() - 1;

        // Explored nodes
        boolean hasExplored;
        synchronized (exploredNodes) { hasExplored = !exploredNodes.isEmpty(); }
        if (hasExplored) {
            int exploredColor = color(80, 255, 48, 32);
            synchronized (exploredNodes) {
                int count = 0;
                for (long key : exploredNodes) {
                    if (count++ >= MAX_EXPLORED_DRAW) break;
                    int x = BlockPosUtil.unpackX(key);
                    int y = BlockPosUtil.unpackY(key);
                    int z = BlockPosUtil.unpackZ(key);
                    drawBox(context, consumers, cameraPos, new AABB(x, y - 1, z, x + 1, y, z + 1),
                            exploredColor, color(15, 255, 48, 32));
                }
            }
        }

        if (limit > 0) {
            if (bezierCache == null && limit >= 2) {
                bezierCache = buildBezierCache(path, limit);
            }

            // Keynode boxes only (skip intermediates)
            for (int i = 0; i < limit; i++) {
                Node n = path.get(i);
                if (!n.isKeynode) continue;
                int[] argb = nodeArgb(n);
                int nx = n.position.flooredX();
                int ny = n.position.flooredY();
                int nz = n.position.flooredZ();
                drawBox(context, consumers, cameraPos, new AABB(nx, ny - 1, nz, nx + 1, ny, nz + 1),
                        argb[0], argb[1]);
            }

            // Bezier path lines
            if (bezierCache != null) {
                for (int i = 0; i < limit - 1; i++) {
                    if (bezierCache[i] == null) continue;
                    int lineArgb = lineArgb(i, last);
                    Vec3[] pts = bezierCache[i];
                    for (int j = 0; j < BEZIER_STEPS - 1; j++) {
                        drawLine(context, consumers, cameraPos, pts[j], pts[j + 1], lineArgb);
                    }
                }
            }
        }

        renderCameraRail(context, consumers, cameraPos);
        if (bufferSource != null) {
            bufferSource.endBatch();
        }
    }

    private static void renderCameraRail(WorldRenderContext context, MultiBufferSource consumers, Vec3 cameraPos) {
        if (cameraPath.isEmpty()) return;

        int limit = Math.min(cameraPath.size(), MAX_CAMERA_RENDER);

        for (int i = 0; i < limit - 1; i++) {
            Vec3 a = cameraPath.get(i);
            Vec3 b = cameraPath.get(i + 1);
            int argb = color(200, 120, 220, 255);
            if (i < currentCameraRailIndex) argb = color(120, 120, 140, 160);
            drawLine(context, consumers, cameraPos, a, b, argb);
        }

        for (int i = 0; i < limit; i++) {
            Vec3 p = cameraPath.get(i);
            boolean active = i == currentCameraRailIndex;
            int argb = active ? color(255, 255, 120, 255) : color(160, 145, 145, 145);
            int fillColor = active ? color(110, 175, 60, 255) : color(25, 120, 120, 120);
            double r = active ? 0.22 : 0.08;
            drawBox(context, consumers, cameraPos,
                    new AABB(p.x - r, p.y - r, p.z - r, p.x + r, p.y + r, p.z + r), argb, fillColor);
        }
    }

    private static void drawBox(WorldRenderContext context, MultiBufferSource consumers, Vec3 cameraPos,
                                AABB box, int strokeColor, int fillColor) {
        drawFilledBox(context, consumers, cameraPos, box, fillColor);
        Vec3 min = new Vec3(box.minX, box.minY, box.minZ);
        Vec3 max = new Vec3(box.maxX, box.maxY, box.maxZ);

        drawLine(context, consumers, cameraPos, min, new Vec3(max.x, min.y, min.z), strokeColor);
        drawLine(context, consumers, cameraPos, new Vec3(max.x, min.y, min.z), new Vec3(max.x, min.y, max.z), strokeColor);
        drawLine(context, consumers, cameraPos, new Vec3(max.x, min.y, max.z), new Vec3(max.x, max.y, max.z), strokeColor);
        drawLine(context, consumers, cameraPos, new Vec3(max.x, max.y, max.z), new Vec3(min.x, max.y, max.z), strokeColor);
        drawLine(context, consumers, cameraPos, new Vec3(min.x, max.y, max.z), new Vec3(min.x, max.y, min.z), strokeColor);
        drawLine(context, consumers, cameraPos, new Vec3(min.x, max.y, min.z), min, strokeColor);

        drawLine(context, consumers, cameraPos, new Vec3(min.x, min.y, max.z), new Vec3(max.x, min.y, max.z), strokeColor);
        drawLine(context, consumers, cameraPos, new Vec3(max.x, min.y, max.z), new Vec3(max.x, max.y, max.z), strokeColor);
        drawLine(context, consumers, cameraPos, new Vec3(max.x, max.y, max.z), new Vec3(min.x, max.y, max.z), strokeColor);
        drawLine(context, consumers, cameraPos, new Vec3(min.x, max.y, max.z), new Vec3(min.x, min.y, max.z), strokeColor);
        drawLine(context, consumers, cameraPos, new Vec3(min.x, min.y, max.z), new Vec3(min.x, max.y, max.z), strokeColor);
    }

    private static void drawFilledBox(WorldRenderContext context, MultiBufferSource consumers, Vec3 cameraPos,
                                       AABB box, int color) {
        if (color == 0) return;

        PoseStack.Pose pose = context.matrixStack().last();
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

    private static void drawLine(WorldRenderContext context, MultiBufferSource consumers, Vec3 cameraPos,
                                 Vec3 from, Vec3 to, int color) {
        PoseStack.Pose pose = context.matrixStack().last();
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

    private static Vec3[][] buildBezierCache(List<Node> path, int limit) {
        Vec3[][] cache = new Vec3[limit - 1][];
        for (int i = 0; i < limit - 1; i++) {
            Vec3 a = nodeFloorCenter(path.get(i));
            Vec3 b = nodeFloorCenter(path.get(i + 1));
            Vec3 prev = (i > 0)
                    ? nodeFloorCenter(path.get(i - 1))
                    : new Vec3(2*a.x - b.x, 2*a.y - b.y, 2*a.z - b.z);
            Vec3 next = (i + 2 < limit)
                    ? nodeFloorCenter(path.get(i + 2))
                    : new Vec3(2*b.x - a.x, 2*b.y - a.y, 2*b.z - a.z);
            Vec3 dirIn  = safeNormalize(a.subtract(prev));
            Vec3 dirOut = safeNormalize(next.subtract(b));
            Vec3 ab     = safeNormalize(b.subtract(a));
            if (dirIn.lengthSqr()  < 0.001) dirIn  = ab;
            if (dirOut.lengthSqr() < 0.001) dirOut = ab;
            Vec3 cp1 = a.add(dirIn.scale(BEZIER_TANGENT));
            Vec3 cp2 = b.subtract(dirOut.scale(BEZIER_TANGENT));
            Vec3[] pts = new Vec3[BEZIER_STEPS];
            for (int j = 0; j < BEZIER_STEPS; j++) {
                pts[j] = evalCubicBezier(a, cp1, cp2, b, j / (double)(BEZIER_STEPS - 1));
            }
            cache[i] = pts;
        }
        return cache;
    }

    private static Vec3 nodeFloorCenter(Node n) {
        return new Vec3(n.position.flooredX() + 0.5, n.position.flooredY() - 0.5, n.position.flooredZ() + 0.5);
    }

    private static Vec3 safeNormalize(Vec3 v) {
        double len = v.length();
        return len < 0.001 ? Vec3.ZERO : v.scale(1.0 / len);
    }

    private static Vec3 evalCubicBezier(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, double t) {
        double mt = 1.0 - t;
        double c0 = mt*mt*mt, c1 = 3*mt*mt*t, c2 = 3*mt*t*t, c3 = t*t*t;
        return new Vec3(c0*p0.x+c1*p1.x+c2*p2.x+c3*p3.x,
                        c0*p0.y+c1*p1.y+c2*p2.y+c3*p3.y,
                        c0*p0.z+c1*p1.z+c2*p2.z+c3*p3.z);
    }

    private static int[] nodeArgb(Node n) {
        if (!n.isKeynode) {
            // Intermediate tracking node - light pink
            return new int[]{ color(200, 255, 180, 200), color(50, 255, 180, 200) };
        }
        if (n.moveType == Node.MoveType.ETHERWARP) {
            return new int[]{ color(235, 80, 220, 255), color(80, 80, 220, 255) };
        }
        // Keynode - purple
        return new int[]{ color(230, 180, 50, 255), color(70, 180, 50, 255) };
    }

    private static int lineArgb(int i, int last) {
        if (i >= last - 1)             return color(217,  51,128,255);
        if (i < currentWaypointIndex)  return color(130, 128,128,128);
        if (i == currentWaypointIndex) return color(217, 255,255,  0);
        return                                color(217,  26,204, 26);
    }
}
