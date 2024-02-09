package net.modfest.scatteredshards.command;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.shard.ShardType;

public class Node {
	public static LiteralArgumentBuilder<ServerCommandSource> literal(String name) {
		return LiteralArgumentBuilder.<ServerCommandSource>literal(name);
	}
	
	public static RequiredArgumentBuilder<ServerCommandSource, Identifier> identifier(String name) {
		return RequiredArgumentBuilder.<ServerCommandSource, Identifier>argument(name, IdentifierArgumentType.identifier());
	}
	
	/**
	 * Returns a builder for an Identifier node which autocompletes shards from the global shard library.
	 * @param name The name of the node
	 * @return A node builder for further modification
	 */
	public static RequiredArgumentBuilder<ServerCommandSource, Identifier> shardId(String name) {
		return identifier(name).suggests((source, builder) -> {
			String prefix = builder.getRemaining();
			ScatteredShardsAPI.getServerLibrary().shards().forEach((id, shard) -> {
				if (prefix.isBlank() || id.toString().startsWith(prefix)) builder.suggest(id.toString());
			});
			return builder.buildFuture();
		});
	}
	
	/**
	 * Returns a builder for an Identifier node which autocompletes shards *from the player's collection*.
	 * @param name The name of the node
	 * @return A node builder for further modification
	 */
	public static RequiredArgumentBuilder<ServerCommandSource, Identifier> collectedShardId(String name) {
		return identifier(name).suggests((ctx, builder) -> {
			ServerPlayerEntity player = ctx.getSource().getPlayer();
			if (player == null) return builder.buildFuture();
			
			String prefix = builder.getRemaining();
			for(Identifier id : ScatteredShardsAPI.getServerCollection(player)) {
				if (prefix.isBlank() || id.toString().startsWith(prefix)) builder.suggest(id.toString());
			}
			
			return builder.buildFuture();
		});
	}
	
	public static RequiredArgumentBuilder<ServerCommandSource, EntitySelector> players(String name) {
		return RequiredArgumentBuilder.<ServerCommandSource, EntitySelector>argument(name, EntityArgumentType.players());
	}
	
	public static RequiredArgumentBuilder<ServerCommandSource, Float> floatValue(String name) {
		return RequiredArgumentBuilder.argument(name, FloatArgumentType.floatArg());
	}
	
	public static RequiredArgumentBuilder<ServerCommandSource, Boolean> booleanValue(String name) {
		return RequiredArgumentBuilder.argument(name, BoolArgumentType.bool());
	}

	public static RequiredArgumentBuilder<ServerCommandSource, String> stringArgument(String name) {
		return RequiredArgumentBuilder.<ServerCommandSource, String>argument(name, StringArgumentType.string());
	}
	
	/**
	 * Creates literal nodes as necessary to extend a command path to include the desired command node, and returns the
	 * node. If the node already exists, just find and return it.
	 * @param root The root - either the root Brigadier node or a subcommand node to start the search from. Either way,
	 *             the first element in `path` will correspond to a *child* of this node.
	 * @param path The desired path to follow or create.
	 * @return The node corresponding to the final element of path. If path is zero-length, root is returned.
	 */
	public CommandNode<ServerCommandSource> getOrCreate(CommandNode<ServerCommandSource> root, String... path) {
		CommandNode<ServerCommandSource> cur = root;
		for(String pathElement : path) {
			var maybeChild = cur.getChild(pathElement);
			if (maybeChild == null) {
				maybeChild = literal(pathElement).build();
				cur.addChild(maybeChild);
			}
			cur = maybeChild;
		}
		
		return cur;
	}

	public static CompletableFuture<Suggestions> suggestModIds(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
		for (var mod : FabricLoader.getInstance().getAllMods()) {
			builder.suggest(mod.getMetadata().getId());
		}
		return builder.buildFuture();
	}

	public static CompletableFuture<Suggestions> suggestShardTypes(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
		ScatteredShardsAPI.getServerLibrary().shardTypes().forEach((id, shardSet) -> {
			if (!id.equals(ShardType.MISSING_ID)) {
				builder.suggest(id.toString());
			}
		});
		return builder.buildFuture();
	}
}
