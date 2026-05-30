package net.guilhermegomes.sort;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.Locale;

public enum SortMode {
	NAME_ASC,
	NAME_DESC,
	COUNT_ASC,
	COUNT_DESC,
	CATEGORY,
	MOD;

	public Comparator<ItemStack> comparator() {
		return switch (this) {
			case NAME_ASC -> Comparator.comparing(SortMode::nameKey)
					.thenComparing(SortMode::registryKey)
					.thenComparingInt(ItemStack::getCount);
			case NAME_DESC -> Comparator.comparing(SortMode::nameKey).reversed()
					.thenComparing(SortMode::registryKey)
					.thenComparingInt(ItemStack::getCount);
			case COUNT_ASC -> Comparator.comparingInt(ItemStack::getCount)
					.thenComparing(SortMode::nameKey)
					.thenComparing(SortMode::registryKey);
			case COUNT_DESC -> Comparator.comparingInt(ItemStack::getCount).reversed()
					.thenComparing(SortMode::nameKey)
					.thenComparing(SortMode::registryKey);
			case CATEGORY -> Comparator.comparingInt(SortMode::categoryRank)
					.thenComparing(SortMode::nameKey)
					.thenComparing(SortMode::registryKey)
					.thenComparingInt(ItemStack::getCount);
			case MOD -> Comparator.comparing(SortMode::namespaceKey)
					.thenComparing(SortMode::nameKey)
					.thenComparing(SortMode::registryKey)
					.thenComparingInt(ItemStack::getCount);
		};
	}

	private static String nameKey(ItemStack stack) {
		return stack.getHoverName().getString().toLowerCase(Locale.ROOT);
	}

	private static String registryKey(ItemStack stack) {
		Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
		return id != null ? id.toString() : stack.getItem().toString();
	}

	private static String namespaceKey(ItemStack stack) {
		Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
		return id != null ? id.getNamespace() : "unknown";
	}

	private static int categoryRank(ItemStack stack) {
		if (stack.getItem() instanceof BlockItem) {
			return 10;
		}
		if (stack.has(DataComponents.FOOD) || stack.has(DataComponents.CONSUMABLE)) {
			return 20;
		}
		if (stack.has(DataComponents.WEAPON) || stack.has(DataComponents.PIERCING_WEAPON) || stack.has(DataComponents.KINETIC_WEAPON)) {
			return 30;
		}
		if (stack.has(DataComponents.TOOL)) {
			return 40;
		}
		if (stack.has(DataComponents.EQUIPPABLE)) {
			return 50;
		}
		if (stack.has(DataComponents.CONTAINER) || stack.has(DataComponents.CONTAINER_LOOT)) {
			return 60;
		}
		if (stack.has(DataComponents.POTION_CONTENTS)) {
			return 70;
		}
		if (stack.has(DataComponents.ENCHANTMENTS) || stack.has(DataComponents.STORED_ENCHANTMENTS)) {
			return 80;
		}
		return 100;
	}
}
