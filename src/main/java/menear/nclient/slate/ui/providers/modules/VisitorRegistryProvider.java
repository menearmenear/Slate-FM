package menear.nclient.slate.ui;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.ui.settings.ListSetting;
import menear.nclient.slate.ui.settings.ModulesTab;
import menear.nclient.slate.ui.settings.SettingGroup;
import menear.nclient.slate.ui.settings.SliderSetting;
import menear.nclient.slate.ui.settings.TextSetting;
import menear.nclient.slate.ui.settings.ToggleSetting;

import java.util.List;

public final class VisitorRegistryProvider extends AbstractModulesRegistryProvider {
    public VisitorRegistryProvider() {
        super(4);
    }

    @Override
    protected ModulesTab.SubTab createSubTab() {
        SettingGroup group = SettingGroup.of(
                "Auto Visitor Settings",
                "Automatically fulfills visitors requests",
                () -> SlateConfig.AUTO_VISITOR.get(),
                v -> {
                    SlateConfig.AUTO_VISITOR.set(v);
                    SlateConfig.save();
                });
        group.add(new SliderSetting("Visitor Threshold", 1, 5,
                () -> (float) SlateConfig.VISITOR_THRESHOLD.get(),
                v -> {
                    SlateConfig.VISITOR_THRESHOLD.set(Math.round(v));
                    SlateConfig.save();
                })
                .withDecimals(0));
        group.add(new SliderSetting("Max Visitor Purchase", 0.0f, 20.0f,
                () -> SlateConfig.VISITOR_MAX_PURCHASE_LIMIT.get() / 1_000_000.0f,
                v -> {
                    SlateConfig.VISITOR_MAX_PURCHASE_LIMIT.set(Math.round(v * 1_000_000.0f));
                    SlateConfig.save();
                })
                .withDecimals(1).withSuffix("m"));
        group.add(new ListSetting("Visitor Ignored", "Add visitor name",
                () -> SlateConfig.VISITOR_ignore.get(),
                v -> {
                    SlateConfig.VISITOR_ignore.set(v);
                    SlateConfig.save();
                }));
        group.add(new ListSetting("Visitor Reject", "Add visitor name",
                () -> SlateConfig.VISITOR_REJECT.get(),
                v -> {
                    SlateConfig.VISITOR_REJECT.set(v);
                    SlateConfig.save();
                }));
        group.add(new ToggleSetting("Equip Custom Item",
                () -> SlateConfig.EQUIP_VISITOR_CUSTOM_ITEM.get(),
                v -> {
                    SlateConfig.EQUIP_VISITOR_CUSTOM_ITEM.set(v);
                    SlateConfig.save();
                }));
        group.add(new TextSetting("Custom Item", "e.g. Blessed Melon Dicer",
                () -> SlateConfig.VISITOR_CUSTOM_ITEM.get(),
                v -> {
                    SlateConfig.VISITOR_CUSTOM_ITEM.set(v);
                    SlateConfig.save();
                })
                .visibleWhen(() -> SlateConfig.EQUIP_VISITOR_CUSTOM_ITEM.get()));
        group.add(new ToggleSetting("Disable Compactors during Visitors",
                () -> SlateConfig.DISABLE_COMPACTORS_DURING_VISITORS.get(),
                v -> {
                    SlateConfig.DISABLE_COMPACTORS_DURING_VISITORS.set(v);
                    SlateConfig.save();
                }));
        group.add(new ToggleSetting("Disable during Jacob's Contests",
                () -> SlateConfig.DISABLE_VISITORS_DURING_JACOBS_CONTEST.get(),
                v -> {
                    SlateConfig.DISABLE_VISITORS_DURING_JACOBS_CONTEST.set(v);
                    SlateConfig.save();
                }));
        group.add(FarmingSettingsFactory.visitorFovRangeSetting());
        return MainGUIRegistry.toggleSubTab(
                "Auto Visitor",
                "Automatically interacts with visitors and fulfills their requests",
                () -> SlateConfig.AUTO_VISITOR.get(),
                v -> {
                    SlateConfig.AUTO_VISITOR.set(v);
                    SlateConfig.save();
                },
                List.of(group));
    }
}
