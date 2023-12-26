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
import net.modfest.scatteredshards.networking.ScatteredShardsNetworking;

public class LibraryCommand {
	
	public static int delete(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		Identifier shardId = ctx.getArgument("shard_id", Identifier.class);
		
		var library = ScatteredShardsAPI.getServerLibrary();
		library.shards().get(shardId).orElseThrow(() -> ShardCommand.INVALID_SHARD.create(shardId));
		
		library.shards().remove(shardId);
		ScatteredShardsNetworking.S2CDeleteShard.sendToAll(ctx.getSource().getServer(), shardId);
		
		ctx.getSource().sendFeedback(() -> Text.translatable("commands.scattered_shards.shard.library.delete", shardId), true);
		
		return Command.SINGLE_SUCCESS;
	}
	
	public static int deleteAll(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		var library = ScatteredShardsAPI.getServerLibrary();
		int toDelete = library.shards().size();
		library.shards().clear();
		ScatteredShardsNetworking.S2CSyncLibrary.sendToAll(ctx.getSource().getServer());
		
		ctx.getSource().sendFeedback(() -> Text.translatable("commands.scattered_shards.shard.library.delete.all", toDelete), true);
		
		return toDelete;
	}
	
	public static void register(CommandNode<ServerCommandSource> parent) {
		var library = Node.literal("library")
				.requires(
					Permissions.require(ScatteredShards.permission("command.library"), 3)
				)
				.build();
		
		//Usage: /shard library delete <shard_id>
		var deleteCommand = Node.literal("delete")
				.requires(
						Permissions.require(ScatteredShards.permission("command.library.delete"), 3)
					)
				.build();
		var deleteIdArgument = Node.shardId("shard_id")
				.executes(LibraryCommand::delete)
				.build();
		deleteCommand.addChild(deleteIdArgument);
		library.addChild(deleteCommand);
		
		//Usage: /shard library delete all
		var deleteAllCommand = Node.literal("all")
				.executes(LibraryCommand::deleteAll)
				.requires(
					Permissions.require(ScatteredShards.permission("command.library.delete.all"), 4)
				)
				.build();
		library.addChild(deleteAllCommand);
		
		parent.addChild(library);
	}
}
