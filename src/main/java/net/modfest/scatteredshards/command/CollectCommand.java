package net.modfest.scatteredshards.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;

public class CollectCommand {

	public static int collect(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		Identifier id = ctx.getArgument("shard_id", Identifier.class);

		//Validate shard
		ScatteredShardsAPI.getServerLibrary().shards().get(id)
			.orElseThrow(() -> ShardCommand.INVALID_SHARD.create(id));

		//Validate that source is a player and collect it
		ScatteredShardsAPI.triggerShardCollection(ctx.getSource().getPlayerOrThrow(), id);

		ctx.getSource().sendFeedback(() -> Text.stringifiedTranslatable("commands.scattered_shards.shard.collect", id), false);

		return Command.SINGLE_SUCCESS;
	}

	public static void register(CommandNode<ServerCommandSource> parent) {
		CommandNode<ServerCommandSource> collectCommand = ShardCommandNodeHelper.literal("collect")
			.requires(Permissions.require(ScatteredShards.permission("command.collect"), 2))
			.build();
		CommandNode<ServerCommandSource> collectIdArgument = ShardCommandNodeHelper.shardId("shard_id")
			.executes(CollectCommand::collect)
			.build();
		parent.addChild(collectCommand);
		collectCommand.addChild(collectIdArgument);
	}
}
