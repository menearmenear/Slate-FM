package menear.nclient.slate.ui;

import menear.nclient.slate.ui.settings.ModulesTab;

import java.util.List;

record MainGUIModuleSection(String name, List<ModulesTab.SubTab> subtabs) {
}
