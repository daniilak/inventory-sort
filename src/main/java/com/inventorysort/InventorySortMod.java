package com.inventorysort;

import com.inventorysort.net.SortPayload;
import com.inventorysort.net.TransferPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventorySortMod implements ModInitializer {
	public static final String MOD_ID = "inventorysort";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playC2S().register(TransferPayload.TYPE, TransferPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(SortPayload.TYPE, SortPayload.STREAM_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(TransferPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			player.server.execute(() -> TransferHandler.handle(player, payload.toChest()));
		});
		ServerPlayNetworking.registerGlobalReceiver(SortPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			player.server.execute(() -> SortHandler.handle(player, payload.container(), payload.vertical()));
		});

		LOGGER.info("Inventory Sort loaded");
	}
}
