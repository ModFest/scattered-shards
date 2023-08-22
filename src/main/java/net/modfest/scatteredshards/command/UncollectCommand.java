package net.modfest.scatteredshards.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.component.ScatteredShardsComponents;
import net.modfest.scatteredshards.component.ShardCollectionComponent;

public class UncollectCommand {
	public static final DynamicCommandExceptionType NOT_IN_COLLECTION = new DynamicCommandExceptionType(
			it -> Text.translatable("error.scattered_shards.shard_not_in_collection", it)
			);
	
	/**
	 * Syntax: <code>/shard uncollect &lt;shard_id&gt;</code>
	 * <p>Removes the specified shard from the library / tablet of the person running the command. Must be used by a player.
	 * @return Always 1 for the shard removed, unless an exception occurs.
	 * @throws CommandSyntaxException if there was a problem executing the command.
	 */
	public static int uncollect(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		Identifier id = ctx.getArgument("shard_id", Identifier.class);
		boolean success = ScatteredShardsComponents.getShardCollection(ctx.getSource().getPlayer()).removeShard(id);
		if (!success) throw NOT_IN_COLLECTION.create(id);
		
		ctx.getSource().sendFeedback(() -> Text.translatable("commands.scattered_shards.shard.uncollect", id), false);
		return Command.SINGLE_SUCCESS;
	}
	
	/**
	 * Syntax: <code>/shard uncollect all</code>
	 * <p>Removes all shards from the library / tablet of the person running the command. Must be used by a player.
	 * @return The number of shards removed. Zero is a valid output from this command (if the collection was empty).
	 * @throws CommandSyntaxException if there was a problem executing the command.
	 */
	public static int uncollectAll(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ShardCollectionComponent collection = ScatteredShardsComponents.getShardCollection(ctx.getSource().getPlayer());
		int shardsToDelete = collection.size();
		collection.clear();
		ctx.getSource().sendFeedback(() -> Text.translatable("commands.scattered_shards.shard.uncollect.all", shardsToDelete), false);

		return shardsToDelete;
	}
	
	public static void register(CommandNode<ServerCommandSource> parent) {
		var uncollectCommand = Node.literal("uncollect")
				.requires(
						Permissions.require(ScatteredShards.permission("command.uncollect"), 2)
					)
				.build();
		parent.addChild(uncollectCommand);
		
		//syntax: uncollect <shard_id>
		var uncollectIdArgument = Node.collectedShardId("shard_id")
				.executes(UncollectCommand::uncollect)
				.build();
		uncollectCommand.addChild(uncollectIdArgument);
		
		//syntax: uncollect all
		var uncollectAllCommand = Node.literal("all")
				.executes(UncollectCommand::uncollectAll)
				.requires((src) -> {
						System.out.println(Permissions.getPermissionValue(src, "scattered_shards.command.uncollect.all"));
						return Permissions.require(ScatteredShards.permission("command.uncollect.all"), 2).test(src);
					}
				)
				.build();
		uncollectCommand.addChild(uncollectAllCommand);
	}
}
