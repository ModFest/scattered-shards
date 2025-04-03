package net.modfest.scatteredshards.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.impl.ShardLibraryPersistentState;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.networking.S2CSyncLibrary;
import net.modfest.scatteredshards.networking.S2CSyncShard;
import net.modfest.scatteredshards.networking.S2CUpdateShard;

import java.util.Optional;

public class LibraryCommand {

	public static int delete(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		Identifier shardId = ctx.getArgument("shard_id", Identifier.class);

		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();
		library.shards().get(shardId).orElseThrow(() -> ShardCommand.INVALID_SHARD.create(shardId));

		Optional<Shard> shard = library.shards().get(shardId);
		library.shards().remove(shardId);
		shard.ifPresent(it -> library.shardSets().remove(it.sourceId(), shardId));
		MinecraftServer server = ctx.getSource().getServer();
		ShardLibraryPersistentState.get(server).markDirty();
		S2CUpdateShard deletePacket = new S2CUpdateShard(shardId, S2CUpdateShard.Mode.DELETE);
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			ServerPlayNetworking.send(player, deletePacket);
		}

		ctx.getSource().sendFeedback(() -> Text.stringifiedTranslatable("commands.scattered_shards.shard.library.delete", shardId), true);

		return Command.SINGLE_SUCCESS;
	}

	public static int deleteAll(CommandContext<ServerCommandSource> ctx) {
		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();
		int toDelete = library.shards().size();
		library.shards().clear();
		library.shardSets().clear();
		MinecraftServer server = ctx.getSource().getServer();
		ShardLibraryPersistentState.get(server).markDirty();
		S2CSyncLibrary syncLibrary = new S2CSyncLibrary(library);
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			ServerPlayNetworking.send(player, syncLibrary);
		}

		ctx.getSource().sendFeedback(() -> Text.stringifiedTranslatable("commands.scattered_shards.shard.library.delete.all", toDelete), true);

		return toDelete;
	}

	public static int migrate(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		Identifier shardId = ctx.getArgument("shard_id", Identifier.class);
		String modId = StringArgumentType.getString(ctx, "mod_id");
		Identifier shardTypeId = ctx.getArgument("shard_type", Identifier.class);
		Identifier newShardId = ShardType.createModId(shardTypeId, modId);

		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();
		library.shardTypes().get(shardTypeId).orElseThrow(() -> ShardCommand.INVALID_SHARD_TYPE.create(shardTypeId));
		Shard shard = library.shards().get(shardId).orElseThrow(() -> ShardCommand.INVALID_SHARD.create(shardId));

		library.shards().remove(shardId);
		library.shardSets().values().removeIf(i -> i.equals(shardId));
		shard.setShardType(shardTypeId);
		library.shards().put(newShardId, shard);

		MinecraftServer server = ctx.getSource().getServer();
		ShardLibraryPersistentState.get(server).markDirty();

		S2CUpdateShard deleteShard = new S2CUpdateShard(shardId, S2CUpdateShard.Mode.DELETE);
		S2CSyncShard syncShard = new S2CSyncShard(newShardId, shard);
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			ServerPlayNetworking.send(player, deleteShard);
			ServerPlayNetworking.send(player, syncShard);
		}

		ctx.getSource().sendFeedback(() -> Text.stringifiedTranslatable("commands.scattered_shards.shard.library.migrate", shardId, newShardId), true);

		return Command.SINGLE_SUCCESS;
	}

	public static void register(CommandNode<ServerCommandSource> parent) {
		CommandNode<ServerCommandSource> library = ShardCommandNodeHelper.literal("library")
			.requires(Permissions.require(ScatteredShards.permission("command.library"), 3))
			.build();

		//Usage: /shard library delete <shard_id>
		CommandNode<ServerCommandSource> deleteCommand = ShardCommandNodeHelper.literal("delete")
			.requires(Permissions.require(ScatteredShards.permission("command.library.delete"), 3))
			.build();
		CommandNode<ServerCommandSource> deleteIdArgument = ShardCommandNodeHelper.shardId("shard_id")
			.executes(LibraryCommand::delete)
			.build();

		//Usage: /shard library delete all
		CommandNode<ServerCommandSource> deleteAllCommand = ShardCommandNodeHelper.literal("all")
			.executes(LibraryCommand::deleteAll)
			.requires(Permissions.require(ScatteredShards.permission("command.library.delete.all"), 4))
			.build();

		CommandNode<ServerCommandSource> migrateCommand = ShardCommandNodeHelper.literal("migrate")
			.requires(Permissions.require(ScatteredShards.permission("command.library.migrate"), 3)).build();

		CommandNode<ServerCommandSource> migrateShardArg = ShardCommandNodeHelper.shardId("shard_id").build();
		CommandNode<ServerCommandSource> migrateModArg = ShardCommandNodeHelper.stringArgument("mod_id").suggests(ShardCommandNodeHelper::suggestModIds).build();
		CommandNode<ServerCommandSource> migrateShardTypeArg = ShardCommandNodeHelper.identifier("shard_type").suggests(ShardCommandNodeHelper::suggestShardTypes)
			.executes(LibraryCommand::migrate).build();

		parent.addChild(library);
		library.addChild(deleteCommand);
		library.addChild(migrateCommand);
		deleteCommand.addChild(deleteIdArgument);
		deleteCommand.addChild(deleteAllCommand);
		migrateCommand.addChild(migrateShardArg);
		migrateShardArg.addChild(migrateModArg);
		migrateModArg.addChild(migrateShardTypeArg);
	}
}
