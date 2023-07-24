package net.modfest.scatteredshards.client.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.client.screen.ShardCreatorGuiDescription;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import org.quiltmc.qsl.command.api.client.ClientCommandRegistrationCallback;
import org.quiltmc.qsl.command.api.client.QuiltClientCommandSource;

import java.util.concurrent.CompletableFuture;

import static org.quiltmc.qsl.command.api.client.ClientCommandManager.argument;
import static org.quiltmc.qsl.command.api.client.ClientCommandManager.literal;

public class ShardCommand {

	public static final DynamicCommandExceptionType INVALID_ID = new DynamicCommandExceptionType(
			id -> Text.translatable("error.scattered_shards.invalid_set_id", id)
	);

	public static int view(CommandContext<QuiltClientCommandSource> context) throws CommandSyntaxException {
		Identifier id = context.getArgument("set_id", Identifier.class);
		var shards = ScatteredShardsAPI.getShardSets().get(id);
		if (shards.isEmpty()) {
			throw INVALID_ID.create(id);
		}
		return Command.SINGLE_SUCCESS;
	}

	public static int creator(CommandContext<QuiltClientCommandSource> context) throws CommandSyntaxException {
		var client = context.getSource().getClient();
		var shard = new Shard(
				ShardType.SECRET,
				Text.literal("My Shard"),
				Text.literal("What is this text for?"),
				Text.literal("What is this secret?"),
				//Either.right(ScatteredShards.id("icon.png"))
				Either.left(Items.DIAMOND_SWORD.getDefaultStack())
		);
		client.send(() -> client.setScreen(new ShardCreatorGuiDescription.Screen(shard)));
		return Command.SINGLE_SUCCESS;
	}

	public static CompletableFuture<Suggestions> suggestShardSets(CommandContext<QuiltClientCommandSource> context, SuggestionsBuilder builder) {
		for (var id : ScatteredShardsAPI.getShardSets().keySet()) {
			builder.suggest(id.toString());
		}
		return builder.buildFuture();
	}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) ->
				dispatcher.register(literal("shard")
						.then(literal("view")
								.then(argument("set_id", IdentifierArgumentType.identifier())
										.suggests(ShardCommand::suggestShardSets)
										.executes(ShardCommand::view)))
						.then(literal("creator")
								.executes(ShardCommand::creator))));
	}
}
