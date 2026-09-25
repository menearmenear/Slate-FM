package menear.nclient.slate.renderer;

import menear.nclient.slate.ui.components.Component;
import menear.nclient.slate.util.SlateLang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for Minecraft {@link Screen}s that render their UI using NanoVG.
 *
 * <p>Subclasses override {@link #renderNVG(NVGRenderer)} to draw their content.
 * Components added via {@link #addComponent(Component)} receive input events
 * automatically.</p>
 */
public abstract class NVGScreen extends Screen {

    private final List<Component> components = new ArrayList<>();

    protected NVGScreen(String title) {
        super(net.minecraft.network.chat.Component.literal(SlateLang.localize(title)));
    }

    // -- Lifecycle -------------------------------------------------------------

    @Override
    protected void init() {
        components.clear();
        initNVG();
    }

    /** Called after init/resize. Add components via {@link #addComponent(Component)}. */
    protected void initNVG() {}

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        SlateRenderQueue.enqueue(this::renderQueued);
    }

    private void renderQueued() {
        if (net.minecraft.client.Minecraft.getInstance().screen != this) {
            return;
        }
        if (!NanoVGManager.isInitialized()) NanoVGManager.init();
        NanoVGManager.beginFrame(width, height);
        NVGRenderer nvg = NanoVGManager.getRenderer();
        try {
            renderNVG(nvg);
            for (Component c : components) {
                if (c.isVisible()) c.render(nvg);
            }
        } finally {
            NanoVGManager.endFrame();
        }
    }

    /** Override to draw custom NanoVG content before components. */
    protected void renderNVG(NVGRenderer nvg) {}

    // -- Input - MC 1.21.11 event-object API ----------------------------------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = components.size() - 1; i >= 0; i--) {
            Component c = components.get(i);
            if (c.isVisible() && c.isEnabled() && c.contains(mouseX, mouseY)) {
                if (c.mousePressed(mouseX, mouseY, button)) return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (int i = components.size() - 1; i >= 0; i--) {
            Component c = components.get(i);
            if (c.isVisible() && c.isEnabled()) {
                if (c.mouseReleased(mouseX, mouseY, button)) return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (int i = components.size() - 1; i >= 0; i--) {
            Component c = components.get(i);
            if (c.isVisible() && c.isEnabled()) {
                if (c.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        for (int i = components.size() - 1; i >= 0; i--) {
            Component c = components.get(i);
            if (c.isVisible() && c.isEnabled() && c.contains(mouseX, mouseY)) {
                if (c.mouseScrolled(mouseX, mouseY, 0.0, scrollY)) return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollY);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        for (int i = components.size() - 1; i >= 0; i--) {
            Component c = components.get(i);
            if (c.isVisible() && c.isEnabled()) {
                if (c.keyPressed(key, scan, mods)) return true;
            }
        }
        // Handle ESC explicitly; never delegate to super to avoid MC focus-navigation NPEs.
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) { onClose(); return true; }
        return false;
    }

    @Override
    public boolean charTyped(char ch, int mods) {
        for (int i = components.size() - 1; i >= 0; i--) {
            Component c = components.get(i);
            if (c.isVisible() && c.isEnabled()) {
                if (c.charTyped(ch, mods)) return true;
            }
        }
        return super.charTyped(ch, mods);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    /** Suppress MC's automatic background blur + menu backdrop - we draw our own. */
    @Override
    public void renderBackground(net.minecraft.client.gui.GuiGraphics graphics) {}

    /** Disable MC's focus-navigation system - we manage our own component focus. */
    @Override
    public java.util.List<? extends net.minecraft.client.gui.components.events.GuiEventListener> children() {
        return java.util.List.of();
    }


    // -- Component management --------------------------------------------------

    protected <T extends Component> T addComponent(T component) {
        components.add(component);
        return component;
    }

    protected void removeComponent(Component component) { components.remove(component); }
    protected List<Component> getComponents() { return components; }
}
