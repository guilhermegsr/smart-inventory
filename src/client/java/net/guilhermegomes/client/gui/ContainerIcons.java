package net.guilhermegomes.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Escolhe o icone da aba de container pelo tipo aberto: tenta o bloco que o jogador estava mirando
 * (distingue bau/barril/etc. que compartilham o mesmo menu) e cai para a classe do menu se necessario.
 */
final class ContainerIcons {
	private ContainerIcons() {
	}

	static Identifier detect(AbstractContainerMenu menu) {
		Identifier byBlock = fromBlock(lookedAtBlock());
		return byBlock != null ? byBlock : fromMenu(menu);
	}

	private static Block lookedAtBlock() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null && mc.hitResult instanceof BlockHitResult hit) {
			return mc.level.getBlockState(hit.getBlockPos()).getBlock();
		}
		return null;
	}

	private static Identifier fromBlock(Block block) {
		if (block instanceof BarrelBlock) {
			return SmartStorageSprites.icon("container_barrel");
		}
		if (block instanceof EnderChestBlock) {
			return SmartStorageSprites.icon("container_ender_chest");
		}
		if (block instanceof ChestBlock) {
			return SmartStorageSprites.icon("container_chest"); // inclui bau armadilha (subclasse)
		}
		if (block instanceof ShulkerBoxBlock) {
			return SmartStorageSprites.icon("container_shulker");
		}
		if (block instanceof DropperBlock) {
			return SmartStorageSprites.icon("container_dropper"); // antes de Dispenser (subclasse)
		}
		if (block instanceof DispenserBlock) {
			return SmartStorageSprites.icon("container_dispenser");
		}
		if (block instanceof HopperBlock) {
			return SmartStorageSprites.icon("container_hopper");
		}
		return null;
	}

	private static Identifier fromMenu(AbstractContainerMenu menu) {
		if (menu instanceof ShulkerBoxMenu) {
			return SmartStorageSprites.icon("container_shulker");
		}
		if (menu instanceof HopperMenu) {
			return SmartStorageSprites.icon("container_hopper");
		}
		if (menu instanceof DispenserMenu) {
			return SmartStorageSprites.icon("container_dispenser");
		}
		return SmartStorageSprites.icon("container_generic");
	}
}
