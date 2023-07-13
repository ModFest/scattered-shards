package net.modfest.scatteredshards.client;

import org.quiltmc.loader.api.minecraft.ClientOnly;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Either;

import org.joml.Quaternionf;
import org.joml.AxisAngle4f;
import org.joml.Vector4f;
import org.joml.Vector3f;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.core.ShardBlockEntity;
import net.modfest.scatteredshards.core.api.shard.Shard;
import net.modfest.scatteredshards.core.api.shard.ShardType;

@ClientOnly
public class ShardBlockEntityRenderer implements BlockEntityRenderer<ShardBlockEntity> {
	public static final float TICKS_PER_RADIAN = 16f;
	
	public ShardBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
		
	}


	@Override
	public void render(ShardBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		//Turn the entity into an itemstack, then render that?
		//entity.getShardId();
		
		Shard shard = entity.getShard();
		if (shard == null) {
			//Let's make one up!
			//shard = new Shard(ScatteredShards.id("visitor"), Text.literal(""), Text.literal(""), Text.literal(""), Either.left(new ItemStack(Items.APPLE)));
			shard = new Shard(ScatteredShards.id("visitor"), Text.literal(""), Text.literal(""), Text.literal(""), Either.right(new Identifier("scattered_shards","textures/item/shard_block.png")));
		}
		
		//TODO: Get the texture IDs for the front and back of the card based on shard.shardType()
		Identifier backTexture = ScatteredShards.id("textures/shard/secret.png");
		Identifier frontTexture = ScatteredShards.id("textures/shard/secret_front.png");
		Either<ItemStack, Identifier> icon = shard.icon();
		
		float l = (entity.getWorld().getTime() + tickDelta) / TICKS_PER_RADIAN;
		l %= Math.PI*2;
		float angle = l; //(float) (Math.PI/8);
		Quaternionf rot = new Quaternionf(new AxisAngle4f(angle, 0f, 1f, 0f));
		Quaternionf tilt = new Quaternionf(new AxisAngle4f((float) (Math.PI/8), 0f, 0f, 1f));
		
		matrices.push();
		//matrices.translate(-0.5, -0.5, -0.5);
		matrices.translate(0.5, 0.5, 0.5);
		matrices.multiply(rot);
		
		matrices.multiply(tilt);
		
		VertexConsumer buf = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(backTexture));
		
		Vector3f dl = new Vector3f(-4/16f, -6/16f, 0f);
		Vector3f dr = new Vector3f( 4/16f, -6/16f, 0f);
		Vector3f ul = new Vector3f(-4/16f,  6/16f, 0f);
		Vector3f ur = new Vector3f( 4/16f,  6/16f, 0f);
		
		Vector3f normal = new Vector3f(0, 0, -1);
		
		//Draw card back
		buf
			.vertex(matrices.peek().getModel(), dl.x, dl.y, dl.z)
			.color(0xFF_FFFFFF)
			.uv(0, 1)
			.overlay(overlay)
			.light(light)
			.normal(normal.x(), normal.y(), normal.z())
			.next();
			
		buf
			.vertex(matrices.peek().getModel(), dr.x, dr.y, dr.z)
			.color(0xFF_FFFFFF)
			.uv(1, 1)
			.overlay(overlay)
			.light(light)
			.normal(normal.x(), normal.y(), normal.z())
			.next();
		
		buf
			.vertex(matrices.peek().getModel(), ur.x, ur.y, ur.z)
			.color(0xFF_FFFFFF)
			.uv(1, 0)
			.overlay(overlay)
			.light(light)
			.normal(normal.x(), normal.y(), normal.z())
			.next();
		
		buf
			.vertex(matrices.peek().getModel(), ul.x, ul.y, ul.z)
			.color(0xFF_FFFFFF)
			.uv(0, 0)
			.overlay(overlay)
			.light(light)
			.normal(normal.x(), normal.y(), normal.z())
			.next();
		
		//Draw card front
		Vector3f revNormal = normal.mul(-1, -1, -1);
		buf = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(frontTexture));
		buf
			.vertex(matrices.peek().getModel(), dl.x, dl.y, dl.z)
			.color(0xFF_FFFFFF)
			.uv(1, 1)
			.overlay(overlay)
			.light(light)
			.normal(matrices.peek().getNormal(), revNormal.x(), revNormal.y(), revNormal.z())
			.next();
			
		buf
			.vertex(matrices.peek().getModel(), ul.x, ul.y, ul.z)
			.color(0xFF_FFFFFF)
			.uv(1, 0)
			.overlay(overlay)
			.light(light)
			.normal(matrices.peek().getNormal(), revNormal.x(), revNormal.y(), revNormal.z())
			.next();
		
		buf
			.vertex(matrices.peek().getModel(), ur.x, ur.y, ur.z)
			.color(0xFF_FFFFFF)
			.uv(0, 0)
			.overlay(overlay)
			.light(light)
			.normal(matrices.peek().getNormal(), revNormal.x(), revNormal.y(), revNormal.z())
			.next();
		
		buf
			.vertex(matrices.peek().getModel(), dr.x, dr.y, dr.z)
			.color(0xFF_FFFFFF)
			.uv(0, 1)
			.overlay(overlay)
			.light(light)
			.normal(matrices.peek().getNormal(), revNormal.x(), revNormal.y(), revNormal.z())
			.next();
		
		
		icon.ifLeft( stack -> {
			matrices.scale(0.3f, 0.3f, 0.3f);
			matrices.translate(0, 0, -0.252f); //extra -0.002 here to prevent full-cubes from zfighting the card
			
			MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), 0);
		});
		
		icon.ifRight( texId -> {
			VertexConsumer v = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(texId));
			
			v.vertex(matrices.peek().getModel(), dl.x + 0.1f, dl.y + 0.2f, dl.z - 0.002f)
				.color(0xFF_FFFFFF)
				.uv(1, 1)
				.overlay(overlay)
				.light(light)
				.normal(matrices.peek().getNormal(), revNormal.x(), revNormal.y(), revNormal.z())
				.next();
				
			v.vertex(matrices.peek().getModel(), ul.x + 0.1f, ul.y - 0.2f, ul.z - 0.002f)
				.color(0xFF_FFFFFF)
				.uv(1, 0)
				.overlay(overlay)
				.light(light)
				.normal(matrices.peek().getNormal(), revNormal.x(), revNormal.y(), revNormal.z())
				.next();
			
			v.vertex(matrices.peek().getModel(), ur.x - 0.1f, ur.y - 0.2f, ur.z - 0.002f)
				.color(0xFF_FFFFFF)
				.uv(0, 0)
				.overlay(overlay)
				.light(light)
				.normal(matrices.peek().getNormal(), revNormal.x(), revNormal.y(), revNormal.z())
				.next();
			
			v.vertex(matrices.peek().getModel(), dr.x - 0.1f, dr.y + 0.2f, dr.z - 0.002f)
				.color(0xFF_FFFFFF)
				.uv(0, 1)
				.overlay(overlay)
				.light(light)
				.normal(matrices.peek().getNormal(), revNormal.x(), revNormal.y(), revNormal.z())
				.next();
		});
		
		matrices.pop();
		
	}
}