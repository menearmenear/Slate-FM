package menear.nclient.slate.bootstrap;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.bootstrap.SlateBootstrapHooks;
import menear.nclient.slate.bootstrap.SlateUiActions;
import menear.nclient.slate.macro.FarmingMacroManager;
import menear.nclient.slate.macro.MacroState;
import menear.nclient.slate.macro.MacroStateManager;
import menear.nclient.slate.modules.CropFeverManager;
import menear.nclient.slate.modules.farming.SqueakyMousematManager;
import menear.nclient.slate.modules.gear.GearManager;
import menear.nclient.slate.modules.inventorymanager.AutoSellManager;
import menear.nclient.slate.modules.inventorymanager.BookCombineManager;
import menear.nclient.slate.modules.inventorymanager.GeorgeManager;
import menear.nclient.slate.modules.inventorymanager.JunkManager;
import menear.nclient.slate.modules.pest.PestManager;
import menear.nclient.slate.modules.profit.ProfitManager;
import menear.nclient.slate.modules.session.DynamicRestManager;
import menear.nclient.slate.modules.session.RecoveryManager;
import menear.nclient.slate.modules.visuals.PipManager;
import menear.nclient.slate.modules.visuals.UngrabMouseManager;
import menear.nclient.slate.util.SlateResources;
import menear.nclient.slate.util.ClientUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class SlateKeybindHandler {
    private static boolean tickHandlerRegistered;

    private SlateKeybindHandler() {
    }

    public static void register() {
        SlateKeybindRegistry.register();
        if (tickHandlerRegistered) {
            return;
        }
        tickHandlerRegistered = true;
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
                        while (SlateKeybindRegistry.getClickGuiKey().consumeClick()) {
                SlateUiActions.toggleMainGui(client);
            }

            if (client.player == null) {
                return;
            }

            while (SlateKeybindRegistry.getMacroToggleKey().consumeClick()) {
                handleMacroToggle(client);
            }

            // Freecam + its teleport bind are polled directly from the physical key state in
            // FreecamManager (like Freelook), so they are not consumed here.

            while (SlateKeybindRegistry.getPipKey().consumeClick()) {
                PipManager.toggle(client);
            }

            while (SlateKeybindRegistry.getUngrabMouseKey().consumeClick()) {
                UngrabMouseManager.toggle(client);
            }
        });
    }

    public static List<RegisteredKeybind> getRegisteredKeybinds() {
        return SlateKeybindRegistry.getRegisteredKeybinds().stream()
                .map(registeredKeybind -> new RegisteredKeybind(
                        registeredKeybind.name(),
                        registeredKeybind.description(),
                        registeredKeybind.mapping()))
                .toList();
    }

    public static KeyMapping getFreecamKey() {
        return SlateKeybindRegistry.getFreecamKey();
    }

    public static KeyMapping getFreecamTeleportToPlayerKey() {
        return SlateKeybindRegistry.getFreecamTeleportToPlayerKey();
    }

    public static KeyMapping getPipKey() {
        return SlateKeybindRegistry.getPipKey();
    }

    public static KeyMapping getUngrabMouseKey() {
        return SlateKeybindRegistry.getUngrabMouseKey();
    }

    private static void handleMacroToggle(Minecraft client) {
        if (MacroStateManager.getCurrentState() == MacroState.State.OFF) {
            startFarmingMacro(client, false);
            return;
        }

        if (!SlateConfig.PERSIST_SESSION_TIMER.get()) {
            DynamicRestManager.reset();
        }
        MacroStateManager.stopMacro(client);
    }

    public static void startFarmingMacro(Minecraft client) {
        startFarmingMacro(client, true);
    }

    public static void startFarmingMacro(Minecraft client, boolean announce) {
        if (client == null) {
            return;
        }

        PestManager.reset();
        CropFeverManager.reset();
        SlateBootstrapHooks.resetFailsafeRuntimeState();
        GearManager.reset();
        GeorgeManager.reset();
        AutoSellManager.reset();
        BookCombineManager.reset();
        JunkManager.reset();
        RecoveryManager.reset();
        SqueakyMousematManager.armReapplyAttempt();
        MacroStateManager.setCurrentState(MacroState.State.FARMING);
        ProfitManager.startStartupPriceFetch();
        ProfitManager.printPetXpPriceDebug(client);
        DynamicRestManager.scheduleNextRest();
        client.execute(() -> FarmingMacroManager.enable(client, FarmingMacroManager.createMacroFromConfig()));
        if (announce) {
            ClientUtils.sendMessage(client, "\u00A7aFarming macro started.", false);
        }
    }

    public record RegisteredKeybind(String name, String description, KeyMapping mapping) {
    }

}

