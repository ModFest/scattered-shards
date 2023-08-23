package net.modfest.scatteredshards.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.component.ScatteredShardsComponents;
import net.modfest.scatteredshards.component.ShardCollectionComponent;
import net.modfest.scatteredshards.component.ShardLibraryComponent;

public class AwardCommand {
	
	public static int award(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		EntitySelector target = ctx.getArgument("players", EntitySelector.class);
		Identifier shardId = ctx.getArgument("shard_id", Identifier.class);
		
		ShardLibraryComponent library = ScatteredShardsComponents.getShardLibrary(ctx);
		Shard shard = library.getShard(shardId);
		if (shard == Shard.MISSING_SHARD) throw ShardCommand.INVALID_SHARD.create(shardId);
		
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
	
	public static void register(CommandNode<ServerCommandSource> parent) {
		var awardCommand = Node.literal("award").build();
		var awardPlayerArgument = Node.players("players").build();
		var awardIdArgument = Node.shardId("shard_id")
				.executes(AwardCommand::award)
				.requires(
					Permissions.require(ScatteredShards.permission("command.award"), 2)
				)
				.build();
		awardCommand.addChild(awardPlayerArgument);
		awardPlayerArgument.addChild(awardIdArgument);
		parent.addChild(awardCommand);
	}
}
