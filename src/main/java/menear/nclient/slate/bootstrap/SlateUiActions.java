package menear.nclient.slate.bootstrap;

import menear.nclient.slate.Slate;
import menear.nclient.slate.ui.MainGUI;
import menear.nclient.slate.ui.MainGUIRegistry;
import menear.nclient.slate.util.ClientUtils;
import net.minecraft.client.Minecraft;

public final class SlateUiActions {
    private SlateUiActions() {
    }

    public static void toggleMainGui(Minecraft client) {
        if (client.screen instanceof MainGUI) {
            client.setScreen(null);
        } else {
            openMainGui(client);
        }
    }

    public static void openMainGui(Minecraft client) {
        if (client == null) {
            return;
        }

        try {
            MainGUIRegistry.refresh();
            client.execute(() -> {
                try {
                    client.setScreen(new MainGUI());
                } catch (RuntimeException | LinkageError e) {
                    Slate.LOGGER.error("Failed to open Slate GUI from queued client task", e);
                    ClientUtils.sendMessage(client, "\u00A7cFailed to open the Slate GUI. Check the client log.", false);
                }
            });
        } catch (RuntimeException | LinkageError e) {
            Slate.LOGGER.error("Failed to open Slate GUI", e);
            ClientUtils.sendMessage(client, "\u00A7cFailed to open the Slate GUI. Check the client log.", false);
        }
    }
}
