package com.inventorysort.net;

import com.inventorysort.InventorySortMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SortPayload(boolean container, boolean vertical) implements CustomPacketPayload {
	public static final Type<SortPayload> TYPE = new Type<>(InventorySortMod.id("sort"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SortPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL,
			SortPayload::container,
			ByteBufCodecs.BOOL,
			SortPayload::vertical,
			SortPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
