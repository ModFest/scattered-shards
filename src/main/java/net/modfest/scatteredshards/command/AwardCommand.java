package net.modfest.scatteredshards.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;

public class AwardCommand {

	public static int award(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		EntitySelector target = ctx.getArgument("players", EntitySelector.class);
		Identifier shardId = ctx.getArgument("shard_id", Identifier.class);

		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();
		library.shards().get(shardId).orElseThrow(() -> ShardCommand.INVALID_SHARD.create(shardId)); //Validate shardId

		int i = 0;
		for (ServerPlayer player : target.findPlayers(ctx.getSource())) {
			if (ScatteredShardsAPI.triggerShardCollection(player, shardId)) {
				i++;
			}
		}
		final int collected = i;

		if (collected == 0) {
			ctx.getSource().sendSuccess(() -> Component.translatableEscape("commands.scattered_shards.shard.award.none", shardId), false);
		} else {
			ctx.getSource().sendSuccess(() -> Component.translatableEscape("commands.scattered_shards.shard.award", shardId, collected), false);
		}

		return collected;
	}

	public static void register(CommandNode<CommandSourceStack> parent) {
		CommandNode<CommandSourceStack> awardCommand = ShardCommandNodeHelper.literal("award")
			.requires(Permissions.require(ScatteredShards.permission("command.award"), 2))
			.build();
		CommandNode<CommandSourceStack> awardPlayerArgument = ShardCommandNodeHelper.players("players").build();
		CommandNode<CommandSourceStack> awardIdArgument = ShardCommandNodeHelper.shardId("shard_id")
			.executes(AwardCommand::award)
			.build();
		parent.addChild(awardCommand);
		awardCommand.addChild(awardPlayerArgument);
		awardPlayerArgument.addChild(awardIdArgument);
	}
}
