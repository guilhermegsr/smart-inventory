package net.guilhermegomes.client.gui;

import net.guilhermegomes.SmartStorage;
import net.guilhermegomes.sort.SortMode;
import net.guilhermegomes.sort.StorageAction;
import net.minecraft.resources.Identifier;

/** Sprites da UI: widgets nine-slice do vanilla e os icones proprios do mod (atlas de GUI). */
final class SmartStorageSprites {
	static final Identifier PANEL = Identifier.withDefaultNamespace("popup/background");
	static final Identifier BUTTON = Identifier.withDefaultNamespace("widget/button");
	static final Identifier BUTTON_HIGHLIGHTED = Identifier.withDefaultNamespace("widget/button_highlighted");
	static final Identifier BUTTON_DISABLED = Identifier.withDefaultNamespace("widget/button_disabled");
	static final Identifier TAB = Identifier.withDefaultNamespace("widget/tab");
	static final Identifier TAB_HIGHLIGHTED = Identifier.withDefaultNamespace("widget/tab_highlighted");

	static final Identifier EMBLEM = icon("emblem");

	private SmartStorageSprites() {
	}

	static Identifier button(boolean enabled, boolean hovered) {
		if (!enabled) {
			return BUTTON_DISABLED;
		}
		return hovered ? BUTTON_HIGHLIGHTED : BUTTON;
	}

	static Identifier tab(boolean hovered) {
		return hovered ? TAB_HIGHLIGHTED : TAB;
	}

	// Seletor Bau/Inv: caixa de botao (o sprite de aba tem fundo aberto/transparente e nao serve aqui).
	static Identifier selector(boolean enabled, boolean selected, boolean hovered) {
		if (!enabled) {
			return BUTTON_DISABLED;
		}
		return selected || hovered ? BUTTON_HIGHLIGHTED : BUTTON;
	}

	static Identifier icon(SortMode mode) {
		return icon(switch (mode) {
			case NAME_ASC -> "sort_name_asc";
			case NAME_DESC -> "sort_name_desc";
			case COUNT_ASC -> "sort_count_asc";
			case COUNT_DESC -> "sort_count_desc";
			case CATEGORY -> "sort_category";
			case MOD -> "sort_mod";
		});
	}

	static Identifier icon(StorageAction action) {
		return icon(switch (action) {
			case COMPACT -> "action_compact";
			case QUICK_STACK -> "action_quick_stack";
			case PULL_MATCHES -> "action_pull";
			case DEPOSIT_INVENTORY -> "action_deposit";
			case WITHDRAW_CONTAINER -> "action_withdraw";
			case SORT -> "emblem";
		});
	}

	static Identifier icon(String name) {
		return Identifier.fromNamespaceAndPath(SmartStorage.MOD_ID, "icon/" + name);
	}
}
