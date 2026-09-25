package menear.nclient.slate.bootstrap;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class SlateKeybindRegistry {
    private static KeyMapping macroToggleKey;
    private static KeyMapping clickGuiKey;
    private static KeyMapping nvgDemoKey;
    private static KeyMapping freecamKey;
    private static KeyMapping freecamTeleportToPlayerKey;
    private static KeyMapping freelookKey;
    private static KeyMapping pipKey;
    private static KeyMapping ungrabMouseKey;
    private static boolean registered;

    private SlateKeybindRegistry() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        String category = "key.categories.slate";
        try {
            macroToggleKey = KeyBindingHelper
                    .registerKeyBinding(new KeyMapping("key.slate.start_script", GLFW.GLFW_KEY_K, category));
            clickGuiKey = KeyBindingHelper
                    .registerKeyBinding(new KeyMapping("key.slate.clickgui", GLFW.GLFW_KEY_INSERT, category));
            nvgDemoKey = KeyBindingHelper
                    .registerKeyBinding(new KeyMapping("Open NVG Demo", GLFW.GLFW_KEY_HOME, category));
            freecamKey = KeyBindingHelper
                    .registerKeyBinding(new KeyMapping("key.slate.freecam", GLFW.GLFW_KEY_F6, category));
            freecamTeleportToPlayerKey = KeyBindingHelper
                    .registerKeyBinding(new KeyMapping("key.slate.freecam_teleport_to_player", GLFW.GLFW_KEY_F7, category));
            freelookKey = KeyBindingHelper
                    .registerKeyBinding(new KeyMapping("key.slate.freelook", GLFW.GLFW_KEY_LEFT_ALT, category));
            pipKey = KeyBindingHelper
                    .registerKeyBinding(new KeyMapping("key.slate.pip", GLFW.GLFW_KEY_P, category));
            ungrabMouseKey = KeyBindingHelper
                    .registerKeyBinding(new KeyMapping("key.slate.ungrab_mouse", GLFW.GLFW_KEY_U, category));
        } catch (IllegalStateException ex) {
            // External feature jars can initialize after options are already built; reuse existing mappings if present.
            macroToggleKey = resolveExistingOrDetached("key.slate.start_script", GLFW.GLFW_KEY_K, category);
            clickGuiKey = resolveExistingOrDetached("key.slate.clickgui", GLFW.GLFW_KEY_INSERT, category);
            nvgDemoKey = resolveExistingOrDetached("Open NVG Demo", GLFW.GLFW_KEY_HOME, category);
            freecamKey = resolveExistingOrDetached("key.slate.freecam", GLFW.GLFW_KEY_F6, category);
            freecamTeleportToPlayerKey = resolveExistingOrDetached("key.slate.freecam_teleport_to_player", GLFW.GLFW_KEY_F7, category);
            freelookKey = resolveExistingOrDetached("key.slate.freelook", GLFW.GLFW_KEY_LEFT_ALT, category);
            pipKey = resolveExistingOrDetached("key.slate.pip", GLFW.GLFW_KEY_P, category);
            ungrabMouseKey = resolveExistingOrDetached("key.slate.ungrab_mouse", GLFW.GLFW_KEY_U, category);
        }
        registered = true;
    }

    private static KeyMapping resolveExistingOrDetached(String translationKey, int defaultKey, String category) {
        KeyMapping existing = findExistingMapping(translationKey);
        if (existing != null) {
            return existing;
        }
        return new KeyMapping(translationKey, defaultKey, category);
    }

    private static KeyMapping findExistingMapping(String translationKey) {
        KeyMapping[] existingMappings = getRegisteredMappingsFromOptions();
        if (existingMappings == null) {
            return null;
        }

        for (KeyMapping mapping : existingMappings) {
            if (mapping != null && translationKey.equals(getMappingTranslationKey(mapping))) {
                return mapping;
            }
        }
        return null;
    }

    private static KeyMapping[] getRegisteredMappingsFromOptions() {
        Minecraft client = Minecraft.getInstance();
        Object options = client.options;
        for (Field field : options.getClass().getDeclaredFields()) {
            if (!field.getType().isArray() || !KeyMapping.class.isAssignableFrom(field.getType().getComponentType())) {
                continue;
            }

            try {
                field.setAccessible(true);
                Object value = field.get(options);
                if (value == null) {
                    continue;
                }

                int length = Array.getLength(value);
                KeyMapping[] mappings = new KeyMapping[length];
                for (int i = 0; i < length; i++) {
                    mappings[i] = (KeyMapping) Array.get(value, i);
                }
                return mappings;
            } catch (ReflectiveOperationException ignored) {
            }
        }

        return null;
    }

    private static String getMappingTranslationKey(KeyMapping mapping) {
        try {
            Method getNameMethod = KeyMapping.class.getMethod("getName");
            Object name = getNameMethod.invoke(mapping);
            return name instanceof String ? (String) name : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    public static KeyMapping getMacroToggleKey() {
        register();
        return macroToggleKey;
    }

    public static KeyMapping getClickGuiKey() {
        register();
        return clickGuiKey;
    }

    public static KeyMapping getNvgDemoKey() {
        register();
        return nvgDemoKey;
    }

    public static KeyMapping getFreecamKey() {
        register();
        return freecamKey;
    }

    public static KeyMapping getFreecamTeleportToPlayerKey() {
        register();
        return freecamTeleportToPlayerKey;
    }

    public static KeyMapping getFreelookKey() {
        register();
        return freelookKey;
    }

    public static KeyMapping getPipKey() {
        register();
        return pipKey;
    }

    public static KeyMapping getUngrabMouseKey() {
        register();
        return ungrabMouseKey;
    }

    public static java.util.List<RegisteredKeybind> getRegisteredKeybinds() {
        register();
        return java.util.List.of(
                new RegisteredKeybind("Toggle Macro", "Starts or stops the active farming macro", getMacroToggleKey()),
                new RegisteredKeybind("Open GUI", "Opens the Slate sidebar menu", getClickGuiKey()),
                new RegisteredKeybind("Open NVG Demo", "Opens the NanoVG demo screen", getNvgDemoKey()),
                new RegisteredKeybind("Toggle Freecam", "Detaches or restores the camera (requires the Freecam module on)", getFreecamKey()),
                new RegisteredKeybind("Freecam Teleport To Player", "Snaps the observer back to the real player", getFreecamTeleportToPlayerKey()),
                new RegisteredKeybind("Freelook (hold)", "Orbit the camera while the player keeps facing forward", getFreelookKey()),
                new RegisteredKeybind("Toggle PiP", "Opens or closes the picture-in-picture window", getPipKey()),
                new RegisteredKeybind("Toggle Ungrab Mouse", "Releases or restores the mouse cursor", getUngrabMouseKey())
        );
    }

    public record RegisteredKeybind(String name, String description, KeyMapping mapping) {
    }
}
