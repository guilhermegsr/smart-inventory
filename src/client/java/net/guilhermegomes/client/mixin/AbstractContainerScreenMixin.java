package net.guilhermegomes.client.mixin;

import net.guilhermegomes.client.SmartStorageClientTargets;
import net.guilhermegomes.client.gui.SmartStorageMenuWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

	@Inject(method = "init", at = @At("RETURN"))
	private void smartStorage$addMenu(CallbackInfo ci) {
		AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
		if (!SmartStorageClientTargets.canAttach(screen.getMenu())) {
			return;
		}

		int x = leftPos + imageWidth + 8;
		if (x + SmartStorageMenuWidget.TRIGGER_WIDTH > this.width) {
			x = leftPos - SmartStorageMenuWidget.TRIGGER_WIDTH - 8;
		}

		this.addRenderableWidget(new SmartStorageMenuWidget(screen, x, Math.max(4, topPos)));
	}
}
