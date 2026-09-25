package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.modules.misc.AutoCarnivalManager;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class AutoCarnivalRegistryProvider extends AbstractMiningRegistryProvider {
    public AutoCarnivalRegistryProvider() {
        super(12);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        SettingGroup group = SettingGroup.alwaysOn(
                        "Miscellaneous",
                        "Shootout settings")
                .add(new SliderSetting("Offset", 0, 1000,
                        () -> (float) SlateConfig.AUTO_CARNIVAL_PING.get(),
                        value -> {
                            SlateConfig.AUTO_CARNIVAL_PING.set(Math.round(value));
                            SlateConfig.save();
                        })
                        .withDecimals(0));

        return MainGUIRegistry.toggleSubTab(
                "Auto Carnival (Shootout)",
                "Automatically clears shootout rounds and requeues them until tickets run out",
                () -> SlateConfig.AUTO_CARNIVAL_SHOOTOUT.get(),
                value -> AutoCarnivalManager.setEnabled(Minecraft.getInstance(), value),
                List.of(group));
    }
}
