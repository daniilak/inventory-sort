package com.inventorysort;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class MenuSlots {
	public final int[] container;
	public final int[] playerMain;
	public final int[] playerStorage;
	public final boolean storageContainer;

	private MenuSlots(int[] container, int[] playerMain, int[] playerStorage, boolean storageContainer) {
		this.container = container;
		this.playerMain = playerMain;
		this.playerStorage = playerStorage;
		this.storageContainer = storageContainer;
	}

	public static MenuSlots of(AbstractContainerMenu menu, Player player) {
		Inventory playerInv = player.getInventory();
		List<Integer> container = new ArrayList<>();
		List<Integer> playerMain = new ArrayList<>();
		List<Integer> hotbar = new ArrayList<>();

		for (int i = 0; i < menu.slots.size(); i++) {
			Slot slot = menu.getSlot(i);
			if (!slot.isActive() || slot.container == null) {
				continue;
			}

			if (slot.container == playerInv) {
				int invIndex = slot.getContainerSlot();
				if (invIndex >= 0 && invIndex < 9) {
					hotbar.add(i);
				} else if (invIndex >= 9 && invIndex < 36) {
					playerMain.add(i);
				}
				continue;
			}

			if (menu instanceof InventoryMenu && (slot.container instanceof CraftingContainer || slot.container instanceof ResultContainer)) {
				continue;
			}
			if (slot instanceof ResultSlot || slot.container instanceof ResultContainer || isOutputSlot(slot)) {
				continue;
			}

			container.add(i);
		}

		playerMain.sort(byVisual(menu));
		hotbar.sort(byVisual(menu));
		container.sort(byVisual(menu));

		int[] main = toArray(playerMain);
		int[] bar = toArray(hotbar);
		return new MenuSlots(toArray(container), main, concat(main, bar), !container.isEmpty());
	}

	public boolean canTransfer() {
		return storageContainer && container.length > 0 && playerMain.length > 0;
	}

	public boolean canSortContainer() {
		return storageContainer && container.length > 1;
	}

	public boolean canSortPlayer() {
		return playerMain.length > 0;
	}

	public static boolean movable(Slot slot, Player player) {
		return slot.isActive() && slot.mayPickup(player) && slot.container != null;
	}

	private static boolean isOutputSlot(Slot slot) {
		for (ItemStack probe : List.of(
				new ItemStack(Items.DIRT),
				new ItemStack(Items.DIAMOND),
				new ItemStack(Items.WHITE_WOOL))) {
			if (slot.mayPlace(probe)) {
				return false;
			}
		}

		// Поддерживаем специализированные слоты модов, которые принимают
		// только один тип предмета, но не принимаем настоящие output-слоты.
		for (var item : BuiltInRegistries.ITEM) {
			if (slot.mayPlace(new ItemStack(item))) {
				return false;
			}
		}
		return true;
	}

	private static Comparator<Integer> byVisual(AbstractContainerMenu menu) {
		return Comparator
				.comparingInt((Integer index) -> menu.getSlot(index).y)
				.thenComparingInt(index -> menu.getSlot(index).x);
	}

	private static int[] toArray(List<Integer> values) {
		return values.stream().mapToInt(Integer::intValue).toArray();
	}

	private static int[] concat(int[] a, int[] b) {
		int[] out = new int[a.length + b.length];
		System.arraycopy(a, 0, out, 0, a.length);
		System.arraycopy(b, 0, out, a.length, b.length);
		return out;
	}
}
