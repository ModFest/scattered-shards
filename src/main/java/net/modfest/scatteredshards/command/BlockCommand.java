package net.modfest.scatteredshards.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.block.ShardBlock;

public class BlockCommand {

	public static int blockCommand(CommandContext<ServerCommandSource> ctx, boolean options) throws CommandSyntaxException {
		ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
		Identifier shardId = ctx.getArgument("shard_id", Identifier.class);

		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();

		boolean canInteract = options && BoolArgumentType.getBool(ctx, "can_interact");
		float glowSize = options ? FloatArgumentType.getFloat(ctx, "glow_size") : 0.5f;
		float glowStrength = options ? FloatArgumentType.getFloat(ctx, "glow_strength") : 0.5f;

		ItemStack stack = ShardBlock.createShardBlock(library, shardId, canInteract, glowSize, glowStrength);

		player.getInventory().offerOrDrop(stack);

		ctx.getSource().sendFeedback(() -> Text.stringifiedTranslatable("commands.scattered_shards.shard.block", shardId), false);
		return Command.SINGLE_SUCCESS;
	}

	public static int block(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		return blockCommand(ctx, false);
	}

	public static int blockOptions(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		return blockCommand(ctx, true);
	}

	public static void register(CommandNode<ServerCommandSource> parent) {
		//Usage: /shard block <shard_id>
		CommandNode<ServerCommandSource> blockCommand = ShardCommandNodeHelper.literal("block")
			.requires(Permissions.require(ScatteredShards.permission("command.block"), 2))
			.build();
		CommandNode<ServerCommandSource> blockIdArgument = ShardCommandNodeHelper.shardId("shard_id")
			.executes(BlockCommand::block)
			.build();
		CommandNode<ServerCommandSource> blockInteractArgument = ShardCommandNodeHelper.booleanValue("can_interact").build();
		CommandNode<ServerCommandSource> blockGlowSizeArgument = ShardCommandNodeHelper.floatValue("glow_size").build();
		CommandNode<ServerCommandSource> blockGlowStrengthArgument = ShardCommandNodeHelper.floatValue("glow_strength")
			.executes(BlockCommand::blockOptions)
			.build(); //Already governed by "/shard block" permission

		parent.addChild(blockCommand);
		blockCommand.addChild(blockIdArgument);
		blockIdArgument.addChild(blockInteractArgument);
		blockInteractArgument.addChild(blockGlowSizeArgument);
		blockGlowSizeArgument.addChild(blockGlowStrengthArgument);
	}
}
