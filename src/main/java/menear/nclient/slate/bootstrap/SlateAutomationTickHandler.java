package menear.nclient.slate.bootstrap;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.config.ConfigHelpers;
import menear.nclient.slate.macro.FarmingMacroManager;
import menear.nclient.slate.macro.MacroState;
import menear.nclient.slate.macro.MacroStateManager;
import menear.nclient.slate.bootstrap.SlateBootstrapHooks;
import menear.nclient.slate.modules.CropFeverManager;
import menear.nclient.slate.modules.discord.DiscordStatusManager;
import menear.nclient.slate.modules.gear.GearManager;
import menear.nclient.slate.modules.inventorymanager.AutoSellManager;
import menear.nclient.slate.modules.inventorymanager.BookCombineManager;
import menear.nclient.slate.modules.inventorymanager.BoosterCookieManager;
import menear.nclient.slate.modules.inventorymanager.GeorgeManager;
import menear.nclient.slate.modules.metaldetector.MetalDetectorSolver;
import menear.nclient.slate.modules.misc.AutoCarnivalManager;
import menear.nclient.slate.modules.inventorymanager.JunkManager;
import menear.nclient.slate.modules.pathfinding.PathfindingManager;
import menear.nclient.slate.modules.pathfinding.rotation.RotationExecutor;
import menear.nclient.slate.modules.pest.DynamicPestsManager;
import menear.nclient.slate.modules.pest.PestManager;
import menear.nclient.slate.modules.pest.helpers.AutoPestExchangeManager;
import menear.nclient.slate.modules.pest.helpers.AutoSprayonatorManager;
import menear.nclient.slate.modules.pest.helpers.PestAotvManager;
import menear.nclient.slate.modules.pest.helpers.PestBonusManager;
import menear.nclient.slate.modules.pest.helpers.PestDestroyer;
import menear.nclient.slate.modules.pest.helpers.PestReturnManager;
import menear.nclient.slate.modules.pest.helpers.VacuumParticleDebug;
import menear.nclient.slate.modules.profit.ProfitManager;
import menear.nclient.slate.modules.rewarp.RewarpManager;
import menear.nclient.slate.modules.rotation.RotationManager;
import menear.nclient.slate.modules.session.DynamicRestManager;
import menear.nclient.slate.modules.session.RecoveryManager;
import menear.nclient.slate.modules.session.RestartManager;
import menear.nclient.slate.modules.SupercraftManager;
import menear.nclient.slate.ui.theme.Theme;
import menear.nclient.slate.util.SlateResources;
import menear.nclient.slate.util.BpsTracker;
import menear.nclient.slate.util.ClientUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public final class SlateAutomationTickHandler {
    private static boolean isPickingUpStash = false;
    private static long lastStashPickupTime = 0;

    private SlateAutomationTickHandler() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
                        if (client.player == null) {
                return;
            }

            if ((client.screen instanceof PauseScreen
                    || client.screen instanceof ChatScreen
                    || SlateBootstrapHooks.isBootstrapConfigScreen(client.screen))
                    && MacroStateManager.isMacroRunning()) {
                MacroStateManager.stopMacro(client);
            }

            handleContainerMenus(client);
            tickManagers(client);
            handleSneakForAotv(client);
            handleFlightStop(client);

            if (MacroStateManager.getCurrentState() == MacroState.State.RECOVERING
                    || RecoveryManager.isWorldChangeRecoveryActive()) {
                RecoveryManager.update(client);
                return;
            }

            handleStashPickup(client);
            RewarpManager.handle(client);
        });
    }

    public static void setPickingUpStash(boolean pickingUpStash) {
        isPickingUpStash = pickingUpStash;
    }

    private static void handleContainerMenus(Minecraft client) {
        if (!(client.screen instanceof AbstractContainerScreen<?> currentScreen)) {
            return;
        }

        GearManager.handleLoadoutMenu(client, currentScreen);
        if (client.screen == currentScreen) {
            GeorgeManager.handleGeorgeMenu(client, currentScreen);
        }
        if (client.screen == currentScreen) {
            AutoSellManager.handleMenu(client, currentScreen);
        }
        if (client.screen == currentScreen) {
            BoosterCookieManager.handleBoosterCookieMenu(client, currentScreen);
        }
        if (client.screen == currentScreen) {
            BookCombineManager.handleAnvilMenu(client, currentScreen);
        }
        if (client.screen == currentScreen) {
            JunkManager.handleInventoryMenu(client, currentScreen);
        }
        if (client.screen == currentScreen) {
            MetalDetectorSolver.handleContainerMenu(client, currentScreen);
        }
        if (client.screen == currentScreen) {
            SupercraftManager.handleRecipeGui(client, currentScreen);
        }
    }

    private static void tickManagers(Minecraft client) {
        GeorgeManager.update(client);
        AutoSellManager.update(client);
        BookCombineManager.update(client);
        JunkManager.update(client);

        DynamicRestManager.update(client);
        SupercraftManager.update(client);
        PestBonusManager.updateFromTab(client);
        AutoPestExchangeManager.update(client);
        PestManager.update(client);
        CropFeverManager.update(client);
        AutoSprayonatorManager.update(client);
        DynamicPestsManager.update(client);
        SlateBootstrapHooks.tickFailsafes(client);
        GearManager.cleanupTick(client);
        RotationManager.update(client);
        RotationExecutor.update(client);
        if (MacroStateManager.getCurrentState() == MacroState.State.FARMING) {
            FarmingMacroManager.tick(client);
        }
        MacroStateManager.periodicUpdate();
        ProfitManager.update(client);
        BpsTracker.tick();
        DiscordStatusManager.update(client);
        Theme.tickRainbow();
        PathfindingManager.update(client);
        RestartManager.update(client);
        AutoCarnivalManager.update(client);

        PestDestroyer.update(client);
        MetalDetectorSolver.update(client);
        VacuumParticleDebug.onClientTick(client);
    }

    private static void handleSneakForAotv(Minecraft client) {
        if (PestAotvManager.isSneakingForAotv && client.options != null) {
            ClientUtils.setKeyMappingState(client.options.keyShift, true);
        }
    }

    private static void handleFlightStop(Minecraft client) {
        if (!PestReturnManager.isStoppingFlight) {
            return;
        }

        PestReturnManager.flightStopTicks++;
        switch (PestReturnManager.flightStopStage) {
            case 0:
                if (client.options.keyJump != null) {
                    ClientUtils.setKeyMappingState(client.options.keyJump, true);
                }
                if (PestReturnManager.flightStopTicks >= 2) {
                    PestReturnManager.flightStopStage = 1;
                    PestReturnManager.flightStopTicks = 0;
                }
                break;
            case 1:
                if (client.options.keyJump != null) {
                    ClientUtils.setKeyMappingState(client.options.keyJump, false);
                }
                if (PestReturnManager.flightStopTicks >= 3) {
                    PestReturnManager.flightStopStage = 2;
                    PestReturnManager.flightStopTicks = 0;
                }
                break;
            case 2:
                if (client.options.keyJump != null) {
                    ClientUtils.setKeyMappingState(client.options.keyJump, true);
                }
                if (PestReturnManager.flightStopTicks >= 2) {
                    PestReturnManager.flightStopStage = 3;
                    PestReturnManager.flightStopTicks = 0;
                }
                break;
            case 3:
                if (client.options.keyJump != null) {
                    ClientUtils.setKeyMappingState(client.options.keyJump, false);
                }
                PestReturnManager.isStoppingFlight = false;
                break;
            default:
                break;
        }
    }

    private static void handleStashPickup(Minecraft client) {
        if (!SlateConfig.AUTO_STASH_MANAGER.get() || !isPickingUpStash || client.player == null) {
            return;
        }

        MacroState.State stashState = MacroStateManager.getCurrentState();
        if (client.screen != null
                || stashState == MacroState.State.VISITING
                || stashState == MacroState.State.CLEANING
                || stashState == MacroState.State.SPRAYING) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastStashPickupTime >= ConfigHelpers.getRandomizedDelay(
                SlateConfig.PICK_UP_STASH_DELAY_MIN.get(),
                SlateConfig.PICK_UP_STASH_DELAY_MAX.get())) {
            lastStashPickupTime = now;
            ClientUtils.sendCommand(client, "/pickupstash");
        }
    }

}

