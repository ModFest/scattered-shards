package net.modfest.scatteredshards.client.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.command.ShardCommand;
import net.modfest.scatteredshards.command.ShardCommandNodeHelper;
import net.modfest.scatteredshards.networking.C2SCreateShardInstant;
import net.modfest.scatteredshards.util.ModMetaUtil;

import static net.modfest.scatteredshards.client.command.ClientShardCommand.identifierArgument;
import static net.modfest.scatteredshards.client.command.ClientShardCommand.literal;
import static net.modfest.scatteredshards.client.command.ClientShardCommand.stringArgument;

public class CreateInstantCommand {
	public static int create(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
		var source = ctx.getSource();

		var modId = StringArgumentType.getString(ctx, "mod_id");
		var shardTypeId = ctx.getArgument("shard_type", ResourceLocation.class);
		var name = StringArgumentType.getString(ctx, "shard_name");
		var lore = StringArgumentType.getString(ctx, "shard_lore");
		var hint = StringArgumentType.getString(ctx, "shard_hint");

		var library = ScatteredShardsAPI.getClientLibrary();

		// Make sure shard type exists
		library.shardTypes().get(shardTypeId).orElseThrow(() -> ShardCommand.INVALID_SHARD_TYPE.create(shardTypeId.toString()));


		var shardId = ShardType.createModId(shardTypeId, modId);
		var shard = Shard.emptyOfType(shardTypeId);
		var modIcon = ModMetaUtil.touchModIcon(modId);

		if (library.shards().get(shardId).isPresent()) {
			source.sendError(Component.translatable("error.scattered_shards.duplicate_id", shardId.toString()));
			return 0;
		}

		shard.setName(Component.literal(name));
		shard.setLore(Component.literal(lore));
		shard.setHint(Component.literal(hint));
		shard.setIcon(modIcon);
		shard.setSourceId(ResourceLocation.fromNamespaceAndPath(modId, "shard_pack"));

		ClientPlayNetworking.send(new C2SCreateShardInstant(shardId, shard));

		source.sendFeedback(Component.translatable("commands.scattered_shards.shard.create_instant", shardId.toString()));

		return 1;
	}

	public static void register(CommandNode<FabricClientCommandSource> parent) {
		var createInstantCommand = literal("create_instant").build();
		var modId = stringArgument("mod_id").suggests(ShardCommandNodeHelper::suggestModIds).build();
		var type = identifierArgument("shard_type").suggests(ClientShardCommand::suggestShardTypes).build();
		var name = stringArgument("shard_name").build();
		var lore = stringArgument("shard_lore").build();
		var hint = stringArgument("shard_hint").executes(CreateInstantCommand::create).build();

		parent.addChild(createInstantCommand);
		createInstantCommand.addChild(modId);
		modId.addChild(type);
		type.addChild(name);
		name.addChild(lore);
		lore.addChild(hint);
	}
}
