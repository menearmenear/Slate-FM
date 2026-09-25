package menear.nclient.slate.bootstrap;

import menear.nclient.slate.macro.MacroStateManager;
import menear.nclient.slate.macro.MacroWorkerThread;
import menear.nclient.slate.modules.visitor.VisitorManager;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

public final class SlateScreenHooks {
    private static String lastScannedVisitorTitle = null;

    private SlateScreenHooks() {
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof AbstractContainerScreen)) {
                return;
            }

            ScreenEvents.afterRender(screen).register((renderedScreen, graphics, mouseX, mouseY, tickDelta) -> {
                if (!(renderedScreen instanceof AbstractContainerScreen containerScreen)) {
                    return;
                }

                String title = containerScreen.getTitle().getString().trim();
                if (title.equals(lastScannedVisitorTitle) || !MacroStateManager.isMacroRunning()) {
                    return;
                }

                if (containerScreen.getMenu().slots.size() <= 29) {
                    return;
                }

                Slot slot = containerScreen.getMenu().getSlot(29);
                if (slot == null || !slot.hasItem()) {
                    return;
                }

                String itemName = slot.getItem().getHoverName().getString();
                if (!itemName.contains("Accept Offer")) {
                    return;
                }

                lastScannedVisitorTitle = title;
                MacroWorkerThread.getInstance().submit("VisitorGui-Scan",
                        () -> VisitorManager.scanVisitorGui(Minecraft.getInstance(), containerScreen));
            });
        });
    }

}

