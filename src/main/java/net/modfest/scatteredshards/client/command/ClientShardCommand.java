package net.modfest.scatteredshards.client.command;

import com.google.common.collect.SetMultimap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.MiniRegistry;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardCollection;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.impl.ShardLibraryImpl;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.client.screen.ShardCreatorGuiDescription;
import net.modfest.scatteredshards.client.screen.ShardTabletGuiDescription;
import net.modfest.scatteredshards.command.ShardCommand;
import net.modfest.scatteredshards.command.ShardCommandNodeHelper;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("SameParameterValue")
public class ClientShardCommand {

	private static final DynamicCommandExceptionType INVALID_SET_ID = new DynamicCommandExceptionType(
		obj -> Text.stringifiedTranslatable("error.scattered_shards.invalid_set_id", obj)
	);
	private static final DynamicCommandExceptionType INVALID_SHARD_ID = new DynamicCommandExceptionType(
		obj -> Text.stringifiedTranslatable("error.scattered_shards.invalid_shard_id", obj)
	);

	public static int view(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		Identifier id = context.getArgument("set_id", Identifier.class);
		ShardLibrary realLibrary = ScatteredShardsAPI.getClientLibrary();
		Set<Identifier> shardPackSet = realLibrary.shardSets().get(id);
		if (shardPackSet.isEmpty()) {
			throw INVALID_SET_ID.create(id);
		}
		ShardCollection shardCollection = ScatteredShardsAPI.getClientCollection();
		ShardLibrary fakeLibrary = new ShardLibraryImpl();
		MiniRegistry<Shard> realShardRegistry = realLibrary.shards();
		MiniRegistry<Shard> fakeShardRegistry = fakeLibrary.shards();
		SetMultimap<Identifier, Identifier> fakeShardSets = fakeLibrary.shardSets();
		MiniRegistry<ShardType> fakeShardTypes = fakeLibrary.shardTypes();
		for (Identifier shardId : shardPackSet) {
			Optional<Shard> optionalShard = realShardRegistry.get(shardId);
			if (optionalShard.isEmpty()) continue;
			Shard shard = optionalShard.get();
			fakeShardRegistry.put(shardId, shard);
			fakeShardSets.put(shard.sourceId(), shardId);
		}
		realLibrary.shardTypes().forEach((fakeShardTypes::put));
		fakeLibrary.shardDisplaySettings().copyFrom(realLibrary.shardDisplaySettings());
		context.getSource().getClient().send(() -> context.getSource().getClient().setScreen(new ShardTabletGuiDescription.Screen(shardCollection, fakeLibrary)));
		return Command.SINGLE_SUCCESS;
	}

	public static int creatorNew(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		String modId = StringArgumentType.getString(context, "mod_id");
		Identifier shardTypeId = context.getArgument("shard_type", Identifier.class);
		ShardType shardType = ScatteredShardsAPI.getClientLibrary().shardTypes().get(shardTypeId)
			.orElseThrow(() -> ShardCommand.INVALID_SHARD_TYPE.create(shardTypeId));

		context.getSource().getClient().send(() -> context.getSource().getClient().setScreen(ShardCreatorGuiDescription.Screen.newShard(modId, shardType)));
		return Command.SINGLE_SUCCESS;
	}

	public static int creatorEdit(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		Identifier shardId = context.getArgument("shard_id", Identifier.class);
		Shard shard = ScatteredShardsAPI.getClientLibrary().shards().get(shardId)
			.orElseThrow(() -> INVALID_SHARD_ID.create(shardId));

		context.getSource().getClient().send(() -> context.getSource().getClient().setScreen(ShardCreatorGuiDescription.Screen.editShard(shard)));
		return Command.SINGLE_SUCCESS;
	}

	public static int shards(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ShardLibrary library = ScatteredShardsAPI.getClientLibrary();
		ShardCollection collection = ScatteredShardsAPI.getClientCollection();
		context.getSource().getClient().send(() -> context.getSource().getClient().setScreen(new ShardTabletGuiDescription.Screen(collection, library)));
		return Command.SINGLE_SUCCESS;
	}

	private static CompletableFuture<Suggestions> suggestShardSets(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
		for (Identifier id : ScatteredShardsAPI.getClientLibrary().shardSets().keySet()) {
			builder.suggest(id.toString());
		}
		return builder.buildFuture();
	}

	private static CompletableFuture<Suggestions> suggestShards(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
		ScatteredShardsAPI.getClientLibrary().shards().forEach((id, shard) -> builder.suggest(id.toString()));
		return builder.buildFuture();
	}

	static CompletableFuture<Suggestions> suggestShardTypes(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
		ScatteredShardsAPI.getClientLibrary().shardTypes().forEach((id, shardSet) -> {
			if (!id.equals(ShardType.MISSING_ID)) {
				builder.suggest(id.toString());
			}
		});
		return builder.buildFuture();
	}

	static LiteralArgumentBuilder<FabricClientCommandSource> literal(String name) {
		return LiteralArgumentBuilder.literal(name);
	}

	static RequiredArgumentBuilder<FabricClientCommandSource, Identifier> identifierArgument(String name) {
		return RequiredArgumentBuilder.argument(name, IdentifierArgumentType.identifier());
	}

	static RequiredArgumentBuilder<FabricClientCommandSource, String> stringArgument(String name) {
		return RequiredArgumentBuilder.argument(name, StringArgumentType.string());
	}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			CommandNode<FabricClientCommandSource> shardcNode = literal("shardc").build();

			//Usage: /shardc view <set_id>
			CommandNode<FabricClientCommandSource> view = literal("view").build();
			CommandNode<FabricClientCommandSource> setId = identifierArgument("set_id")
				.suggests(ClientShardCommand::suggestShardSets)
				.executes(ClientShardCommand::view).build();

			//Usage: /shardc creator
			//-> new <mod_id> <shard_type>
			//-> edit <shard_id>
			CommandNode<FabricClientCommandSource> creator = literal("creator").requires((source) -> source.getPlayer().hasPermissionLevel(2)).build();
			CommandNode<FabricClientCommandSource> creatorNew = literal("new").build();
			CommandNode<FabricClientCommandSource> modId = stringArgument("mod_id")
				.suggests(ShardCommandNodeHelper::suggestModIds)
				.build();
			CommandNode<FabricClientCommandSource> shardType = identifierArgument("shard_type")
				.suggests(ClientShardCommand::suggestShardTypes)
				.executes(ClientShardCommand::creatorNew)
				.build();

			CommandNode<FabricClientCommandSource> creatorEdit = literal("edit").build();
			CommandNode<FabricClientCommandSource> shardId = identifierArgument("shard_id")
				.suggests(ClientShardCommand::suggestShards)
				.executes(ClientShardCommand::creatorEdit).build();

			//Usage: /shards
			CommandNode<FabricClientCommandSource> shardsCommand = literal("shards").executes(ClientShardCommand::shards).build();

			dispatcher.getRoot().addChild(shardcNode);
			shardcNode.addChild(view);
			view.addChild(setId);
			shardcNode.addChild(creator);
			creator.addChild(creatorNew);
			creatorNew.addChild(modId);
			modId.addChild(shardType);
			creator.addChild(creatorEdit);
			creatorEdit.addChild(shardId);

			CreateInstantCommand.register(creator);

			dispatcher.getRoot().addChild(shardsCommand);
		});
	}
}
