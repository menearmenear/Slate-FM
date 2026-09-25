package menear.nclient.slate.bootstrap;

import menear.nclient.slate.modules.visuals.FreecamManager;
import menear.nclient.slate.modules.visuals.FreelookManager;
import menear.nclient.slate.modules.movement.MovementPlaybackManager;

public final class SlateTickHandlers {
    private SlateTickHandlers() {
    }

    public static void register() {
        SlateKeybindHandler.register();
        SlateReconnectTickHandler.register();
        SlateWorldChangeTickHandler.register();
        SlateAutomationTickHandler.register();
        MovementPlaybackManager.register();
        FreecamManager.register();
        FreelookManager.register();
    }

    public static void setPickingUpStash(boolean pickingUpStash) {
        SlateAutomationTickHandler.setPickingUpStash(pickingUpStash);
    }
}
