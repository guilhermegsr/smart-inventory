package net.guilhermegomes.sort;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

public final class StorageSlotResolver {
	private StorageSlotResolver() {
	}

	public static boolean canAttach(AbstractContainerMenu menu, Player player, boolean preserveHotbar) {
		return hasTarget(menu, player, SortTarget.CONTAINER, preserveHotbar)
				|| hasTarget(menu, player, SortTarget.INVENTORY, preserveHotbar);
	}

	public static boolean hasTarget(AbstractContainerMenu menu, Player player, SortTarget target, boolean preserveHotbar) {
		return !resolve(menu, player, target, preserveHotbar).isEmpty();
	}

	public static List<SlotRef> resolve(AbstractContainerMenu menu, Player player, SortTarget target, boolean preserveHotbar) {
		Inventory playerInventory = player.getInventory();
		List<SlotRef> slots = new ArrayList<>();
		for (int i = 0; i < menu.slots.size(); i++) {
			Slot slot = menu.slots.get(i);
			if (target == SortTarget.INVENTORY && isSortableInventorySlot(slot, playerInventory, player, preserveHotbar)) {
				slots.add(new SlotRef(i, slot));
			}
			if (target == SortTarget.CONTAINER && isStorageMenu(menu) && isSortableContainerSlot(slot, playerInventory, player)) {
				slots.add(new SlotRef(i, slot));
			}
		}
		return slots;
	}

	private static boolean isSortableInventorySlot(Slot slot, Inventory playerInventory, Player player, boolean preserveHotbar) {
		if (!slot.isActive() || slot.isFake() || slot.container != playerInventory) {
			return false;
		}

		int inventorySlot = slot.getContainerSlot();
		if (inventorySlot < 0 || inventorySlot >= Inventory.INVENTORY_SIZE) {
			return false;
		}
		if (preserveHotbar && Inventory.isHotbarSlot(inventorySlot)) {
			return false;
		}
		return slot.getItem().isEmpty() || slot.mayPickup(player);
	}

	private static boolean isSortableContainerSlot(Slot slot, Inventory playerInventory, Player player) {
		if (!slot.isActive() || slot.isFake() || slot.container == playerInventory) {
			return false;
		}
		return slot.getItem().isEmpty() || slot.mayPickup(player);
	}

	private static boolean isStorageMenu(AbstractContainerMenu menu) {
		return menu instanceof ChestMenu
				|| menu instanceof ShulkerBoxMenu
				|| menu instanceof HopperMenu
				|| menu instanceof DispenserMenu;
	}
}
