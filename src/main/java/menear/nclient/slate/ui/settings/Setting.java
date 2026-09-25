package menear.nclient.slate.ui.settings;

/**
 * Base interface for all configurable settings in the ClickGUI.
 * Implementations hold typed getter/setter references to SlateConfig fields.
 */
public interface Setting {
    String getName();
    default String getRawName() {
        return getName();
    }
    SettingType getType();
    boolean isVisible();
}
