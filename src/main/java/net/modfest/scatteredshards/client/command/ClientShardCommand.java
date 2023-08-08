package net.modfest.scatteredshards.client.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.client.screen.ShardCreatorGuiDescription;
import net.modfest.scatteredshards.client.screen.ShardTabletGuiDescription;
import net.modfest.scatteredshards.component.ScatteredShardsComponents;
import org.quiltmc.qsl.command.api.client.ClientCommandRegistrationCallback;
import org.quiltmc.qsl.command.api.client.QuiltClientCommandSource;

import java.util.concurrent.CompletableFuture;

public class ClientShardCommand {

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
		client.send(() -> client.setScreen(new ShardCreatorGuiDescription.Screen()));
		return Command.SINGLE_SUCCESS;
	}

	public static int shards(CommandContext<QuiltClientCommandSource> context) throws CommandSyntaxException {
		var client = context.getSource().getClient();
		var collection = ScatteredShardsComponents.getShardCollection(context.getSource().getPlayer());
		var library = ScatteredShardsComponents.getShardLibrary(context.getSource().getWorld());

		client.send(() -> client.setScreen(new ShardTabletGuiDescription.Screen(collection, library)));

		return Command.SINGLE_SUCCESS;
	}

	public static CompletableFuture<Suggestions> suggestShardSets(CommandContext<QuiltClientCommandSource> context, SuggestionsBuilder builder) {
		for (var id : ScatteredShardsAPI.getShardSets().keySet()) {
			builder.suggest(id.toString());
		}
		return builder.buildFuture();
	}

	private static LiteralCommandNode<QuiltClientCommandSource> literal(String name) {
		return LiteralArgumentBuilder.<QuiltClientCommandSource>literal(name).build();
	}

	private static LiteralCommandNode<QuiltClientCommandSource> literal(String name, Command<QuiltClientCommandSource> command) {
		return LiteralArgumentBuilder.<QuiltClientCommandSource>literal(name).executes(command).build();
	}

	private static RequiredArgumentBuilder<QuiltClientCommandSource, Identifier> identifierArgument(String name) {
		return RequiredArgumentBuilder.<QuiltClientCommandSource, Identifier>argument(name, IdentifierArgumentType.identifier());
	}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) -> {

			var shardcRoot = literal("shardc");
			dispatcher.getRoot().addChild(shardcRoot);

			//Usage: /shardc view <set_id>
			var view = literal("view");
			var setId = identifierArgument("set_id")
					.suggests(ClientShardCommand::suggestShardSets)
					.executes(ClientShardCommand::view);
			view.addChild(setId.build());
			shardcRoot.addChild(view);

			//Usage: /shardc creator
			var creator = literal("creator", ClientShardCommand::creator);
			shardcRoot.addChild(creator);

			//Usage: /shards
			var shardsCommand = literal("shards", ClientShardCommand::shards);
			dispatcher.getRoot().addChild(shardsCommand);
		});
	}
}
