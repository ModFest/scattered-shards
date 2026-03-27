package net.modfest.scatteredshards.mixin;

import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.modfest.scatteredshards.mixinsupport.ShardArgument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntitySelectorParser.class)
public class EntitySelectorParserMixin implements ShardArgument {

	@Unique
	private boolean scards$hasShard = false;

	@Override
	public void setHasShard(boolean value) {
		scards$hasShard = value;
	}

	@Override
	public boolean selectsByShard() {
		return scards$hasShard;
	}
}
