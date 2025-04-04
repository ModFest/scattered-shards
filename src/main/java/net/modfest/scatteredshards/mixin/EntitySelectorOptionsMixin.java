package net.modfest.scatteredshards.mixin;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.EntitySelectorOptions;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.mixinsupport.ShardArgument;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Mixin(EntitySelectorOptions.class)
public class EntitySelectorOptionsMixin {

	@Unique
	private static final DynamicCommandExceptionType scards$UNKNOWN_ID = new DynamicCommandExceptionType(
		shardId -> Text.stringifiedTranslatable("argument.scattered_shards.entity.options.has_shard.invalid", shardId)
	);

	// stolen with permission from fireblanket
	@Shadow
	private static void putOption(String id, EntitySelectorOptions.SelectorHandler handler, Predicate<EntitySelectorReader> condition, Text description) {
		throw new IllegalStateException("Unimplemented mixin");
	}

	@SuppressWarnings("rawtypes")
	@Shadow
	@Final
	private static Map OPTIONS;

	@Inject(method = "register", at = @At("TAIL"))
	private static void injectShard(CallbackInfo info) {
		if (!OPTIONS.containsKey("has_shard")) {
			putOption("has_shard", reader -> {
				reader.setSuggestionProvider((builder, consumer) -> {
					CommandSource.suggestIdentifiers(ScatteredShardsAPI.getServerLibrary().shards().streamKeys(), builder);
					return builder.buildFuture();
				});

				int i = reader.getReader().getCursor();
				reader.setLocalWorldOnly();
				Identifier id = Identifier.fromCommandInput(reader.getReader());
				Optional<Shard> shard = ScatteredShardsAPI.getServerLibrary().shards().get(id);
				if (shard.isEmpty()) {
					reader.getReader().setCursor(i);
					throw scards$UNKNOWN_ID.create(id);
				}

				((ShardArgument) reader).setHasShard(true);
				reader.addPredicate(
					entity -> entity instanceof ServerPlayerEntity player && ScatteredShardsAPI.getServerCollection(player).contains(id)
				);
			}, reader -> !((ShardArgument) reader).selectsByShard(), Text.translatable("argument.scattered_shards.entity.options.has_shard.description"));
		}
	}
}
