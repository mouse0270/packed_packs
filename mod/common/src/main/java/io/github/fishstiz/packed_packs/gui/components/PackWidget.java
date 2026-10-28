package io.github.fishstiz.packed_packs.gui.components;

import io.github.fishstiz.fidgetz.v0.gui.components.FZText;
import io.github.fishstiz.fidgetz.v0.utils.GuiGraphicsUtils;
import io.github.fishstiz.packed_packs.util.FolderPackIds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.util.CommonColors;

import java.util.function.Consumer;

class PackWidget implements Renderable, LayoutElement {
    static final int ICON_SIZE = 32;
    private static final Component INCOMPATIBLE_TITLE = Component.translatable("pack.incompatible");
    private static final int DESCRIPTION_LINES = 2;
    private static final int SPACING = 2;
    private static final int BADGE_SIZE = 11;
    private static final String[] ROOT_ICON = {
            "...#...", "..###..", ".#####.", "#######", ".#####.", ".##.##.", ".##.##."
    };
    private static final String[] RELATIVE_ICON = {
            ".###...", "#######", "#.....#", "#.....#", "#.....#", "#.....#", "#######"
    };
    private final Font font;
    private final PackList.Entry entry;
    private final FZText nameWidget;
    private final MultiLineTextWidget descriptionWidget;
    private final boolean hasLocationBadge;
    private final boolean rootReference;
    private final Component locationTooltip;
    private int cachedBodyWidth;
    private boolean warningShown;
    private int x;
    private int y;
    private int width;
    private int height;

    PackWidget(PackList.Entry entry, int height) {
        this.entry = entry;
        this.font = Minecraft.getInstance().font;
        this.nameWidget = FZText.builder(entry.pack.title()).build();
        this.height = height;
        this.descriptionWidget = new MultiLineTextWidget(getExtendedDescription(), font);
        String reference = FolderPackIds.serialize(entry.pack.id(), entry.pack.parentId());
        this.rootReference = FolderPackIds.isRoot(reference);
        this.hasLocationBadge = entry.pack.path() != null && entry.pack.parentId() != null
                && (rootReference || reference.startsWith("relative/"));
        this.locationTooltip = hasLocationBadge
                ? Component.translatable(rootReference ? "packed_packs.location.root" : "packed_packs.location.relative")
                    .append("\n").append(Component.literal(entry.pack.path().toAbsolutePath().normalize().toString())
                            .withStyle(Style.EMPTY.withColor(CommonColors.GRAY)))
                : Component.empty();
        repositionElements();
    }

    private Component getExtendedDescription() {
        return ComponentUtils.mergeStyles(entry.pack.decoratedDescription().copy(), Style.EMPTY.withColor(CommonColors.GRAY));
    }

    private void repositionHorizontal() {
        int bodyX = x + ICON_SIZE + SPACING * 2;
        int bodyWidth = (x + width) - SPACING * 2 - bodyX;

        nameWidget.setX(bodyX);
        nameWidget.setWidth(bodyWidth);

        descriptionWidget.setMaxRows(DESCRIPTION_LINES);
        descriptionWidget.setCentered(false);
        descriptionWidget.setX(nameWidget.getX());
        descriptionWidget.setMaxWidth(bodyWidth);

        this.cachedBodyWidth = bodyWidth;
    }

    private void repositionVertical() {
        int lineHeight = font.lineHeight;
        int totalContentHeight = lineHeight + SPACING + (lineHeight * DESCRIPTION_LINES);
        int startY = y + (height - totalContentHeight) / 2;
        nameWidget.setY(startY);
        this.descriptionWidget.setY(startY + lineHeight + SPACING);
    }

    private void repositionElements() {
        repositionHorizontal();
        repositionVertical();
    }

    public void setWidth(int width) {
        this.width = width;
        repositionElements();
    }

    public void setHeight(int height) {
        this.height = height;
        repositionVertical();
    }

    @Override
    public void setX(int x) {
        this.x = x;
        repositionHorizontal();
    }

    @Override
    public void setY(int y) {
        this.y = y;
        repositionVertical();
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> widgetVisitor) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        GuiGraphicsUtils.texture(graphics, entry.getPackIcon(), x + SPACING, y, ICON_SIZE, ICON_SIZE);
        nameWidget.render(graphics, mouseX, mouseY, partialTick);
        this.descriptionWidget.render(graphics, mouseX, mouseY, partialTick);
    }

    boolean isLocationBadgeHovered(double mouseX, double mouseY) {
        int left = x + SPACING;
        int top = y + ICON_SIZE - BADGE_SIZE;
        return hasLocationBadge && entry.isHovered()
                && mouseX >= left && mouseX < left + BADGE_SIZE
                && mouseY >= top && mouseY < top + BADGE_SIZE;
    }

    // Draw after the selection/action overlays so the badge stays legible.
    void renderLocationBadge(GuiGraphics graphics, int mouseX, int mouseY, boolean allowTooltip) {
        if (!hasLocationBadge) return;
        int left = x + SPACING;
        int top = y + ICON_SIZE - BADGE_SIZE;
        int color = rootReference ? 0xFF8DCEFF : 0xFFFFD580;
        graphics.fill(left, top, left + BADGE_SIZE, top + BADGE_SIZE, 0xF0181C24);
        graphics.renderOutline(left, top, BADGE_SIZE, BADGE_SIZE, color);
        String[] pixels = rootReference ? ROOT_ICON : RELATIVE_ICON;
        for (int row = 0; row < pixels.length; row++) {
            for (int col = 0; col < pixels[row].length(); col++) {
                if (pixels[row].charAt(col) == '#') {
                    graphics.fill(left + 2 + col, top + 2 + row, left + 3 + col, top + 3 + row, color);
                }
            }
        }
        if (allowTooltip && isLocationBadgeHovered(mouseX, mouseY) && Minecraft.getInstance().screen != null) {
            Minecraft.getInstance().screen.setTooltipForNextRenderPass(locationTooltip);
        }
    }

    void checkCompatibility(boolean showWarning) {
        if (showWarning) {
            if (!this.warningShown && !entry.pack.compatibility().isCompatible() && !entry.isIncompatibleWarningsHidden()) {
                nameWidget.setMessage(INCOMPATIBLE_TITLE);
                this.descriptionWidget.setMessage(entry.pack.compatibility().getDescription());
                this.warningShown = true;
            }
        } else if (this.warningShown) {
            nameWidget.setMessage(entry.pack.title());
            nameWidget.setWidth(this.cachedBodyWidth);
            this.descriptionWidget.setMessage(getExtendedDescription());
            this.warningShown = false;
        }
    }
}
