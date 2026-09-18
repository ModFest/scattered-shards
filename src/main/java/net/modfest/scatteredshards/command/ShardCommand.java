package net.modfest.scatteredshards.command;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.permission.v1.PermissionContextOwner;
import net.fabricmc.fabric.api.permission.v1.PermissionNode;
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionLevel;
import net.modfest.scatteredshards.ScatteredShards;

import java.util.function.Predicate;

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

	public static final class Permissions {
		private Permissions() {
		}

		public static final PermissionNode<Boolean> AWARD = ScatteredShards.permission("command.award"); // 2
		public static final PermissionNode<Boolean> ITEM = ScatteredShards.permission("command.item"); // 2
		public static final PermissionNode<Boolean> LIBRARY = ScatteredShards.permission("command.library"); // 3
		public static final PermissionNode<Boolean> LIBRARY_DELETE = ScatteredShards.permission("command.library.delete"); // 3
		public static final PermissionNode<Boolean> LIBRARY_DELETE_ALL = ScatteredShards.permission("command.library.delete.all"); // 4
		public static final PermissionNode<Boolean> LIBRARY_MIGRATE = ScatteredShards.permission("command.library.migrate"); // 3
		public static final PermissionNode<Boolean> COLLECT = ScatteredShards.permission("command.collect"); // 2
		public static final PermissionNode<Boolean> UNCOLLECT = ScatteredShards.permission("command.uncollect"); // 2
		public static final PermissionNode<Boolean> UNCOLLECT_ALL = ScatteredShards.permission("command.uncollect.all"); // 2
		public static final PermissionNode<Boolean> BLOCK = ScatteredShards.permission("command.block"); // 2

		public static <T extends PermissionContextOwner> Predicate<T> require(PermissionNode<Boolean> node, int level) {
			return PermissionPredicates.require(node, PermissionLevel.byId(level));
		}
	}
}
