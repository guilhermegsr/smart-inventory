package net.guilhermegomes.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.guilhermegomes.sort.ServerStorageActions;

public final class SmartStorageNetworking {
	private SmartStorageNetworking() {
	}

	public static void register() {
		PayloadTypeRegistry.serverboundPlay().register(StorageActionPayload.TYPE, StorageActionPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(StorageActionPayload.TYPE, (payload, context) -> context.server().execute(() -> ServerStorageActions.handle(context.player(), payload)));
	}
}
