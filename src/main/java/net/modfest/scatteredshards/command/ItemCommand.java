package net.modfest.scatteredshards.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.item.ShardItem;

public class ItemCommand {

	public static int item(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		ResourceLocation shardId = ctx.getArgument("shard_id", ResourceLocation.class);
		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();

		var name = library.shards().get(shardId).map(Shard::name).orElse(null);

		ItemStack stack = ShardItem.createShardItem(shardId, name);

		player.getInventory().placeItemBackInInventory(stack);

		ctx.getSource().sendSuccess(() -> Component.translatableEscape("commands.scattered_shards.shard.item", shardId), false);
		return Command.SINGLE_SUCCESS;
	}

	public static void register(CommandNode<CommandSourceStack> parent) {
		//Usage: /shard item <shard_id>
		CommandNode<CommandSourceStack> blockCommand = ShardCommandNodeHelper.literal("item")
			.requires(Permissions.require(ScatteredShards.permission("command.item"), 2))
			.build();
		CommandNode<CommandSourceStack> shardIdArgument = ShardCommandNodeHelper.shardId("shard_id")
			.executes(ItemCommand::item)
			.build();

		parent.addChild(blockCommand);
		blockCommand.addChild(shardIdArgument);
	}
}
