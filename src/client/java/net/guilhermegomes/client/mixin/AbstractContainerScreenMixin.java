package net.guilhermegomes.client.mixin;

import net.guilhermegomes.client.SmartStorageClientTargets;
import net.guilhermegomes.client.gui.SmartStorageMenuWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {
	protected AbstractContainerScreenMixin(Component title) {
		super(title);
	}

	@Shadow
	protected int leftPos;

	@Shadow
	protected int topPos;

	@Shadow
	@Final
	protected int imageWidth;

	@Unique
	private SmartStorageMenuWidget smartStorage$menu;

	@Inject(method = "init", at = @At("RETURN"))
	private void smartStorage$addMenu(CallbackInfo ci) {
		AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
		if (!SmartStorageClientTargets.canAttach(screen.getMenu())) {
			smartStorage$menu = null;
			return;
		}

		smartStorage$menu = new SmartStorageMenuWidget(screen, smartStorage$menuX(), smartStorage$menuY());
		this.addRenderableWidget(smartStorage$menu);
	}

	// O livro de receitas desloca leftPos depois do init (e ao alternar/redimensionar);
	// reposiciona o painel a cada frame para mante-lo colado a borda da GUI.
	@Inject(method = "extractContents", at = @At("HEAD"))
	private void smartStorage$followGui(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		if (smartStorage$menu != null) {
			smartStorage$menu.setX(smartStorage$menuX());
			smartStorage$menu.setY(smartStorage$menuY());
		}
	}

	@Unique
	private int smartStorage$menuX() {
		int x = leftPos + imageWidth + 8;
		if (x + SmartStorageMenuWidget.TRIGGER_WIDTH > this.width) {
			x = leftPos - SmartStorageMenuWidget.TRIGGER_WIDTH - 8;
		}
		return x;
	}

	@Unique
	private int smartStorage$menuY() {
		return Math.max(4, topPos);
	}
}
