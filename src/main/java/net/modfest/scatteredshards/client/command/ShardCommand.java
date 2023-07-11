package net.modfest.scatteredshards.client.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.client.screen.ShardCreatorGuiDescription;
import org.quiltmc.qsl.command.api.client.ClientCommandRegistrationCallback;
import org.quiltmc.qsl.command.api.client.QuiltClientCommandSource;

import java.util.concurrent.CompletableFuture;

import static org.quiltmc.qsl.command.api.client.ClientCommandManager.argument;
import static org.quiltmc.qsl.command.api.client.ClientCommandManager.literal;

public class ShardCommand {

	public static int view(CommandContext<QuiltClientCommandSource> context) throws CommandSyntaxException {
		Identifier id = context.getArgument("set_id", Identifier.class);
		var client = context.getSource().getClient();
		client.send(() -> client.setScreen(new ShardCreatorGuiDescription.Screen()));
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
										.executes(ShardCommand::view)))));
	}
}
