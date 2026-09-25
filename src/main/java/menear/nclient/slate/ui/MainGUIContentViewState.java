package menear.nclient.slate.ui;

import menear.nclient.slate.ui.settings.ModulesTab;

record MainGUIContentViewState(int activeMain, int activeSubtab, int activeFilter,
                               ModulesTab.SubTab activeSubTab, int activeCategoryIdx,
                               String searchQuery) {
}
