package net.modfest.scatteredshards.command;

import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.text.Text;

public class ShardCommand {
	
	public static final DynamicCommandExceptionType INVALID_SHARD = new DynamicCommandExceptionType(
			it -> Text.translatable("error.scattered_shards.invalid_shard_id", it)
			);
	
	public static final DynamicCommandExceptionType NO_ROOM_FOR_ITEM = new DynamicCommandExceptionType(
			it -> Text.translatable("error.scattered_shards.no_inventory_room", it)
			);
	
	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, context, environment) -> {
			var shardRoot = Node.literal("shard").build();
			dispatcher.getRoot().addChild(shardRoot);
			
			CollectCommand.register(shardRoot);
			AwardCommand.register(shardRoot);
			UncollectCommand.register(shardRoot);
			BlockCommand.register(shardRoot);
			LibraryCommand.register(shardRoot);
		});
	}
}
