package menear.nclient.slate.proxy;

import menear.nclient.slate.config.SlateConfig;
import menear.nclient.slate.bootstrap.SlateBootstrapHooks;
import menear.nclient.slate.renderer.SlateBackground;
import menear.nclient.slate.util.SlateLang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class SlateProxyScreen extends Screen {
    private static final int PANEL_W = 520;
    private static final int LIST_W = 210;
    private static final int ROW_H = 24;
    private static final int FIELD_W = 210;
    private static final int FIELD_H = 20;

    private final Screen parent;

    private EditBox nameField;
    private EditBox addressField;
    private EditBox usernameField;
    private EditBox passwordField;
    private Checkbox enabledBox;
    private Button typeButton;
    private Button saveButton;
    private Button deleteButton;
    private SlateProxy.ProxyType type = SlateProxy.ProxyType.SOCKS5;
    private int editingIndex = -1;
    private String message = "";

    private int panelX;
    private int panelY;
    private int listX;
    private int listY;
    private int formX;
    private int formY;

    public SlateProxyScreen(Screen parent) {
        super(Component.literal(SlateLang.localize("Proxy Manager")));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_W) / 2;
        panelY = Math.max(28, (height - 250) / 2);
        listX = panelX;
        listY = panelY + 34;
        formX = panelX + LIST_W + 28;
        formY = panelY + 34;

        editingIndex = SlateProxyManager.selectedIndex();
        List<SlateProxy> proxies = SlateProxyManager.proxies();
        SlateProxy selected = editingIndex >= 0 && editingIndex < proxies.size() ? proxies.get(editingIndex) : null;

        enabledBox = new Checkbox(formX, formY, 20, 20,
                Component.literal(SlateLang.localize("Enable proxy")), SlateProxyManager.isEnabled());
        addRenderableWidget(enabledBox);

        nameField = field(formX, formY + 36, "Name", selected == null ? "" : selected.name());
        addressField = field(formX, formY + 76, "IP:PORT", selected == null ? "" : selected.address());
        usernameField = field(formX, formY + 116, "Username", selected == null ? "" : selected.username());
        passwordField = field(formX, formY + 156, "Password", selected == null ? "" : selected.password());
        type = selected == null ? SlateProxy.ProxyType.SOCKS5 : selected.type();

        typeButton = addRenderableWidget(Button.builder(Component.literal(SlateLang.localize(type.displayName())), button -> {
            type = type.next();
            button.setMessage(Component.literal(SlateLang.localize(type.displayName())));
        }).bounds(formX, formY + 196, 100, 20).build());

        saveButton = addRenderableWidget(Button.builder(Component.literal(SlateLang.localize("Save")), button -> saveCurrent())
                .bounds(formX + 110, formY + 196, 100, 20)
                .build());
        deleteButton = addRenderableWidget(Button.builder(Component.literal(SlateLang.localize("Delete")), button -> deleteCurrent())
                .bounds(formX + 220, formY + 196, 70, 20)
                .build());
        deleteButton.active = editingIndex >= 0;

        addRenderableWidget(Button.builder(Component.literal(SlateLang.localize("New")), button -> newProxy())
                .bounds(listX, panelY + 4, 64, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal(SlateLang.localize("Use")), button -> useCurrent())
                .bounds(listX + 72, panelY + 4, 64, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal(SlateLang.localize("Back")), button -> minecraft.setScreen(parent))
                .bounds(panelX + PANEL_W - 72, panelY + 4, 72, 20)
                .build());
    }

    private EditBox field(int x, int y, String hint, String value) {
        EditBox box = new EditBox(font, x, y, FIELD_W, FIELD_H, Component.literal(SlateLang.localize(hint)));
        box.setMaxLength(512);
        box.setValue(value);
        box.setHint(Component.literal(SlateLang.localize(hint)));
        addRenderableWidget(box);
        return box;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (SlateConfig.CUSTOM_UI_ENABLED.get()) {
            SlateBackground.INSTANCE.render(width, height, mouseX, mouseY);
        } else {
            super.renderBackground(graphics);
        }
        graphics.drawCenteredString(font, title, width / 2, panelY - 18, 0xFFFFFF);
        drawProxyListBackground(graphics, mouseX, mouseY);
        drawLabels(graphics);
        try (SlateBootstrapHooks.DisplayTransformScope ignored = SlateBootstrapHooks.suspendDisplayTransforms()) {
            super.render(graphics, mouseX, mouseY, partialTick);
        }
        drawProxyListText(graphics);
        if (!message.isBlank()) {
            graphics.drawCenteredString(font, message, width / 2, panelY + 266, 0xFFFF5555);
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
    }

    private void drawProxyListBackground(GuiGraphics graphics, int mouseX, int mouseY) {
        List<SlateProxy> proxies = SlateProxyManager.proxies();
        graphics.fill(listX, listY, listX + LIST_W, listY + 206, 0x88000000);
        if (proxies.isEmpty()) {
            return;
        }

        for (int i = 0; i < proxies.size(); i++) {
            int y = listY + i * ROW_H;
            if (y + ROW_H > listY + 206) {
                break;
            }
            boolean hovered = mouseX >= listX && mouseX <= listX + LIST_W && mouseY >= y && mouseY <= y + ROW_H;
            int color = i == editingIndex ? 0xAA3A3A3A : hovered ? 0x663A3A3A : 0x33000000;
            graphics.fill(listX + 2, y + 2, listX + LIST_W - 2, y + ROW_H - 2, color);
        }
    }

    private void drawProxyListText(GuiGraphics graphics) {
        List<SlateProxy> proxies = SlateProxyManager.proxies();
        if (proxies.isEmpty()) {
            graphics.drawCenteredString(font, SlateLang.localize("No proxies saved"), listX + LIST_W / 2, listY + 92, 0xFFAAAAAA);
            return;
        }

        int selected = SlateProxyManager.selectedIndex();
        for (int i = 0; i < proxies.size(); i++) {
            int y = listY + i * ROW_H;
            if (y + ROW_H > listY + 206) {
                break;
            }
            String prefix = i == selected && SlateProxyManager.isEnabled() ? "* " : "";
            graphics.drawString(font, prefix + proxies.get(i).displayName(), listX + 8, y + 5, 0xFFFFFFFF, true);
            graphics.drawString(font, proxies.get(i).shortStatus(), listX + 8, y + 15, 0xFFB0B0B0, true);
        }
    }

    private void drawLabels(GuiGraphics graphics) {
        graphics.drawString(font, SlateLang.localize("Name"), formX, formY + 25, 0xAAAAAA, false);
        graphics.drawString(font, SlateLang.localize("IP:PORT"), formX, formY + 65, 0xAAAAAA, false);
        graphics.drawString(font, SlateLang.localize("Username"), formX, formY + 105, 0xAAAAAA, false);
        graphics.drawString(font, SlateLang.localize("Password"), formX, formY + 145, 0xAAAAAA, false);
        graphics.drawString(font, SlateLang.localize("Type"), formX, formY + 185, 0xAAAAAA, false);
        graphics.drawString(font, SlateProxyManager.selectedStatus(), listX, listY + 214, 0xAAAAAA, false);
        graphics.drawString(font, SlateLang.localize("Last used: ") + SlateProxyManager.lastUsedStatus(), listX, listY + 226, 0x777777, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && SlateConfig.CUSTOM_UI_ENABLED.get()) {
            SlateBackground.INSTANCE.addRipple((float) mouseX, (float) mouseY);
        }

        if (button == 0 && mouseX >= listX && mouseX <= listX + LIST_W
                && mouseY >= listY && mouseY <= listY + 206) {
            int index = (int) ((mouseY - listY) / ROW_H);
            if (index >= 0 && index < SlateProxyManager.proxies().size()) {
                loadProxy(index);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void loadProxy(int index) {
        List<SlateProxy> proxies = SlateProxyManager.proxies();
        if (index < 0 || index >= proxies.size()) {
            return;
        }
        SlateProxy proxy = proxies.get(index);
        editingIndex = index;
        nameField.setValue(proxy.name());
        addressField.setValue(proxy.address());
        usernameField.setValue(proxy.username());
        passwordField.setValue(proxy.password());
        type = proxy.type();
            typeButton.setMessage(Component.literal(SlateLang.localize(type.displayName())));
        deleteButton.active = true;
        message = "";
    }

    private void newProxy() {
        editingIndex = -1;
        nameField.setValue("");
        addressField.setValue("");
        usernameField.setValue("");
        passwordField.setValue("");
        type = SlateProxy.ProxyType.SOCKS5;
        typeButton.setMessage(Component.literal(SlateLang.localize(type.displayName())));
        deleteButton.active = false;
        message = "";
    }

    private void saveCurrent() {
        SlateProxy proxy = currentFormProxy();
        if (!proxy.isValid()) {
            message = SlateLang.localize("Proxy address must be host:port.");
            addressField.setFocused(true);
            return;
        }

        if (editingIndex >= 0) {
            SlateProxyManager.update(editingIndex, proxy);
        } else {
            SlateProxyManager.add(proxy);
            editingIndex = SlateProxyManager.selectedIndex();
            deleteButton.active = true;
        }
        SlateProxyManager.setEnabled(enabledBox.selected());
        SlateProxyManager.refreshProxyStatusButtons(parent);
        message = "";
    }

    private void useCurrent() {
        saveCurrent();
        if (message.isBlank() && editingIndex >= 0) {
            SlateProxyManager.select(editingIndex);
            SlateProxyManager.setEnabled(true);
            SlateProxyManager.refreshProxyStatusButtons(parent);
            rebuildWidgets();
        }
    }

    private void deleteCurrent() {
        if (editingIndex < 0) {
            return;
        }
        SlateProxyManager.remove(editingIndex);
        SlateProxyManager.refreshProxyStatusButtons(parent);
        newProxy();
    }

    private SlateProxy currentFormProxy() {
        return new SlateProxy(
                nameField.getValue(),
                addressField.getValue(),
                type,
                usernameField.getValue(),
                passwordField.getValue());
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

