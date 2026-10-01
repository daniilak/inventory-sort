package com.inventorysort.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class OverlayButton extends Button {
	public OverlayButton(int x, int y, String icon, String tooltip, OnPress onPress) {
		super(x, y, 10, 10, Component.literal(icon), onPress, DEFAULT_NARRATION);
		setTooltip(Tooltip.create(Component.literal(tooltip)));
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		int x = getX();
		int y = getY();
		int color = isHoveredOrFocused() ? 0xE0FFFFFF : 0xC0C6C6C6;
		int fill = isHoveredOrFocused() ? 0xAA202020 : 0x99000000;
		graphics.fill(x, y, x + width, y + height, fill);
		graphics.fill(x, y, x + width, y + 1, color);
		graphics.fill(x, y + height - 1, x + width, y + height, 0x80000000);
		graphics.fill(x, y, x + 1, y + height, color);
		graphics.fill(x + width - 1, y, x + width, y + height, 0x80000000);
		graphics.drawCenteredString(Minecraft.getInstance().font, getMessage(), x + width / 2, y + 1, isHoveredOrFocused() ? 0xFFFFFF : 0xE0E0E0);
	}
}
