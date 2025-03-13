package net.modfest.scatteredshards.command;

import com.mojang.brigadier.Command;
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
import net.modfest.scatteredshards.item.ShardItem;

public class ItemCommand {

	public static int item(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
		Identifier shardId = ctx.getArgument("shard_id", Identifier.class);

		ItemStack stack = ShardItem.createShardItem(shardId);

		player.getInventory().offerOrDrop(stack);

		ctx.getSource().sendFeedback(() -> Text.stringifiedTranslatable("commands.scattered_shards.shard.item", shardId), false);
		return Command.SINGLE_SUCCESS;
	}

	public static void register(CommandNode<ServerCommandSource> parent) {
		//Usage: /shard item <shard_id>
		CommandNode<ServerCommandSource> blockCommand = ShardCommandNodeHelper.literal("item")
			.requires(Permissions.require(ScatteredShards.permission("command.item"), 2))
			.build();
		CommandNode<ServerCommandSource> shardIdArgument = ShardCommandNodeHelper.shardId("shard_id")
			.executes(ItemCommand::item)
			.build();

		parent.addChild(blockCommand);
		blockCommand.addChild(shardIdArgument);
	}
}
