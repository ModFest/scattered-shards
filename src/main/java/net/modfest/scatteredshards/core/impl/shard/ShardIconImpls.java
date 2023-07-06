package net.modfest.scatteredshards.core.impl.shard;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.core.api.shard.ShardIcon;

public class ShardIconImpls {

	public record TextureBacked(Identifier texture) implements ShardIcon {

		@Override
		public void render() {

		}
	}

	public record StackBacked(ItemStack stack) implements ShardIcon {

		@Override
		public void render() {

		}
	}
}
