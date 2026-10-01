package com.inventorysort;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class TransferHandler {
	private TransferHandler() {
	}

	public static void handle(ServerPlayer player, boolean toChest) {
		if (player.isSpectator()) {
			return;
		}

		AbstractContainerMenu menu = player.containerMenu;
		if (menu == player.inventoryMenu) {
			return;
		}

		MenuSlots slots = MenuSlots.of(menu, player);
		if (!slots.canTransfer()) {
			return;
		}

		int[] from = toChest ? slots.playerMain : slots.container;
		int[] to = toChest ? slots.container : slots.playerStorage;
		transfer(menu, from, to, player);
		menu.broadcastChanges();
	}

	static void transfer(AbstractContainerMenu menu, int[] from, int[] to, ServerPlayer player) {
		for (int fromIndex : from) {
			if (fromIndex < 0 || fromIndex >= menu.slots.size()) {
				continue;
			}

			Slot fromSlot = menu.getSlot(fromIndex);
			if (!MenuSlots.movable(fromSlot, player) || fromSlot.getItem().isEmpty()) {
				continue;
			}

			ItemStack remaining = fromSlot.getItem().copy();
			fromSlot.set(ItemStack.EMPTY);
			fromSlot.setChanged();

			remaining = mergeExisting(menu, to, remaining, player);
			remaining = fillEmpty(menu, to, remaining, player);

			if (!remaining.isEmpty()) {
				ItemStack leftover = fromSlot.getItem();
				if (leftover.isEmpty()) {
					fromSlot.set(remaining);
				} else if (ItemStack.isSameItemSameComponents(leftover, remaining)) {
					leftover.grow(remaining.getCount());
					fromSlot.set(leftover);
				} else if (!player.addItem(remaining)) {
					player.drop(remaining, false);
				}
				fromSlot.setChanged();
			}
		}
	}

	private static ItemStack mergeExisting(AbstractContainerMenu menu, int[] to, ItemStack stack, ServerPlayer player) {
		if (stack.isEmpty()) {
			return stack;
		}

		for (int toIndex : to) {
			if (stack.isEmpty()) {
				return ItemStack.EMPTY;
			}
			if (toIndex < 0 || toIndex >= menu.slots.size()) {
				continue;
			}

			Slot toSlot = menu.getSlot(toIndex);
			ItemStack dest = toSlot.getItem();
			if (dest.isEmpty() || !ItemStack.isSameItemSameComponents(dest, stack) || !toSlot.mayPlace(stack)) {
				continue;
			}

			int max = Math.min(stack.getMaxStackSize(), toSlot.getMaxStackSize());
			int space = max - dest.getCount();
			if (space <= 0) {
				continue;
			}

			int move = Math.min(space, stack.getCount());
			dest.grow(move);
			stack.shrink(move);
			toSlot.set(dest);
			toSlot.setChanged();
		}

		return stack;
	}

	private static ItemStack fillEmpty(AbstractContainerMenu menu, int[] to, ItemStack stack, ServerPlayer player) {
		if (stack.isEmpty()) {
			return stack;
		}

		for (int toIndex : to) {
			if (stack.isEmpty()) {
				return ItemStack.EMPTY;
			}
			if (toIndex < 0 || toIndex >= menu.slots.size()) {
				continue;
			}

			Slot toSlot = menu.getSlot(toIndex);
			if (!toSlot.getItem().isEmpty() || !toSlot.mayPlace(stack)) {
				continue;
			}

			int max = Math.min(stack.getMaxStackSize(), toSlot.getMaxStackSize());
			ItemStack placed = stack.split(Math.min(max, stack.getCount()));
			toSlot.set(placed);
			toSlot.setChanged();
		}

		return stack;
	}
}
