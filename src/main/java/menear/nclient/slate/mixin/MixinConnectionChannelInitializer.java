package menear.nclient.slate.mixin;

import menear.nclient.slate.proxy.SlateProxy;
import menear.nclient.slate.proxy.SlateProxyManager;
import io.netty.channel.Channel;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.network.Connection$1")
public final class MixinConnectionChannelInitializer {
    @Inject(method = "initChannel(Lio/netty/channel/Channel;)V", at = @At("HEAD"))
    private void slate$addProxyHandler(Channel channel, CallbackInfo ci) {
        SlateProxy proxy = SlateProxyManager.selectedProxy();
        if (proxy == null) {
            SlateProxyManager.markConnectionProxy(null);
            return;
        }

        if (proxy.type() == SlateProxy.ProxyType.SOCKS5) {
            channel.pipeline().addFirst(new Socks5ProxyHandler(
                    proxy.socketAddress(),
                    proxy.username().isEmpty() ? null : proxy.username(),
                    proxy.password().isEmpty() ? null : proxy.password()));
        } else {
            channel.pipeline().addFirst(new Socks4ProxyHandler(
                    proxy.socketAddress(),
                    proxy.username().isEmpty() ? null : proxy.username()));
        }
        SlateProxyManager.markConnectionProxy(proxy);
    }
}
