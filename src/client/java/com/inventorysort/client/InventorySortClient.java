package com.inventorysort.client;

import com.inventorysort.MenuSlots;
import com.inventorysort.client.mixin.AbstractContainerScreenAccessor;
import com.inventorysort.net.SortPayload;
import com.inventorysort.net.TransferPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public class InventorySortClient implements ClientModInitializer {
	private static final int SIZE = 10;
	private static final int GAP = 1;
	private static final int TOOLBAR_COUNT = 4;

	@Override
	public void onInitializeClient() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!(screen instanceof AbstractContainerScreen<?> container) || screen instanceof CreativeModeInventoryScreen) {
				return;
			}
			if (client.player == null) {
				return;
			}
			if (!ClientPlayNetworking.canSend(SortPayload.TYPE)
					|| !ClientPlayNetworking.canSend(TransferPayload.TYPE)) {
				return;
			}

			AbstractContainerMenu menu = container.getMenu();
			MenuSlots slots = MenuSlots.of(menu, client.player);
			if (!slots.canSortPlayer() && !slots.canTransfer()) {
				return;
			}

			AbstractContainerScreenAccessor access = (AbstractContainerScreenAccessor) container;
			int left = access.inventorysort$leftPos();
			int top = access.inventorysort$topPos();

			if (slots.canTransfer()) {
				int[] pair = toolbarPos(container, access, TOOLBAR_COUNT);
				add(container, pair[0], pair[1], "H", "Сорт по строкам", () -> ClientPlayNetworking.send(new SortPayload(true, false)));
				add(container, pair[0] + SIZE + GAP, pair[1], "V", "Сорт по столбцам", () -> ClientPlayNetworking.send(new SortPayload(true, true)));
				add(container, pair[0] + 2 * (SIZE + GAP), pair[1], "↓", "Сложить без хотбара", () -> ClientPlayNetworking.send(new TransferPayload(true)));
				add(container, pair[0] + 3 * (SIZE + GAP), pair[1], "↑", "Забрать всё", () -> ClientPlayNetworking.send(new TransferPayload(false)));
			}

			if (slots.canSortPlayer()) {
				int[] pair = overlayPos(menu, slots.playerMain, left, top, 2);
				add(container, pair[0], pair[1], "H", "Инвентарь по строкам", () -> ClientPlayNetworking.send(new SortPayload(false, false)));
				add(container, pair[0] + SIZE + GAP, pair[1], "V", "Инвентарь по столбцам", () -> ClientPlayNetworking.send(new SortPayload(false, true)));
			}
		});
	}

	private static int[] overlayPos(AbstractContainerMenu menu, int[] region, int left, int top, int count) {
		Slot first = menu.getSlot(region[0]);
		int maxX = first.x;
		int minY = first.y;
		for (int index : region) {
			Slot slot = menu.getSlot(index);
			maxX = Math.max(maxX, slot.x);
			minY = Math.min(minY, slot.y);
		}

		int width = count * SIZE + (count - 1) * GAP;
		int x = left + maxX + 18 - width;
		int y = top + minY - SIZE - 1;
		return new int[] {x, y};
	}

	private static int[] toolbarPos(AbstractContainerScreen<?> screen, AbstractContainerScreenAccessor access, int count) {
		int width = access.inventorysort$imageWidth();
		int totalWidth = count * SIZE + (count - 1) * GAP;
		int left = access.inventorysort$leftPos();
		int top = access.inventorysort$topPos();
		int x = left + width - totalWidth - 4;
		int titleWidth = Minecraft.getInstance().font.width(screen.getTitle());
		int titleEnd = left + (width + titleWidth) / 2;
		if (x < titleEnd + 3) {
			x = left + 4;
		}
		return new int[] {x, top + 3};
	}

	private static void add(AbstractContainerScreen<?> screen, int x, int y, String icon, String tooltip, Runnable action) {
		Screens.getButtons(screen).add(new OverlayButton(x, y, icon, tooltip, button -> action.run()));
	}
}
