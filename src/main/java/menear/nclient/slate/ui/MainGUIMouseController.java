package menear.nclient.slate.ui;

import net.minecraft.client.Minecraft;


final class MainGUIMouseController {
    private final MainGUI owner;

    MainGUIMouseController(MainGUI owner) {
        this.owner = owner;
    }

    boolean mouseClicked(double mouseX, double mouseY, int button) {
        float mx = owner.scaleMouseX(mouseX);
        float my = owner.scaleMouseY(mouseY);

        if (owner.handleKeybindMouseButtonCapture(button)) {
            return true;
        }

        if (button == 1) {
            owner.handleRightClickEvent(mx, my);
            return true;
        }

        if (button != 0) {
            return true;
        }

        if (owner.hasActiveColorOverlay()) {
            if (owner.handleColorPickerClickEvent(mx, my)) {
                return true;
            }
            owner.commitColor();
        }

        if (owner.hasInlineEditorFocus() && !owner.isPointInsideActiveEditorState(mx, my)) {
            owner.commitText();
        }

        if (owner.isOutsidePanel(mx, my)) {
            owner.handleOutsidePanelClick();
            return true;
        }

        if (owner.tryStartContentScrollbarDrag(mx, my)) {
            return true;
        }

        if (owner.tryStartSubtabScrollbarDrag(mx, my)) {
            return true;
        }

        if (owner.isSearchButtonHit(mx, my)) {
            owner.handleSearchButtonClick();
            return true;
        }

        if (owner.hasOpenDropdown()) {
            owner.handleDropdownClickEvent(mx, my);
            return true;
        }

        if (owner.isPointInsideSearchFieldState(mx, my)) {
            owner.handleSearchFieldClick(mx, my);
            return true;
        }

        owner.collapseSearchIfNeeded();

        if (owner.tryBeginPanelDrag(mx, my)) {
            return true;
        }

        if (owner.tryHandleSidebarNavigationClick(mx, my, Minecraft.getInstance())) {
            return true;
        }

        if (owner.isSidebarExpandedOverContent(mx, my)) {
            return true;
        }

        if (owner.runClickAreas(mx, my)) {
            return true;
        }

        owner.handleContentAreaClick(mx, my);
        return true;
    }

    boolean mouseDragged(double mouseX, double mouseY, int button, double dX, double dY) {
        float mx = owner.scaleMouseX(mouseX);
        float my = owner.scaleMouseY(mouseY);

        if (owner.dragPanel(mx, my)) {
            return true;
        }
        if (owner.dragSlider(mx)) {
            return true;
        }
        if (owner.dragRangeSlider(mx)) {
            return true;
        }
        if (owner.dragActiveColorOverlay(mx, my)) {
            return true;
        }
        if (owner.updateActiveTextSelection(mx)) {
            return true;
        }
        if (owner.tryBeginActiveTextSelection(button, mx, my)) {
            return true;
        }
        if (owner.dragScrollbar(my)) {
            return true;
        }
        return false;
    }

    boolean mouseReleased(double mouseX, double mouseY, int button) {
        owner.finishMouseInteractions();
        return false;
    }

    boolean mouseScrolled(double mx, double my, double sx, double sy) {
        return owner.handleMouseScroll(owner.scaleMouseX(mx), owner.scaleMouseY(my), (float) sy);
    }
}
