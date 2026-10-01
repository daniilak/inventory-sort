package com.inventorysort.client.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
	@Accessor("leftPos")
	int inventorysort$leftPos();

	@Accessor("topPos")
	int inventorysort$topPos();

	@Accessor("imageWidth")
	int inventorysort$imageWidth();

	@Accessor("imageHeight")
	int inventorysort$imageHeight();
}
