package net.modfest.scatteredshards;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.block.ShardBlock;
import net.modfest.scatteredshards.component.ScatteredShardsComponents;
import net.modfest.scatteredshards.component.ShardCollectionComponent;
import net.modfest.scatteredshards.component.ShardLibraryComponent;

public class ShardCommand {
	
	public static final DynamicCommandExceptionType INVALID_SHARD = new DynamicCommandExceptionType(
			it -> Text.translatable("error.scattered_shards.invalid_shard_id", it)
			);
	
	public static final DynamicCommandExceptionType NO_ROOM_FOR_ITEM = new DynamicCommandExceptionType(
			it -> Text.translatable("error.scattered_shards.no_inventory_room", it)
			);
	
	public static int collect(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		Identifier id = ctx.getArgument("shard_id", Identifier.class);
		
		ShardLibraryComponent library = ScatteredShardsComponents.getShardLibrary(ctx);
		
		Shard shard = library.getShard(id);
		if (shard == Shard.MISSING_SHARD) throw INVALID_SHARD.create(id);
		
		ScatteredShardsComponents.getShardCollection(ctx.getSource().getPlayer()).addShard(id);
		
		ctx.getSource().sendFeedback(() -> Text.translatable("commands.scattered_shards.shard.collect", id), false);
		
		return Command.SINGLE_SUCCESS;
	}
	
	public static int award(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		EntitySelector target = ctx.getArgument("players", EntitySelector.class);
		Identifier shardId = ctx.getArgument("shard_id", Identifier.class);
		
		ShardLibraryComponent library = ScatteredShardsComponents.getShardLibrary(ctx);
		Shard shard = library.getShard(shardId);
		if (shard == Shard.MISSING_SHARD) throw INVALID_SHARD.create(shardId);
		
		int i = 0;
		for(ServerPlayerEntity player : target.getPlayers(ctx.getSource())) {
			ShardCollectionComponent collection = ScatteredShardsComponents.getShardCollection(player);
			if (collection.addShard(shardId)) {
				i++;
			}
		}
		final int collected = i;
		
		if (collected == 0) {
			ctx.getSource().sendFeedback(() -> Text.translatable("commands.scattered_shards.shard.award.none", shardId), false);
		} else {
			ctx.getSource().sendFeedback(() -> Text.translatable("commands.scattered_shards.shard.award", shardId, collected), false);
		}
		
		return collected;
	}

	public static int blockCommand(CommandContext<ServerCommandSource> ctx, boolean options) throws CommandSyntaxException {
		ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
		Identifier shardId = ctx.getArgument("shard_id", Identifier.class);

		ShardLibraryComponent library = ScatteredShardsComponents.getShardLibrary(ctx);

		boolean canInteract = options && BoolArgumentType.getBool(ctx, "can_interact");
		float glowSize = options ? FloatArgumentType.getFloat(ctx, "glow_size") : 0.5f;
		float glowStrength = options ? FloatArgumentType.getFloat(ctx, "glow_strength") : 0.5f;

		ItemStack stack = ShardBlock.createShardBlock(library, shardId, canInteract, glowSize, glowStrength);

		if (player.giveItemStack(stack)) {
			ctx.getSource().sendFeedback(() -> Text.translatable("commands.scattered_shards.shard.block", shardId), false);
			return Command.SINGLE_SUCCESS;
		} else {
			throw NO_ROOM_FOR_ITEM.create(ScatteredShardsContent.SHARD_BLOCK_ITEM.getName());
		}
	}
	
	public static int block(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		return blockCommand(ctx, false);
	}

	public static int blockOptions(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		return blockCommand(ctx, true);
	}
	
	public static int uncollect(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		Identifier id = ctx.getArgument("shard_id", Identifier.class);
		ScatteredShardsComponents.getShardCollection(ctx.getSource().getPlayer()).removeShard(id);
		ctx.getSource().sendFeedback(() -> Text.translatable("commands.scattered_shards.shard.uncollect", id), false);
		return Command.SINGLE_SUCCESS;
	}
	
	public static int uncollectAll(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ShardCollectionComponent collection = ScatteredShardsComponents.getShardCollection(ctx.getSource().getPlayer());
		int shardsToDelete = collection.size();
		collection.clear();
		ctx.getSource().sendFeedback(() -> Text.translatable("commands.scattered_shards.shard.uncollect.all", shardsToDelete), false);

		return shardsToDelete;
	}
	
	public static int delete(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		Identifier shardId = ctx.getArgument("shard_id", Identifier.class);
		
		ShardLibraryComponent library = ScatteredShardsComponents.getShardLibrary(ctx);
		if (!library.contains(shardId)) throw INVALID_SHARD.create(shardId);
		
		library.deleteShard(shardId, ctx.getSource().getWorld(), ctx.getSource());
		
		ctx.getSource().sendFeedback(() -> Text.translatable("commands.scattered_shards.shard.library.delete", shardId), true);
		
		return Command.SINGLE_SUCCESS;
	}
	
	public static int deleteAll(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ShardLibraryComponent library = ScatteredShardsComponents.getShardLibrary(ctx);
		int toDelete = library.size();
		library.clear(ctx.getSource().getWorld());
		
		ctx.getSource().sendFeedback(() -> Text.translatable("commands.scattered_shards.shard.library.delete.all", toDelete), true);
		
		return toDelete;
	}
	
	public static CompletableFuture<Suggestions> suggestShardIds(CommandContext<ServerCommandSource> source, SuggestionsBuilder builder) {
		String prefix = builder.getRemaining();
		for(Identifier id : ScatteredShardsComponents.getShardLibrary(source).getShardIds()) {
			if (prefix == "" || id.toString().startsWith(prefix)) builder.suggest(id.toString());
		}
		return builder.buildFuture();
	}
	
	public static CompletableFuture<Suggestions> suggestShardIdsAndAll(CommandContext<ServerCommandSource> source, SuggestionsBuilder builder) {
		String prefix = builder.getRemaining();
		if (prefix == "" || "all".startsWith(prefix)) builder.suggest("all");
		for(Identifier id : ScatteredShardsComponents.getShardLibrary(source).getShardIds()) {
			if (prefix == "" || id.toString().startsWith(prefix)) builder.suggest(id.toString());
		}
		return builder.buildFuture();
	}
	
	private static LiteralCommandNode<ServerCommandSource> literal(String name) {
		return LiteralArgumentBuilder.<ServerCommandSource>literal(name).build();
	}

	private static RequiredArgumentBuilder<ServerCommandSource, Boolean> boolArgument(String name) {
		return RequiredArgumentBuilder.argument(name, BoolArgumentType.bool());
	}

	private static RequiredArgumentBuilder<ServerCommandSource, Float> floatArgument(String name) {
		return RequiredArgumentBuilder.argument(name, FloatArgumentType.floatArg());
	}
	
	private static RequiredArgumentBuilder<ServerCommandSource, Identifier> identifierArgument(String name) {
		return RequiredArgumentBuilder.<ServerCommandSource, Identifier>argument(name, IdentifierArgumentType.identifier());
	}
	
	private static RequiredArgumentBuilder<ServerCommandSource, EntitySelector> playersArgument(String name) {
		return RequiredArgumentBuilder.<ServerCommandSource, EntitySelector>argument(name, EntityArgumentType.players());
	}
	
	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, context, environment) -> {
			var shardRoot = literal("shard");
			dispatcher.getRoot().addChild(shardRoot);
			
			//Usage: /shard collect <shard_id>
			var collectCommand = literal("collect");
			var collectIdArgument = identifierArgument("shard_id")
					.suggests(ShardCommand::suggestShardIds)
					.executes(ShardCommand::collect)
					.requires(
						Permissions.require(ScatteredShards.permission("command.collect"), 2)
					)
					.build();
			collectCommand.addChild(collectIdArgument);
			shardRoot.addChild(collectCommand);
			
			//Usage: /shard award <player> <shard_id>
			var awardCommand = literal("award");
			var awardPlayerArgument = playersArgument("players").build();
			var awardIdArgument = identifierArgument("shard_id")
					.suggests(ShardCommand::suggestShardIds)
					.executes(ShardCommand::award)
					.requires(
						Permissions.require(ScatteredShards.permission("command.award"), 2)
					)
					.build();
			awardCommand.addChild(awardPlayerArgument);
			awardPlayerArgument.addChild(awardIdArgument);
			shardRoot.addChild(awardCommand);
			
			//Usage: /shard block <shard_id>
			var blockCommand = literal("block");
			var blockIdArgument = identifierArgument("shard_id")
					.suggests(ShardCommand::suggestShardIds)
					.executes(ShardCommand::block)
					.requires(
						Permissions.require(ScatteredShards.permission("command.block"), 2)
					)
					.build();
			var blockInteractArgument = boolArgument("can_interact").build();
			var blockGlowSizeArgument = floatArgument("glow_size").build();
			var blockGlowStrengthArgument = floatArgument("glow_strength")
					.executes(ShardCommand::blockOptions)
					.requires(
							Permissions.require(ScatteredShards.permission("command.block"), 2)
					)
					.build();
			blockGlowSizeArgument.addChild(blockGlowStrengthArgument);
			blockInteractArgument.addChild(blockGlowSizeArgument);
			blockIdArgument.addChild(blockInteractArgument);
			blockCommand.addChild(blockIdArgument);
			shardRoot.addChild(blockCommand);
			
			//Usage: /shard uncollect <shard_id>
			var uncollectCommand = literal("uncollect");
			var uncollectIdArgument = identifierArgument("shard_id")
					.suggests(ShardCommand::suggestShardIdsAndAll)
					.executes(ShardCommand::uncollect)
					.requires(
						Permissions.require(ScatteredShards.permission("command.uncollect"), 2)
					)
					.build();
			uncollectCommand.addChild(uncollectIdArgument);
			shardRoot.addChild(uncollectCommand);
			
			//Usage: /shard uncollect all
			var uncollectAllCommand = LiteralArgumentBuilder.<ServerCommandSource>literal("all")
					.executes(ShardCommand::uncollectAll)
					.requires(
						Permissions.require(ScatteredShards.permission("command.uncollect.all"), 2)
					)
					.build();
			uncollectCommand.addChild(uncollectAllCommand);
			
			
			//Subcommand: /shard library
			var libraryRoot = literal("library");
			shardRoot.addChild(libraryRoot);
			
			//Usage: /shard library delete <shard_id>
			var deleteCommand = literal("delete");
			var deleteIdArgument = identifierArgument("shard_id")
					.suggests(ShardCommand::suggestShardIdsAndAll)
					.executes(ShardCommand::delete)
					.requires(
						Permissions.require(ScatteredShards.permission("command.delete"), 3)
					)
					.build();
			deleteCommand.addChild(deleteIdArgument);
			libraryRoot.addChild(deleteCommand);
			
			//Usage: /shard library delete all
			var deleteAllCommand = LiteralArgumentBuilder.<ServerCommandSource>literal("all")
					.executes(ShardCommand::deleteAll)
					.requires(
						Permissions.require(ScatteredShards.permission("command.delete.all"), 4)
					)
					.build();
			libraryRoot.addChild(deleteAllCommand);
		});
	}
}
