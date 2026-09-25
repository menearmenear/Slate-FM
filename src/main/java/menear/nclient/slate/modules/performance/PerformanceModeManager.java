package menear.nclient.slate.modules.performance;

import menear.nclient.slate.config.SlateConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;

public class PerformanceModeManager {
    private static int renderDistanceBefore = 0;
    private static int maxFpsBefore = 0;
    private static ParticleStatus particlesBefore = null;
    private static boolean changedRenderDistance = false;
    private static boolean changedMaxFps = false;
    private static boolean changedParticles = false;
    private static boolean enabled = false;
    
    public static boolean isEnabled() {
        return enabled;
    }

    public static void start(Minecraft mc) {
        if (!SlateConfig.PERFORMANCE_MODE.get()) return;
        if (enabled) return;
        
        enabled = true;
        if (mc.options != null) {
            renderDistanceBefore = mc.options.renderDistance().get();
            maxFpsBefore = mc.options.framerateLimit().get();
            particlesBefore = mc.options.particles().get();

            if (SlateConfig.PERFORMANCE_LIMIT_CHUNK_DISTANCE.get()) {
                mc.options.renderDistance().set(SlateConfig.PERFORMANCE_CHUNK_DISTANCE.get());
                changedRenderDistance = true;
            }
            if (SlateConfig.PERFORMANCE_LIMIT_FPS.get()) {
                mc.options.framerateLimit().set(SlateConfig.PERFORMANCE_MODE_MAX_FPS.get());
                changedMaxFps = true;
            }
            if (SlateConfig.PERFORMANCE_DISABLE_PARTICLES.get()) {
                mc.options.particles().set(ParticleStatus.MINIMAL);
                changedParticles = true;
            }
            
            if (mc.levelRenderer != null) {
                mc.levelRenderer.allChanged();
            }
        }
    }

    public static void stop(Minecraft mc) {
        if (!enabled) return;
        
        enabled = false;
        if (mc.options != null) {
            // Restore only if they were actually changed by us
            if (changedRenderDistance) {
                mc.options.renderDistance().set(renderDistanceBefore);
            }
            if (changedMaxFps) {
                mc.options.framerateLimit().set(maxFpsBefore);
            }
            if (changedParticles) {
                mc.options.particles().set(particlesBefore);
            }
            
            renderDistanceBefore = 0;
            maxFpsBefore = 0;
            particlesBefore = null;
            changedRenderDistance = false;
            changedMaxFps = false;
            changedParticles = false;
            
            if (mc.levelRenderer != null) {
                mc.levelRenderer.allChanged();
            }
        }
    }

    public static boolean isParticlesDisabled() {
        return enabled && SlateConfig.PERFORMANCE_DISABLE_PARTICLES.get();
    }
}
