package net.modfest.scatteredshards.client.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
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
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.client.screen.ShardCreatorGuiDescription;
import net.modfest.scatteredshards.client.screen.ShardTabletGuiDescription;
import net.modfest.scatteredshards.component.ScatteredShardsComponents;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.command.api.client.ClientCommandRegistrationCallback;
import org.quiltmc.qsl.command.api.client.QuiltClientCommandSource;

import java.util.concurrent.CompletableFuture;

public class ClientShardCommand {

	private static DynamicCommandExceptionType createInvalidException(String item) {
		return new DynamicCommandExceptionType(
				obj -> Text.translatable("error.scattered_shards.invalid_" + item, obj)
		);
	}

	private static final DynamicCommandExceptionType INVALID_SET_ID = createInvalidException("set_id");
	private static final DynamicCommandExceptionType INVALID_MOD_ID = createInvalidException("mod_id");
	private static final DynamicCommandExceptionType INVALID_SHARD_TYPE = createInvalidException("shard_type");
	private static final DynamicCommandExceptionType INVALID_SHARD_ID = createInvalidException("shard_id");

	public static int view(CommandContext<QuiltClientCommandSource> context) throws CommandSyntaxException {
		Identifier id = context.getArgument("set_id", Identifier.class);
		var shards = ScatteredShardsAPI.getShardSets().get(id);
		if (shards.isEmpty()) {
			throw INVALID_SET_ID.create(id);
		}
		return Command.SINGLE_SUCCESS;
	}

	public static int creatorNew(CommandContext<QuiltClientCommandSource> context) throws CommandSyntaxException {
		String modId = StringArgumentType.getString(context, "mod_id");
		//if (!QuiltLoader.isModLoaded(modId)) {
		//	throw INVALID_MOD_ID.create(modId);
		//}
		Identifier shardTypeId = context.getArgument("shard_type", Identifier.class);
		ShardType shardType = ScatteredShardsAPI.getShardTypes().get(shardTypeId);
		if (shardType == null) {
			throw INVALID_SHARD_TYPE.create(shardTypeId);
		}
		var client = context.getSource().getClient();
		client.send(() -> client.setScreen(ShardCreatorGuiDescription.Screen.newShard(modId, shardType)));
		return Command.SINGLE_SUCCESS;
	}

	public static int creatorEdit(CommandContext<QuiltClientCommandSource> context) throws CommandSyntaxException {
		Identifier shardId = context.getArgument("shard_id", Identifier.class);
		Shard shard = ScatteredShardsComponents.getShardLibrary(context.getSource().getWorld()).getShard(shardId);
		if (shard == Shard.MISSING_SHARD) {
			throw INVALID_SHARD_ID.create(shardId);
		}
		var client = context.getSource().getClient();
		client.send(() -> client.setScreen(ShardCreatorGuiDescription.Screen.editShard(shard)));
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

	public static CompletableFuture<Suggestions> suggestShards(CommandContext<QuiltClientCommandSource> context, SuggestionsBuilder builder) {
		for (var id : ScatteredShardsComponents.getShardLibrary(context.getSource().getWorld()).getShardIds()) {
			builder.suggest(id.toString());
		}
		return builder.buildFuture();
	}

	public static CompletableFuture<Suggestions> suggestShardTypes(CommandContext<QuiltClientCommandSource> context, SuggestionsBuilder builder) {
		for (var id : ScatteredShardsAPI.getShardTypes().keySet()) {
			builder.suggest(id.toString());
		}
		return builder.buildFuture();
	}
	
	public static CompletableFuture<Suggestions> suggestModIds(CommandContext<QuiltClientCommandSource> context, SuggestionsBuilder builder) {
		for (var mod : QuiltLoader.getAllMods()) {
			builder.suggest(mod.metadata().id());
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
	
	private static RequiredArgumentBuilder<QuiltClientCommandSource, String> stringArgument(String name) {
		return RequiredArgumentBuilder.<QuiltClientCommandSource, String>argument(name, StringArgumentType.string());
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
				//-> new <mod_id> <shard_type>
				//-> edit <shard_id>
			var creator = literal("creator");

			var creatorNew = literal("new");
			var modId = stringArgument("mod_id")
					.suggests(ClientShardCommand::suggestModIds);
			var modIdBuild = modId.build();
			var shardType = identifierArgument("shard_type")
					.suggests(ClientShardCommand::suggestShardTypes)
					.executes(ClientShardCommand::creatorNew);
			modIdBuild.addChild(shardType.build());
			creatorNew.addChild(modIdBuild);

			var creatorEdit = literal("edit");
			var shardId = identifierArgument("shard_id")
					.suggests(ClientShardCommand::suggestShards)
					.executes(ClientShardCommand::creatorEdit);
			creatorEdit.addChild(shardId.build());

			creator.addChild(creatorNew);
			creator.addChild(creatorEdit);

			shardcRoot.addChild(creator);

			//Usage: /shards
			var shardsCommand = literal("shards", ClientShardCommand::shards);
			dispatcher.getRoot().addChild(shardsCommand);
		});
	}
}
