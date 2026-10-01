package com.inventorysort;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SortHandler {
	private SortHandler() {
	}

	public static void handle(ServerPlayer player, boolean container, boolean vertical) {
		if (player.isSpectator()) {
			return;
		}

		AbstractContainerMenu menu = player.containerMenu;
		MenuSlots slots = MenuSlots.of(menu, player);
		int[] region = container ? slots.container : slots.playerMain;
		if (region.length == 0) {
			return;
		}

		if (!sort(menu, region, vertical, player)) {
			player.displayClientMessage(
					Component.literal("Inventory Sort: сортировка отменена — слоты контейнера несовместимы."),
					true
			);
			return;
		}
		menu.broadcastChanges();
	}

	/**
	 * Builds the complete placement before mutating the menu. If any stack
	 * cannot be placed safely, the operation is cancelled without a partial
	 * move or dropping an item.
	 */
	static boolean sort(AbstractContainerMenu menu, int[] region, boolean vertical, ServerPlayer player) {
		List<Integer> unlocked = new ArrayList<>();
		List<ItemStack> items = new ArrayList<>();

		for (int slotIndex : region) {
			if (slotIndex < 0 || slotIndex >= menu.slots.size()) {
				continue;
			}

			Slot slot = menu.getSlot(slotIndex);
			if (!MenuSlots.movable(slot, player)) {
				continue;
			}

			ItemStack stack = slot.getItem();
			if (!stack.isEmpty()) {
				items.add(stack.copy());
			}
			unlocked.add(slotIndex);
		}

		List<ItemStack> remaining = compact(items);
		remaining.sort(byId());
		unlocked.sort(placement(menu, vertical));

		ItemStack[] planned = new ItemStack[menu.slots.size()];
		for (int slotIndex : unlocked) {
			Slot slot = menu.getSlot(slotIndex);
			int itemIndex = firstPlaceable(remaining, slot);
			if (itemIndex < 0) {
				continue;
			}

			ItemStack source = remaining.get(itemIndex);
			int max = Math.min(source.getMaxStackSize(), slot.getMaxStackSize());
			if (max <= 0) {
				continue;
			}

			int amount = Math.min(source.getCount(), max);
			ItemStack placed = source.copy();
			placed.setCount(amount);
			source.shrink(amount);
			if (source.isEmpty()) {
				remaining.remove(itemIndex);
			}
			planned[slotIndex] = placed;
		}

		if (!remaining.isEmpty()) {
			// Specialized slots can reject an otherwise valid item. Do not make
			// a partial change and do not fall back to player.drop().
			return false;
		}

		for (int slotIndex : unlocked) {
			Slot slot = menu.getSlot(slotIndex);
			ItemStack placed = planned[slotIndex];
			slot.set(placed == null ? ItemStack.EMPTY : placed);
			slot.setChanged();
		}
		return true;
	}

	private static int firstPlaceable(List<ItemStack> stacks, Slot slot) {
		for (int i = 0; i < stacks.size(); i++) {
			if (slot.mayPlace(stacks.get(i))) {
				return i;
			}
		}
		return -1;
	}

	private static List<ItemStack> compact(List<ItemStack> items) {
		List<ItemStack> compacted = new ArrayList<>();
		for (ItemStack incoming : items) {
			int leftover = incoming.getCount();
			for (ItemStack existing : compacted) {
				if (leftover <= 0) {
					break;
				}
				if (!ItemStack.isSameItemSameComponents(existing, incoming)) {
					continue;
				}
				int space = existing.getMaxStackSize() - existing.getCount();
				if (space <= 0) {
					continue;
				}
				int move = Math.min(space, leftover);
				existing.grow(move);
				leftover -= move;
			}
			if (leftover > 0) {
				ItemStack extra = incoming.copy();
				extra.setCount(leftover);
				compacted.add(extra);
			}
		}
		return compacted;
	}

	private static Comparator<ItemStack> byId() {
		return Comparator
				.comparing((ItemStack stack) -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())
				.thenComparing(stack -> stack.getComponents().toString())
				.thenComparing(ItemStack::getCount, Comparator.reverseOrder());
	}

	private static Comparator<Integer> placement(AbstractContainerMenu menu, boolean vertical) {
		if (vertical) {
			return Comparator
					.comparingInt((Integer index) -> menu.getSlot(index).x)
					.thenComparingInt(index -> menu.getSlot(index).y);
		}
		return Comparator
				.comparingInt((Integer index) -> menu.getSlot(index).y)
				.thenComparingInt(index -> menu.getSlot(index).x);
	}
}
