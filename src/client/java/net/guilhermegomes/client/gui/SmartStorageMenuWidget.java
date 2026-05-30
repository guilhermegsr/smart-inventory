package net.guilhermegomes.client.gui;

import net.guilhermegomes.client.SmartStorageClientActions;
import net.guilhermegomes.client.SmartStorageClientTargets;
import net.guilhermegomes.client.sort.SmartStorageOptions;
import net.guilhermegomes.sort.SortMode;
import net.guilhermegomes.sort.SortTarget;
import net.guilhermegomes.sort.StorageAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Painel flutuante anexado a telas de container. Renderiza com sprites nine-slice do vanilla
 * (aba/botao/painel) e icones proprios do mod; a logica de organizar roda no servidor.
 * Todo texto e Component.translatable, resolvido no idioma do cliente.
 */
public class SmartStorageMenuWidget extends AbstractWidget {
	public static final int TRIGGER_WIDTH = 24;

	private static final int TRIGGER_HEIGHT = 24;
	private static final int OPEN_DROP = 3; // aberto: gatilho desce e mergulha sob a borda do painel
	private static final int PAD = 7;
	private static final int GAP = 2;
	private static final int ROW_GAP = 3;
	private static final int DIVIDER_GAP = 6;
	private static final int GROUP_GAP = 14; // separa o grupo "enviar" do grupo "pegar"
	private static final int BTN = 18;
	private static final int ICON = 14;
	private static final int SORT_COLUMNS = 6;
	private static final int PANEL_WIDTH = PAD * 2 + SORT_COLUMNS * BTN + (SORT_COLUMNS - 1) * GAP;

	private static final int SEP_DARK = 0xFF0E1014;
	private static final int SEP_LIGHT = 0xFF3C4250;
	private static final int ACCENT = 0xFF6EA8FF;

	private static final SortMode[] SORT_MODES = {
			SortMode.NAME_ASC, SortMode.NAME_DESC, SortMode.COUNT_ASC,
			SortMode.COUNT_DESC, SortMode.CATEGORY, SortMode.MOD
	};

	private final AbstractContainerScreen<?> screen;
	private final Identifier containerIcon;
	private SortTarget target;
	private boolean open;

	public SmartStorageMenuWidget(AbstractContainerScreen<?> screen, int x, int y) {
		super(x, y, TRIGGER_WIDTH, TRIGGER_HEIGHT, Component.translatable("smart_storage.title"));
		this.screen = screen;
		this.containerIcon = ContainerIcons.detect(screen.getMenu());
		this.target = SmartStorageClientTargets.hasTarget(screen.getMenu(), SortTarget.CONTAINER)
				? SortTarget.CONTAINER
				: SortTarget.INVENTORY;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		drawTrigger(graphics, mouseX, mouseY);
		if (!open) {
			return;
		}

		graphics.nextStratum();
		drawPanel(graphics, layout(), mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (!visible || !active || event.button() != 0) {
			return false;
		}

		double mouseX = event.x();
		double mouseY = event.y();
		if (inside(mouseX, mouseY, getX(), triggerY(), TRIGGER_WIDTH, TRIGGER_HEIGHT)) {
			open = !open;
			playDownSound(Minecraft.getInstance().getSoundManager());
			return true;
		}

		if (!open) {
			return false;
		}

		Layout layout = layout();
		if (!inside(mouseX, mouseY, layout.x(), layout.y(), PANEL_WIDTH, layout.height())) {
			open = false;
			return false;
		}

		// O painel continua aberto apos uma acao; so fecha pelo gatilho ou clicando fora.
		for (MenuButton button : layout.buttons()) {
			if (!inside(mouseX, mouseY, button.x(), button.y(), button.width(), BTN)) {
				continue;
			}
			if (button.enabled()) {
				button.action().run();
				playDownSound(Minecraft.getInstance().getSoundManager());
			}
			return true;
		}

		return true;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		if (!visible) {
			return false;
		}
		Layout layout = layout();
		return inside(mouseX, mouseY, getX(), triggerY(), TRIGGER_WIDTH, TRIGGER_HEIGHT)
				|| open && inside(mouseX, mouseY, layout.x(), layout.y(), PANEL_WIDTH, layout.height());
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		output.add(NarratedElementType.TITLE, getMessage());
		output.add(NarratedElementType.USAGE, Component.translatable("smart_storage.narration.usage"));
	}

	private void drawTrigger(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		int y = triggerY();
		boolean hovered = inside(mouseX, mouseY, getX(), y, TRIGGER_WIDTH, TRIGGER_HEIGHT);
		// fechado: botao completo; aberto: aba (caixa com fundo aberto) que mergulha no painel abaixo.
		Identifier sprite = open ? SmartStorageSprites.tab(hovered) : SmartStorageSprites.button(true, hovered);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, getX(), y, TRIGGER_WIDTH, TRIGGER_HEIGHT);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SmartStorageSprites.EMBLEM, getX() + (TRIGGER_WIDTH - ICON) / 2, y + (TRIGGER_HEIGHT - ICON) / 2, ICON, ICON);
		if (hovered) {
			graphics.setTooltipForNextFrame(Component.translatable(open ? "smart_storage.trigger.close" : "smart_storage.trigger.open"), mouseX, mouseY);
		}
	}

	private int triggerY() {
		return getY() + (open ? OPEN_DROP : 0);
	}

	private void drawPanel(GuiGraphicsExtractor graphics, Layout layout, int mouseX, int mouseY) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SmartStorageSprites.PANEL, layout.x(), layout.y(), PANEL_WIDTH, layout.height());
		for (int separatorY : layout.separators()) {
			graphics.fill(layout.x() + PAD, separatorY, layout.x() + PANEL_WIDTH - PAD, separatorY + 1, SEP_DARK);
			graphics.fill(layout.x() + PAD, separatorY + 1, layout.x() + PANEL_WIDTH - PAD, separatorY + 2, SEP_LIGHT);
		}
		for (MenuButton button : layout.buttons()) {
			drawMenuButton(graphics, button, mouseX, mouseY);
		}
	}

	private void drawMenuButton(GuiGraphicsExtractor graphics, MenuButton button, int mouseX, int mouseY) {
		boolean over = inside(mouseX, mouseY, button.x(), button.y(), button.width(), BTN);
		boolean hovered = over && button.enabled();
		Identifier sprite = button.tab()
				? SmartStorageSprites.selector(button.enabled(), button.selected(), hovered)
				: SmartStorageSprites.button(button.enabled(), hovered);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, button.x(), button.y(), button.width(), BTN);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, button.icon(), button.x() + (button.width() - ICON) / 2, button.y() + (BTN - ICON) / 2, ICON, ICON);
		if (button.tab() && button.selected()) {
			graphics.fill(button.x() + 1, button.y() + BTN - 2, button.x() + button.width() - 1, button.y() + BTN, ACCENT);
		}

		if (over && button.tooltip() != null) {
			graphics.setTooltipForNextFrame(button.tooltip(), mouseX, mouseY);
		}
	}

	private Layout layout() {
		AbstractContainerMenu menu = screen.getMenu();
		boolean canSend = SmartStorageClientActions.canSend(menu);
		boolean hasContainer = SmartStorageClientTargets.hasTarget(menu, SortTarget.CONTAINER);
		boolean hasInventory = SmartStorageClientTargets.hasTarget(menu, SortTarget.INVENTORY);
		if (target == SortTarget.CONTAINER && !hasContainer) {
			target = SortTarget.INVENTORY;
		}
		if (target == SortTarget.INVENTORY && !hasInventory && hasContainer) {
			target = SortTarget.CONTAINER;
		}

		boolean hasTransferActions = hasContainer && hasInventory;
		int height = contentHeight(hasTransferActions);
		int x = panelX();
		int y = panelY(height);
		int left = x + PAD;
		int inner = PANEL_WIDTH - PAD * 2;
		Component disabledReason = disabledReason(menu);

		List<MenuButton> buttons = new ArrayList<>();
		List<Integer> separators = new ArrayList<>();
		int cursor = y + PAD;

		// Filtros: alvo (seletores Container/Inv) + ordenacao
		int tabWidth = (inner - GAP) / 2;
		buttons.add(tabButton(SortTarget.CONTAINER, containerIcon, left, cursor, tabWidth, hasContainer, Component.translatable("smart_storage.target", screen.getTitle())));
		buttons.add(tabButton(SortTarget.INVENTORY, SmartStorageSprites.icon("tab_inventory"), left + tabWidth + GAP, cursor, inner - tabWidth - GAP, hasInventory, Component.translatable("smart_storage.target", Component.translatable("smart_storage.inventory"))));
		cursor += BTN + ROW_GAP;
		for (int i = 0; i < SORT_MODES.length; i++) {
			buttons.add(sortButton(menu, SORT_MODES[i], left + i * (BTN + GAP), cursor, canSend, disabledReason));
		}
		cursor += BTN;

		separators.add(cursor + (DIVIDER_GAP - 2) / 2);
		cursor += DIVIDER_GAP;

		// Acoes: transferencias agrupadas por direcao (enviar | pegar); Juntar fica isolado abaixo.
		if (hasTransferActions) {
			int cluster = 2 * BTN + GAP;
			int sendLeft = left + (inner - (2 * cluster + GROUP_GAP)) / 2;
			int takeLeft = sendLeft + cluster + GROUP_GAP;
			buttons.add(actionButton(menu, StorageAction.QUICK_STACK, sendLeft, cursor, canSend, tooltip(canSend, Component.translatable("smart_storage.action.quick_stack"), disabledReason)));
			buttons.add(actionButton(menu, StorageAction.DEPOSIT_INVENTORY, sendLeft + BTN + GAP, cursor, canSend, tooltip(canSend, Component.translatable("smart_storage.action.deposit"), disabledReason)));
			buttons.add(actionButton(menu, StorageAction.PULL_MATCHES, takeLeft, cursor, canSend, tooltip(canSend, Component.translatable("smart_storage.action.pull"), disabledReason)));
			buttons.add(actionButton(menu, StorageAction.WITHDRAW_CONTAINER, takeLeft + BTN + GAP, cursor, canSend, tooltip(canSend, Component.translatable("smart_storage.action.withdraw"), disabledReason)));
			cursor += BTN + GAP;
		}
		boolean targetAvailable = SmartStorageClientTargets.hasTarget(menu, target);
		buttons.add(actionButton(menu, StorageAction.COMPACT, left + (inner - BTN) / 2, cursor, canSend && targetAvailable, tooltip(canSend && targetAvailable, Component.translatable("smart_storage.action.compact"), disabledReason)));
		cursor += BTN;

		separators.add(cursor + (DIVIDER_GAP - 2) / 2);
		cursor += DIVIDER_GAP;

		// Opcao: hotbar
		Identifier lockIcon = SmartStorageSprites.icon(SmartStorageOptions.preserveHotbar ? "lock_closed" : "lock_open");
		Component hotbarTooltip = Component.translatable(SmartStorageOptions.preserveHotbar ? "smart_storage.hotbar.protected" : "smart_storage.hotbar.included");
		buttons.add(new MenuButton(left + (inner - BTN) / 2, cursor, BTN, lockIcon, true,
				() -> SmartStorageOptions.preserveHotbar = !SmartStorageOptions.preserveHotbar, hotbarTooltip, false, false));

		return new Layout(x, y, height, buttons, separators);
	}

	private MenuButton tabButton(SortTarget tabTarget, Identifier icon, int x, int y, int width, boolean enabled, Component tooltip) {
		return new MenuButton(x, y, width, icon, enabled,
				() -> target = tabTarget, tooltip, true, target == tabTarget);
	}

	private MenuButton sortButton(AbstractContainerMenu menu, SortMode mode, int x, int y, boolean canSend, Component disabledReason) {
		boolean enabled = canSend && SmartStorageClientTargets.hasTarget(menu, target);
		return new MenuButton(x, y, BTN, SmartStorageSprites.icon(mode), enabled,
				() -> SmartStorageClientActions.send(menu, StorageAction.SORT, target, mode),
				tooltip(enabled, sortDescription(mode), disabledReason), false, false);
	}

	private MenuButton actionButton(AbstractContainerMenu menu, StorageAction action, int x, int y, boolean enabled, Component tooltip) {
		SortTarget actionTarget = action == StorageAction.COMPACT ? target : SortTarget.CONTAINER;
		return new MenuButton(x, y, BTN, SmartStorageSprites.icon(action), enabled,
				() -> SmartStorageClientActions.send(menu, action, actionTarget, SortMode.NAME_ASC), tooltip, false, false);
	}

	private static Component sortDescription(SortMode mode) {
		return Component.translatable(switch (mode) {
			case NAME_ASC -> "smart_storage.sort.name_asc";
			case NAME_DESC -> "smart_storage.sort.name_desc";
			case COUNT_ASC -> "smart_storage.sort.count_asc";
			case COUNT_DESC -> "smart_storage.sort.count_desc";
			case CATEGORY -> "smart_storage.sort.category";
			case MOD -> "smart_storage.sort.mod";
		});
	}

	private Component disabledReason(AbstractContainerMenu menu) {
		if (!menu.getCarried().isEmpty()) {
			return Component.translatable("smart_storage.disabled.carried");
		}
		if (!SmartStorageClientActions.canSend()) {
			return Component.translatable("smart_storage.disabled.server");
		}
		if (!SmartStorageClientTargets.hasTarget(menu, target)) {
			return Component.translatable("smart_storage.disabled.target");
		}
		return null;
	}

	private static Component tooltip(boolean enabled, Component enabledText, Component disabledReason) {
		return enabled || disabledReason == null ? enabledText : disabledReason;
	}

	private int panelX() {
		return Math.max(4, Math.min(getX(), screen.width - PANEL_WIDTH - 4));
	}

	private int panelY(int height) {
		int preferred = getY() + TRIGGER_HEIGHT - 1;
		int maxY = Math.max(4, screen.height - height - 4);
		return Math.max(4, Math.min(preferred, maxY));
	}

	private static int contentHeight(boolean hasTransferActions) {
		int actionRows = hasTransferActions ? 2 : 1;
		return PAD + BTN + ROW_GAP + BTN
				+ DIVIDER_GAP + actionRows * BTN + (actionRows - 1) * GAP
				+ DIVIDER_GAP + BTN + PAD;
	}

	private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	private record Layout(int x, int y, int height, List<MenuButton> buttons, List<Integer> separators) {
	}

	private record MenuButton(int x, int y, int width, Identifier icon, boolean enabled, Runnable action,
							  Component tooltip, boolean tab, boolean selected) {
	}
}
