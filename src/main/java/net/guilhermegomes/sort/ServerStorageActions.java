package net.guilhermegomes.sort;

import net.guilhermegomes.network.StorageActionPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class ServerStorageActions {
	private ServerStorageActions() {
	}

	public static void handle(ServerPlayer player, StorageActionPayload payload) {
		AbstractContainerMenu menu = player.containerMenu;
		if (menu.containerId != payload.containerId() || !menu.getCarried().isEmpty() || !menu.stillValid(player)) {
			return;
		}

		switch (payload.action()) {
			case SORT -> sort(player, menu, payload.target(), payload.mode(), payload.preserveHotbar(), payload.order());
			case COMPACT -> compact(player, menu, payload.target(), payload.preserveHotbar());
			case QUICK_STACK -> quickStack(player, menu, payload.preserveHotbar());
			case PULL_MATCHES -> pullMatches(player, menu, payload.preserveHotbar());
			case DEPOSIT_INVENTORY -> depositInventory(player, menu, payload.preserveHotbar());
			case WITHDRAW_CONTAINER -> withdrawContainer(player, menu, payload.preserveHotbar());
		}
	}

	private static void sort(ServerPlayer player, AbstractContainerMenu menu, SortTarget target, SortMode mode, boolean preserveHotbar, int[] order) {
		List<SlotRef> slots = StorageSlotResolver.resolve(menu, player, target, preserveHotbar);
		if (slots.size() < 2) {
			return;
		}

		// O cliente envia a ordem (resolvida no idioma do jogador); sem ela, o servidor ordena sozinho.
		List<ItemStack> sorted = order.length > 0 ? orderedStacks(slots, order) : sortedStacks(slots, mode);
		if (!canApply(player, slots, sorted)) {
			return;
		}

		apply(menu, slots, sorted);
	}

	private static List<ItemStack> orderedStacks(List<SlotRef> slots, int[] order) {
		Map<Integer, ItemStack> available = new LinkedHashMap<>();
		for (SlotRef ref : slots) {
			ItemStack stack = ref.slot().getItem();
			if (!stack.isEmpty()) {
				available.put(ref.menuSlotId(), stack.copy());
			}
		}

		// Permutacao dos itens atuais: cada slot pedido e consumido uma vez, entao nada duplica.
		List<ItemStack> result = new ArrayList<>(slots.size());
		for (int id : order) {
			ItemStack stack = available.remove(id);
			if (stack != null) {
				result.add(stack);
			}
		}
		result.addAll(available.values()); // itens nao citados pelo cliente vao para o fim (nada se perde)
		while (result.size() < slots.size()) {
			result.add(ItemStack.EMPTY);
		}
		return result;
	}

	private static void compact(ServerPlayer player, AbstractContainerMenu menu, SortTarget target, boolean preserveHotbar) {
		List<SlotRef> slots = StorageSlotResolver.resolve(menu, player, target, preserveHotbar);
		List<ItemStack> compacted = compactedStacks(slots);
		if (compacted.isEmpty() || !canApply(player, slots, compacted) || listsMatch(slots, compacted)) {
			return;
		}

		apply(menu, slots, compacted);
	}

	private static void quickStack(ServerPlayer player, AbstractContainerMenu menu, boolean preserveHotbar) {
		List<SlotRef> containerSlots = StorageSlotResolver.resolve(menu, player, SortTarget.CONTAINER, preserveHotbar);
		List<SlotRef> inventorySlots = StorageSlotResolver.resolve(menu, player, SortTarget.INVENTORY, preserveHotbar);
		List<ItemStack> containerItems = nonEmptyStacks(containerSlots);
		transfer(menu, player, inventorySlots, containerSlots, stack -> containsEquivalent(containerItems, stack));
	}

	private static void pullMatches(ServerPlayer player, AbstractContainerMenu menu, boolean preserveHotbar) {
		List<SlotRef> containerSlots = StorageSlotResolver.resolve(menu, player, SortTarget.CONTAINER, preserveHotbar);
		List<SlotRef> inventorySlots = StorageSlotResolver.resolve(menu, player, SortTarget.INVENTORY, preserveHotbar);
		List<ItemStack> inventoryItems = nonEmptyStacks(inventorySlots);
		transfer(menu, player, containerSlots, inventorySlots, stack -> containsEquivalent(inventoryItems, stack));
	}

	private static void depositInventory(ServerPlayer player, AbstractContainerMenu menu, boolean preserveHotbar) {
		List<SlotRef> containerSlots = StorageSlotResolver.resolve(menu, player, SortTarget.CONTAINER, preserveHotbar);
		List<SlotRef> inventorySlots = StorageSlotResolver.resolve(menu, player, SortTarget.INVENTORY, preserveHotbar);
		transfer(menu, player, inventorySlots, containerSlots, stack -> true);
	}

	private static void withdrawContainer(ServerPlayer player, AbstractContainerMenu menu, boolean preserveHotbar) {
		List<SlotRef> containerSlots = StorageSlotResolver.resolve(menu, player, SortTarget.CONTAINER, preserveHotbar);
		List<SlotRef> inventorySlots = StorageSlotResolver.resolve(menu, player, SortTarget.INVENTORY, preserveHotbar);
		transfer(menu, player, containerSlots, inventorySlots, stack -> true);
	}

	private static boolean transfer(AbstractContainerMenu menu, ServerPlayer player, List<SlotRef> sourceSlots, List<SlotRef> targetSlots, Predicate<ItemStack> sourcePredicate) {
		if (sourceSlots.isEmpty() || targetSlots.isEmpty()) {
			return false;
		}

		List<ItemStack> sourceStacks = snapshot(sourceSlots);
		List<ItemStack> targetStacks = snapshot(targetSlots);
		boolean changed = false;

		for (int i = 0; i < sourceStacks.size(); i++) {
			ItemStack source = sourceStacks.get(i);
			if (source.isEmpty() || !sourcePredicate.test(source)) {
				continue;
			}
			if (!sourceSlots.get(i).slot().mayPickup(player)) {
				continue;
			}

			int before = source.getCount();
			moveIntoTargets(source, targetSlots, targetStacks);
			if (source.getCount() != before) {
				changed = true;
				if (source.getCount() <= 0) {
					sourceStacks.set(i, ItemStack.EMPTY);
				}
			}
		}

		if (!changed || !canApply(player, sourceSlots, sourceStacks) || !canApply(player, targetSlots, targetStacks)) {
			return false;
		}

		apply(menu, sourceSlots, sourceStacks);
		apply(menu, targetSlots, targetStacks);
		return true;
	}

	private static void moveIntoTargets(ItemStack source, List<SlotRef> targetSlots, List<ItemStack> targetStacks) {
		for (int i = 0; i < targetStacks.size() && !source.isEmpty(); i++) {
			ItemStack target = targetStacks.get(i);
			if (target.isEmpty() || !ItemStack.isSameItemSameComponents(target, source)) {
				continue;
			}

			Slot slot = targetSlots.get(i).slot();
			if (!slot.mayPlace(source)) {
				continue;
			}

			int maxCount = slot.getMaxStackSize(source);
			int moved = Math.min(source.getCount(), maxCount - target.getCount());
			if (moved <= 0) {
				continue;
			}

			target.grow(moved);
			source.shrink(moved);
		}

		for (int i = 0; i < targetStacks.size() && !source.isEmpty(); i++) {
			ItemStack target = targetStacks.get(i);
			if (!target.isEmpty()) {
				continue;
			}

			Slot slot = targetSlots.get(i).slot();
			if (!slot.mayPlace(source)) {
				continue;
			}

			int moved = Math.min(source.getCount(), slot.getMaxStackSize(source));
			if (moved <= 0) {
				continue;
			}

			targetStacks.set(i, source.split(moved));
		}
	}

	private static List<ItemStack> sortedStacks(List<SlotRef> slots, SortMode mode) {
		List<ItemStack> stacks = nonEmptyStacks(slots);
		stacks.sort(mode.comparator());
		while (stacks.size() < slots.size()) {
			stacks.add(ItemStack.EMPTY);
		}
		return stacks;
	}

	private static List<ItemStack> compactedStacks(List<SlotRef> slots) {
		List<ItemStack> stacks = snapshot(slots);
		if (stacks.isEmpty()) {
			return List.of();
		}

		for (int targetIndex = 0; targetIndex < stacks.size(); targetIndex++) {
			ItemStack target = stacks.get(targetIndex);
			if (target.isEmpty()) {
				continue;
			}

			int maxCount = slots.get(targetIndex).slot().getMaxStackSize(target);
			if (maxCount <= 1 || target.getCount() >= maxCount) {
				continue;
			}

			for (int sourceIndex = targetIndex + 1; sourceIndex < stacks.size() && target.getCount() < maxCount; sourceIndex++) {
				ItemStack source = stacks.get(sourceIndex);
				if (source.isEmpty() || !ItemStack.isSameItemSameComponents(target, source)) {
					continue;
				}

				int moved = Math.min(source.getCount(), maxCount - target.getCount());
				target.grow(moved);
				source.shrink(moved);
				if (source.isEmpty()) {
					stacks.set(sourceIndex, ItemStack.EMPTY);
				}
			}
		}

		List<ItemStack> packed = new ArrayList<>(stacks.size());
		for (ItemStack stack : stacks) {
			if (!stack.isEmpty()) {
				packed.add(stack.copy());
			}
		}
		while (packed.size() < stacks.size()) {
			packed.add(ItemStack.EMPTY);
		}

		return packed;
	}

	private static List<ItemStack> snapshot(List<SlotRef> slots) {
		List<ItemStack> stacks = new ArrayList<>(slots.size());
		for (SlotRef slot : slots) {
			stacks.add(slot.slot().getItem().copy());
		}
		return stacks;
	}

	private static List<ItemStack> nonEmptyStacks(List<SlotRef> slots) {
		List<ItemStack> stacks = new ArrayList<>();
		for (SlotRef slot : slots) {
			ItemStack stack = slot.slot().getItem();
			if (!stack.isEmpty()) {
				stacks.add(stack.copy());
			}
		}
		return stacks;
	}

	private static boolean canApply(ServerPlayer player, List<SlotRef> slots, List<ItemStack> desiredStacks) {
		if (slots.size() != desiredStacks.size()) {
			return false;
		}

		for (int i = 0; i < slots.size(); i++) {
			Slot slot = slots.get(i).slot();
			ItemStack current = slot.getItem();
			ItemStack desired = desiredStacks.get(i);
			if (!current.isEmpty() && !slot.mayPickup(player)) {
				return false;
			}
			if (!desired.isEmpty() && (!slot.mayPlace(desired) || desired.getCount() > slot.getMaxStackSize(desired))) {
				return false;
			}
		}
		return true;
	}

	private static void apply(AbstractContainerMenu menu, List<SlotRef> slots, List<ItemStack> desiredStacks) {
		for (int i = 0; i < slots.size(); i++) {
			ItemStack desired = desiredStacks.get(i);
			slots.get(i).slot().set(desired.isEmpty() ? ItemStack.EMPTY : desired.copy());
		}
		menu.broadcastChanges();
	}

	private static boolean listsMatch(List<SlotRef> slots, List<ItemStack> desiredStacks) {
		for (int i = 0; i < slots.size(); i++) {
			if (!ItemStack.matches(slots.get(i).slot().getItem(), desiredStacks.get(i))) {
				return false;
			}
		}
		return true;
	}

	private static boolean containsEquivalent(List<ItemStack> stacks, ItemStack stack) {
		for (ItemStack candidate : stacks) {
			if (ItemStack.isSameItemSameComponents(candidate, stack)) {
				return true;
			}
		}
		return false;
	}
}
