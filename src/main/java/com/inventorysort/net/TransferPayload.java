package com.inventorysort.net;

import com.inventorysort.InventorySortMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TransferPayload(boolean toChest) implements CustomPacketPayload {
	public static final Type<TransferPayload> TYPE = new Type<>(InventorySortMod.id("transfer"));

	public static final StreamCodec<RegistryFriendlyByteBuf, TransferPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL,
			TransferPayload::toChest,
			TransferPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
