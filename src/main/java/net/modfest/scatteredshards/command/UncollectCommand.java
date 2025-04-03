package net.modfest.scatteredshards.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardCollection;
import net.modfest.scatteredshards.api.impl.ShardCollectionPersistentState;
import net.modfest.scatteredshards.networking.S2CSyncCollection;

public class UncollectCommand {
	/**
	 * Syntax: <code>/shard uncollect &lt;shard_id&gt;</code>
	 * <p>Removes the specified shard from the library / tablet of the person running the command. Must be used by a player.
	 *
	 * @return Always 1 for the shard removed, unless an exception occurs.
	 * @throws CommandSyntaxException if there was a problem executing the command.
	 */
	public static int uncollect(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		Identifier id = ctx.getArgument("shard_id", Identifier.class);

		//Validate shard
		ScatteredShardsAPI.getServerLibrary().shards().get(id)
			.orElseThrow(() -> ShardCommand.INVALID_SHARD.create(id));

		//Validate that source is a player and uncollect it
		ScatteredShardsAPI.triggerShardUncollection(ctx.getSource().getPlayerOrThrow(), id);

		ctx.getSource().sendFeedback(() -> Text.stringifiedTranslatable("commands.scattered_shards.shard.uncollect", id), false);

		return Command.SINGLE_SUCCESS;
	}

	/**
	 * Syntax: <code>/shard uncollect all</code>
	 * <p>Removes all shards from the library / tablet of the person running the command. Must be used by a player.
	 *
	 * @return The number of shards removed. Zero is a valid output from this command (if the collection was empty).
	 * @throws CommandSyntaxException if there was a problem executing the command.
	 */
	public static int uncollectAll(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
		ShardCollection collection = ScatteredShardsAPI.getServerCollection(player);
		int shardsToDelete = collection.size();
		collection.clear();
		ServerPlayNetworking.send(player, new S2CSyncCollection(collection));
		MinecraftServer server = ctx.getSource().getServer();
		ShardCollectionPersistentState.get(server).markDirty();

		ctx.getSource().sendFeedback(() -> Text.translatable("commands.scattered_shards.shard.uncollect.all", shardsToDelete), false);

		return shardsToDelete;
	}

	public static void register(CommandNode<ServerCommandSource> parent) {
		CommandNode<ServerCommandSource> uncollectCommand = ShardCommandNodeHelper.literal("uncollect")
			.requires(Permissions.require(ScatteredShards.permission("command.uncollect"), 2))
			.build();

		//syntax: uncollect <shard_id>
		CommandNode<ServerCommandSource> uncollectIdArgument = ShardCommandNodeHelper.collectedShardId("shard_id")
			.executes(UncollectCommand::uncollect)
			.build();

		//syntax: uncollect all
		CommandNode<ServerCommandSource> uncollectAllCommand = ShardCommandNodeHelper.literal("all")
			.executes(UncollectCommand::uncollectAll)
			.requires(
				Permissions.require(ScatteredShards.permission("command.uncollect.all"), 2)
			)
			.build();

		parent.addChild(uncollectCommand);
		uncollectCommand.addChild(uncollectIdArgument);
		uncollectCommand.addChild(uncollectAllCommand);
	}
}
