package net.modfest.scatteredshards;

import java.util.concurrent.CompletableFuture;

import org.quiltmc.qsl.command.api.CommandRegistrationCallback;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.component.ScatteredShardsComponents;
import net.modfest.scatteredshards.component.ShardLibraryComponent;

public class ShardCommand {
	
	public static int collect(CommandContext<ServerCommandSource> ctx) {
		Identifier id = ctx.getArgument("shard_id", Identifier.class);
		
		ShardLibraryComponent library = ScatteredShardsComponents.getShardLibrary(ctx);
		
		Shard shard = library.getShard(id);
		if (shard == Shard.MISSING_SHARD) {
			ctx.getSource().sendError(Text.literal("Unknown shard: "+id.toString()));
			return -1;
		} else {
			if (ctx.getSource().getEntity() instanceof ServerPlayerEntity player) {
				ctx.getSource().sendFeedback(()->Text.literal("Collecting shard '"+id+"'"), false);
				
				ScatteredShardsComponents.getShardCollection(player).addShard(id);
			}
			
			return Command.SINGLE_SUCCESS;
		}
	}
	
	public static int uncollect(CommandContext<ServerCommandSource> ctx) {
		Identifier id = ctx.getArgument("shard_id", Identifier.class);
		
		if (ctx.getSource().getEntity() instanceof ServerPlayerEntity player) {
			ctx.getSource().sendFeedback(()->Text.literal("Un-Collecting shard '"+id+"'"), false);
			
			ScatteredShardsComponents.getShardCollection(player).removeShard(id);
			return Command.SINGLE_SUCCESS;
		} else {
			ctx.getSource().sendError(Text.literal("Non-players can't uncollect shards."));
			return -1;
		}
	}
	
	public static CompletableFuture<Suggestions> suggestShardIds(CommandContext<ServerCommandSource> source, SuggestionsBuilder builder) {
		for(Identifier id : ScatteredShardsComponents.getShardLibrary(source).getShardIds()) {
			builder.suggest(id.toString());
		}
		return builder.buildFuture();
	}
	
	private static LiteralCommandNode<ServerCommandSource> literal(String name) {
		return LiteralArgumentBuilder.<ServerCommandSource>literal(name).build();
	}
	
	@SuppressWarnings("unused")
	private static LiteralCommandNode<ServerCommandSource> literal(String name, Command<ServerCommandSource> command) {
		return LiteralArgumentBuilder.<ServerCommandSource>literal(name).executes(command).build();
	}
	
	private static RequiredArgumentBuilder<ServerCommandSource, Identifier> identifierArgument(String name) {
		return RequiredArgumentBuilder.<ServerCommandSource, Identifier>argument(name, IdentifierArgumentType.identifier());
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
					.build();
			collectCommand.addChild(collectIdArgument);
			shardRoot.addChild(collectCommand);
			
			//Usage: /shard uncollect <shard_id>
			var uncollectCommand = literal("uncollect");
			var uncollectIdArgument = identifierArgument("shard_id")
					.suggests(ShardCommand::suggestShardIds)
					.executes(ShardCommand::uncollect)
					.build();
			uncollectCommand.addChild(uncollectIdArgument);
			shardRoot.addChild(uncollectCommand);
			
		});
	}
}
