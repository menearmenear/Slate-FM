package menear.nclient.slate.ui;

import org.lwjgl.glfw.GLFW;

final class MainGUIKeyboardController {
    private final MainGUI owner;

    MainGUIKeyboardController(MainGUI owner) {
        this.owner = owner;
    }

    boolean keyPressed(int key, int scanCode, int modifiers) {
        if (owner.handleKeybindCapture(key, scanCode, modifiers)) {
            return true;
        }

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            return owner.handleEscapeKey();
        }

        boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;

        if (ctrl && (key == GLFW.GLFW_KEY_A || key == GLFW.GLFW_KEY_C || key == GLFW.GLFW_KEY_V)
                && owner.handleInlineControlShortcut(key)) {
            return true;
        }

        if (owner.handleSearchEditorKey(key, ctrl, shift)) {
            return true;
        }

        if (owner.handleProfileNameKey(key)) {
            return true;
        }

        if (owner.handleInlineEditorKey(key, ctrl, shift)) {
            return true;
        }

        return owner.handleOverlayHexKey(key);
    }

    boolean charTyped(char ch, int modifiers) {
        if (owner.isAnyKeybindCaptureActive()) {
            return true;
        }
        if (owner.handleInlineCharTyped(ch)) {
            return true;
        }
        return owner.handleOverlayHexChar(ch);
    }
}
