package net.guilhermegomes.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.guilhermegomes.client.sort.SmartStorageOptions;
import net.guilhermegomes.network.StorageActionPayload;
import net.guilhermegomes.sort.SlotRef;
import net.guilhermegomes.sort.SortMode;
import net.guilhermegomes.sort.SortTarget;
import net.guilhermegomes.sort.StorageAction;
import net.guilhermegomes.sort.StorageSlotResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SmartStorageClientActions {
	private static final int[] NO_ORDER = new int[0];

	private SmartStorageClientActions() {
	}

	public static boolean canSend() {
		return ClientPlayNetworking.canSend(StorageActionPayload.TYPE);
	}

	public static boolean canSend(AbstractContainerMenu menu) {
		return menu.getCarried().isEmpty() && canSend();
	}

	public static boolean send(AbstractContainerMenu menu, StorageAction action, SortTarget target, SortMode mode) {
		if (!canSend(menu)) {
			return false;
		}

		// Ordenacao depende dos nomes localizados: calcula no cliente e envia a ordem dos slots.
		int[] order = action == StorageAction.SORT ? sortOrder(menu, target, mode) : NO_ORDER;
		ClientPlayNetworking.send(new StorageActionPayload(menu.containerId, action, target, mode, SmartStorageOptions.preserveHotbar, order));
		return true;
	}

	private static int[] sortOrder(AbstractContainerMenu menu, SortTarget target, SortMode mode) {
		Player player = Minecraft.getInstance().player;
		if (player == null) {
			return NO_ORDER;
		}

		List<SlotRef> filled = new ArrayList<>();
		for (SlotRef ref : StorageSlotResolver.resolve(menu, player, target, SmartStorageOptions.preserveHotbar)) {
			if (!ref.slot().getItem().isEmpty()) {
				filled.add(ref);
			}
		}

		Comparator<ItemStack> comparator = mode.comparator();
		filled.sort((a, b) -> comparator.compare(a.slot().getItem(), b.slot().getItem()));

		int[] order = new int[filled.size()];
		for (int i = 0; i < order.length; i++) {
			order[i] = filled.get(i).menuSlotId();
		}
		return order;
	}
}
