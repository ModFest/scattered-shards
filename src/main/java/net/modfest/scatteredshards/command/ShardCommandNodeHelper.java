package net.modfest.scatteredshards.command;

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
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.shard.ShardType;

import java.util.concurrent.CompletableFuture;

public class ShardCommandNodeHelper {
	public static LiteralArgumentBuilder<CommandSourceStack> literal(String name) {
		return LiteralArgumentBuilder.literal(name);
	}

	public static RequiredArgumentBuilder<CommandSourceStack, Identifier> identifier(String name) {
		return RequiredArgumentBuilder.argument(name, IdentifierArgument.id());
	}

	/**
	 * Returns a builder for an Identifier node which autocompletes shards from the global shard library.
	 *
	 * @param name The name of the node
	 * @return A node builder for further modification
	 */
	public static RequiredArgumentBuilder<CommandSourceStack, Identifier> shardId(String name) {
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
	 *
	 * @param name The name of the node
	 * @return A node builder for further modification
	 */
	public static RequiredArgumentBuilder<CommandSourceStack, Identifier> collectedShardId(String name) {
		return identifier(name).suggests((ctx, builder) -> {
			ServerPlayer player = ctx.getSource().getPlayer();
			if (player == null) return builder.buildFuture();

			String prefix = builder.getRemaining();
			for (Identifier id : ScatteredShardsAPI.getServerCollection(player)) {
				if (prefix.isBlank() || id.toString().startsWith(prefix)) builder.suggest(id.toString());
			}

			return builder.buildFuture();
		});
	}

	public static RequiredArgumentBuilder<CommandSourceStack, EntitySelector> players(String name) {
		return RequiredArgumentBuilder.argument(name, EntityArgument.players());
	}

	public static RequiredArgumentBuilder<CommandSourceStack, Float> floatValue(String name) {
		return RequiredArgumentBuilder.argument(name, FloatArgumentType.floatArg());
	}

	public static RequiredArgumentBuilder<CommandSourceStack, Boolean> booleanValue(String name) {
		return RequiredArgumentBuilder.argument(name, BoolArgumentType.bool());
	}

	public static RequiredArgumentBuilder<CommandSourceStack, String> stringArgument(String name) {
		return RequiredArgumentBuilder.argument(name, StringArgumentType.string());
	}

	public static RequiredArgumentBuilder<CommandSourceStack, Identifier> identifierArgument(String name) {
		return RequiredArgumentBuilder.argument(name, IdentifierArgument.id());
	}

	/**
	 * Creates literal nodes as necessary to extend a command path to include the desired command node, and returns the
	 * node. If the node already exists, just find and return it.
	 *
	 * @param root The root - either the root Brigadier node or a subcommand node to start the search from. Either way,
	 *             the first element in `path` will correspond to a *child* of this node.
	 * @param path The desired path to follow or create.
	 * @return The node corresponding to the final element of path. If path is zero-length, root is returned.
	 */
	public CommandNode<CommandSourceStack> getOrCreate(CommandNode<CommandSourceStack> root, String... path) {
		CommandNode<CommandSourceStack> cur = root;
		for (String pathElement : path) {
			CommandNode<CommandSourceStack> maybeChild = cur.getChild(pathElement);
			if (maybeChild == null) {
				maybeChild = literal(pathElement).build();
				cur.addChild(maybeChild);
			}
			cur = maybeChild;
		}

		return cur;
	}

	public static CompletableFuture<Suggestions> suggestModIds(CommandContext<?> context, SuggestionsBuilder builder) {
		for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			builder.suggest(mod.getMetadata().getId());
		}
		return builder.buildFuture();
	}

	public static CompletableFuture<Suggestions> suggestShardTypes(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		ScatteredShardsAPI.getServerLibrary().shardTypes().forEach((id, shardSet) -> {
			if (!id.equals(ShardType.MISSING_ID)) {
				builder.suggest(id.toString());
			}
		});
		return builder.buildFuture();
	}
}
