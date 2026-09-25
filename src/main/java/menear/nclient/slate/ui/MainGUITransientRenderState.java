package menear.nclient.slate.ui;

import menear.nclient.slate.ui.settings.ColorSetting;
import menear.nclient.slate.ui.settings.DropdownSetting;

final class MainGUITransientRenderState {
    ColorSetting hoveredColor;
    boolean activeEditorBoundsSet;
    float filterBarAnimX;
    float filterBarAnimW;
    float filterBarTargetX;
    float filterBarTargetW;
    boolean filterBarInited;
    float maxScrollY;
    float searchMaxScrollY;
    float profileMaxScrollY;
    float subtabMaxScrollY;
    float sbMainTrackT;
    float sbMainTrackH;
    float sbMainThumbH;
    float sbSrchTrackT;
    float sbSrchTrackH;
    float sbSrchThumbH;
    float sbProfTrackT;
    float sbProfTrackH;
    float sbProfThumbH;
    float sbSubTrackT;
    float sbSubTrackH;
    float sbSubThumbH;
    DropdownSetting openDropdown;
    ColorSetting activeColor;
    boolean suppressNestedContentScissor;
    float scrollY;
    float targetScrollY;
}
