package net.guilhermegomes.client;

import net.guilhermegomes.client.sort.SmartStorageOptions;
import net.guilhermegomes.sort.SortTarget;
import net.guilhermegomes.sort.StorageSlotResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class SmartStorageClientTargets {
	private SmartStorageClientTargets() {
	}

	public static boolean canAttach(AbstractContainerMenu menu) {
		Player player = Minecraft.getInstance().player;
		return player != null && StorageSlotResolver.canAttach(menu, player, SmartStorageOptions.preserveHotbar());
	}

	public static boolean hasTarget(AbstractContainerMenu menu, SortTarget target) {
		Player player = Minecraft.getInstance().player;
		return player != null && StorageSlotResolver.hasTarget(menu, player, target, SmartStorageOptions.preserveHotbar());
	}
}
