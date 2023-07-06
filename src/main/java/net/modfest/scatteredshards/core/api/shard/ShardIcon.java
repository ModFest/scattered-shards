package net.modfest.scatteredshards.shard;

import org.quiltmc.loader.api.minecraft.ClientOnly;

public abstract class ShardIcon {

	@ClientOnly
	public abstract void render();

	private static class TextureBacked extends ShardIcon {

		@Override
		public void render() {

		}
	}
}
