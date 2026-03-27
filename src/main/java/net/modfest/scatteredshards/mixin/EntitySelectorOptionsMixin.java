package net.modfest.scatteredshards.mixin;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
		shardId -> Component.translatableEscape("argument.scattered_shards.entity.options.has_shard.invalid", shardId)
	);

	// stolen with permission from fireblanket
	@Shadow
	private static void register(String id, EntitySelectorOptions.Modifier handler, Predicate<EntitySelectorParser> condition, Component description) {
		throw new IllegalStateException("Unimplemented mixin");
	}

	@SuppressWarnings("rawtypes")
	@Shadow
	@Final
	private static Map OPTIONS;

	@Inject(method = "bootStrap", at = @At("TAIL"))
	private static void injectShard(CallbackInfo info) {
		if (!OPTIONS.containsKey("has_shard")) {
			register("has_shard", reader -> {
				reader.setSuggestions((builder, consumer) -> {
					SharedSuggestionProvider.suggestResource(ScatteredShardsAPI.getServerLibrary().shards().streamKeys(), builder);
					return builder.buildFuture();
				});

				int i = reader.getReader().getCursor();
				reader.setWorldLimited();
				Identifier id = Identifier.read(reader.getReader());
				Optional<Shard> shard = ScatteredShardsAPI.getServerLibrary().shards().get(id);
				if (shard.isEmpty()) {
					reader.getReader().setCursor(i);
					throw scards$UNKNOWN_ID.create(id);
				}

				((ShardArgument) reader).setHasShard(true);
				reader.addPredicate(
					entity -> entity instanceof ServerPlayer player && ScatteredShardsAPI.getServerCollection(player).contains(id)
				);
			}, reader -> !((ShardArgument) reader).selectsByShard(), Component.translatable("argument.scattered_shards.entity.options.has_shard.description"));
		}
	}
}
