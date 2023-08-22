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
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.component.ScatteredShardsComponents;
import net.modfest.scatteredshards.component.ShardLibraryComponent;

public class CollectCommand {
	
	public static int collect(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		Identifier id = ctx.getArgument("shard_id", Identifier.class);
		
		ShardLibraryComponent library = ScatteredShardsComponents.getShardLibrary(ctx);
		
		Shard shard = library.getShard(id);
		if (shard == Shard.MISSING_SHARD) throw ShardCommand.INVALID_SHARD.create(id);
		
		ScatteredShardsComponents.getShardCollection(ctx.getSource().getPlayer()).addShard(id);
		
		ctx.getSource().sendFeedback(() -> Text.translatable("commands.scattered_shards.shard.collect", id), false);
		
		return Command.SINGLE_SUCCESS;
	}
	
	public static void register(CommandNode<ServerCommandSource> parent) {
		var collectCommand = Node.literal("collect").build();
		var collectIdArgument = Node.shardId("shard_id")
				.executes(CollectCommand::collect)
				.requires(
					Permissions.require(ScatteredShards.permission("command.collect"), 2)
				)
				.build();
		collectCommand.addChild(collectIdArgument);
		parent.addChild(collectCommand);
	}
}
