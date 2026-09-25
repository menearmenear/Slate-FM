package menear.nclient.slate;

import menear.nclient.slate.bootstrap.SlateBootstrapHooks;
import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.feature.ClientFeatureBootstrap;
import menear.nclient.slate.feature.LiveSlateBootstrapHooks;
import menear.nclient.slate.proxy.SlateProxyManager;
import menear.nclient.slate.renderer.SlateRenderQueue;
import menear.nclient.slate.renderer.NanoVGManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class SlateClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SlateConfig.init();
        SlateProxyManager.init();
        SlateBootstrapHooks.install(new LiveSlateBootstrapHooks());
        ClientFeatureBootstrap.initialize();

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ClientFeatureBootstrap.shutdown());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> SlateConfig.flush());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            SlateBootstrapHooks.reset();
            SlateRenderQueue.clear();
            if (NanoVGManager.isInitialized()) {
                NanoVGManager.destroy();
            }
        });
    }
}
