package net.guilhermegomes.network;

import net.guilhermegomes.SmartStorage;
import net.guilhermegomes.sort.SortMode;
import net.guilhermegomes.sort.SortTarget;
import net.guilhermegomes.sort.StorageAction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * @param order ordem desejada dos slots (menu slot ids) calculada no cliente, para SORT respeitar o
 *              idioma do jogador. Vazio para as demais acoes ou como fallback (servidor ordena sozinho).
 */
public record StorageActionPayload(int containerId, StorageAction action, SortTarget target, SortMode mode, boolean preserveHotbar, int[] order) implements CustomPacketPayload {
	private static final int MAX_ORDER = 256; // limite defensivo na leitura

	public static final Type<StorageActionPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(SmartStorage.MOD_ID, "storage_action"));
	public static final StreamCodec<RegistryFriendlyByteBuf, StorageActionPayload> CODEC = StreamCodec.ofMember(StorageActionPayload::write, StorageActionPayload::read);

	private static StorageActionPayload read(RegistryFriendlyByteBuf buffer) {
		return new StorageActionPayload(
				buffer.readContainerId(),
				buffer.readEnum(StorageAction.class),
				buffer.readEnum(SortTarget.class),
				buffer.readEnum(SortMode.class),
				buffer.readBoolean(),
				buffer.readVarIntArray(MAX_ORDER)
		);
	}

	private void write(RegistryFriendlyByteBuf buffer) {
		buffer.writeContainerId(containerId);
		buffer.writeEnum(action);
		buffer.writeEnum(target);
		buffer.writeEnum(mode);
		buffer.writeBoolean(preserveHotbar);
		buffer.writeVarIntArray(order);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
