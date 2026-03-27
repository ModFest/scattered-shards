package net.modfest.scatteredshards.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

	public static int delete(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ResourceLocation shardId = ctx.getArgument("shard_id", ResourceLocation.class);

		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();
		library.shards().get(shardId).orElseThrow(() -> ShardCommand.INVALID_SHARD.create(shardId));

		Optional<Shard> shard = library.shards().get(shardId);
		library.shards().remove(shardId);
		shard.ifPresent(it -> library.shardSets().remove(it.sourceId(), shardId));
		MinecraftServer server = ctx.getSource().getServer();
		ShardLibraryPersistentState.get(server).setDirty();
		S2CUpdateShard deletePacket = new S2CUpdateShard(shardId, S2CUpdateShard.Mode.DELETE);
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			ServerPlayNetworking.send(player, deletePacket);
		}

		ctx.getSource().sendSuccess(() -> Component.translatableEscape("commands.scattered_shards.shard.library.delete", shardId), true);

		return Command.SINGLE_SUCCESS;
	}

	public static int deleteAll(CommandContext<CommandSourceStack> ctx) {
		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();
		int toDelete = library.shards().size();
		library.shards().clear();
		library.shardSets().clear();
		MinecraftServer server = ctx.getSource().getServer();
		ShardLibraryPersistentState.get(server).setDirty();
		S2CSyncLibrary syncLibrary = new S2CSyncLibrary(library);
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			ServerPlayNetworking.send(player, syncLibrary);
		}

		ctx.getSource().sendSuccess(() -> Component.translatableEscape("commands.scattered_shards.shard.library.delete.all", toDelete), true);

		return toDelete;
	}

	public static int migrate(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ResourceLocation shardId = ctx.getArgument("shard_id", ResourceLocation.class);
		String modId = StringArgumentType.getString(ctx, "mod_id");
		ResourceLocation shardTypeId = ctx.getArgument("shard_type", ResourceLocation.class);
		ResourceLocation newShardId = ShardType.createModId(shardTypeId, modId);

		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();
		library.shardTypes().get(shardTypeId).orElseThrow(() -> ShardCommand.INVALID_SHARD_TYPE.create(shardTypeId));
		Shard shard = library.shards().get(shardId).orElseThrow(() -> ShardCommand.INVALID_SHARD.create(shardId));

		library.shards().remove(shardId);
		library.shardSets().values().removeIf(i -> i.equals(shardId));
		shard.setShardType(shardTypeId);
		library.shards().put(newShardId, shard);

		MinecraftServer server = ctx.getSource().getServer();
		ShardLibraryPersistentState.get(server).setDirty();

		S2CUpdateShard deleteShard = new S2CUpdateShard(shardId, S2CUpdateShard.Mode.DELETE);
		S2CSyncShard syncShard = new S2CSyncShard(newShardId, shard);
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			ServerPlayNetworking.send(player, deleteShard);
			ServerPlayNetworking.send(player, syncShard);
		}

		ctx.getSource().sendSuccess(() -> Component.translatableEscape("commands.scattered_shards.shard.library.migrate", shardId, newShardId), true);

		return Command.SINGLE_SUCCESS;
	}

	public static void register(CommandNode<CommandSourceStack> parent) {
		CommandNode<CommandSourceStack> library = ShardCommandNodeHelper.literal("library")
			.requires(Permissions.require(ScatteredShards.permission("command.library"), 3))
			.build();

		//Usage: /shard library delete <shard_id>
		CommandNode<CommandSourceStack> deleteCommand = ShardCommandNodeHelper.literal("delete")
			.requires(Permissions.require(ScatteredShards.permission("command.library.delete"), 3))
			.build();
		CommandNode<CommandSourceStack> deleteIdArgument = ShardCommandNodeHelper.shardId("shard_id")
			.executes(LibraryCommand::delete)
			.build();

		//Usage: /shard library delete all
		CommandNode<CommandSourceStack> deleteAllCommand = ShardCommandNodeHelper.literal("all")
			.executes(LibraryCommand::deleteAll)
			.requires(Permissions.require(ScatteredShards.permission("command.library.delete.all"), 4))
			.build();

		CommandNode<CommandSourceStack> migrateCommand = ShardCommandNodeHelper.literal("migrate")
			.requires(Permissions.require(ScatteredShards.permission("command.library.migrate"), 3)).build();

		CommandNode<CommandSourceStack> migrateShardArg = ShardCommandNodeHelper.shardId("shard_id").build();
		CommandNode<CommandSourceStack> migrateModArg = ShardCommandNodeHelper.stringArgument("mod_id").suggests(ShardCommandNodeHelper::suggestModIds).build();
		CommandNode<CommandSourceStack> migrateShardTypeArg = ShardCommandNodeHelper.identifier("shard_type").suggests(ShardCommandNodeHelper::suggestShardTypes)
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
