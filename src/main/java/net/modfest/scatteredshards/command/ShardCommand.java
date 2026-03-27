package net.modfest.scatteredshards.command;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class ShardCommand {

	public static final DynamicCommandExceptionType INVALID_SHARD = new DynamicCommandExceptionType(
		it -> Component.translatableEscape("error.scattered_shards.invalid_shard_id", it)
	);

	public static final DynamicCommandExceptionType INVALID_SHARD_TYPE = new DynamicCommandExceptionType(
		it -> Component.translatable("error.scattered_shards.invalid_shard_type", it)
	);

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, context, environment) -> {
			/*
			I'm not setting a permission for this one because the "subcommands" have their own unique permission settings
			- SkyNotTheLimit
			 */
			CommandNode<CommandSourceStack> shardNode = ShardCommandNodeHelper.literal("shard").build();

			dispatcher.getRoot().addChild(shardNode);

			CollectCommand.register(shardNode);
			AwardCommand.register(shardNode);
			UncollectCommand.register(shardNode);
			BlockCommand.register(shardNode);
			ItemCommand.register(shardNode);
			LibraryCommand.register(shardNode);
		});
	}
}
