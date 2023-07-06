package net.modfest.scatteredshards.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;

import java.util.concurrent.CompletableFuture;

public class ShardCommand {

	public static int view(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		Identifier id = IdentifierArgumentType.getIdentifier(context, "set_id");
		context.getSource().sendSystemMessage(Text.literal(ScatteredShardsAPI.getShardSets().get(id).toString()));
		return 0;
	}

	public static CompletableFuture<Suggestions> suggestShardSets(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
		for (var id : ScatteredShardsAPI.getShardSets().keySet()) {
			builder.suggest(id.toString());
		}
		return builder.buildFuture();
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) ->
				dispatcher.register(CommandManager.literal("shard")
						.then(CommandManager.literal("view")
								.then(CommandManager.argument("set_id", IdentifierArgumentType.identifier())
										.suggests(ShardCommand::suggestShardSets)
										.executes(ShardCommand::view)))));
	}
}
