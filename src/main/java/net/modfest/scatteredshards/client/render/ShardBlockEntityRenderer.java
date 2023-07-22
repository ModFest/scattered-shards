package net.modfest.scatteredshards.client.render;

import org.quiltmc.loader.api.minecraft.ClientOnly;

import com.mojang.blaze3d.vertex.VertexConsumer;

import org.joml.Quaternionf;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.modfest.scatteredshards.block.ShardBlockEntity;
import net.modfest.scatteredshards.api.shard.Shard;

@ClientOnly
public class ShardBlockEntityRenderer implements BlockEntityRenderer<ShardBlockEntity> {
	public static final float TICKS_PER_RADIAN = 16f;

	public ShardBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {

	}


	@Override
	public void render(ShardBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

		Shard shard = entity.getShard();
		if (shard == null) {
			//Let's make one up!
			shard = Shard.MISSING_SHARD;
		}

		float l = (entity.getWorld().getTime() + tickDelta) / TICKS_PER_RADIAN;
		l %= Math.PI*2;
		float angle = l; //(float) (Math.PI/8);
		Quaternionf rot = new Quaternionf(new AxisAngle4f(angle, 0f, 1f, 0f));
		Quaternionf tilt = new Quaternionf(new AxisAngle4f((float) (Math.PI/8), 0f, 0f, 1f));

		matrices.push();

		matrices.translate(0.5, 0.5, 0.5);
		matrices.multiply(rot);
		matrices.multiply(tilt);

		VertexConsumer buf = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(shard.shardType().getBackingTexture()));

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
		buf = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(shard.shardType().getFrontTexture()));
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


		shard.icon().ifLeft( stack -> {
			matrices.scale(0.3f, 0.3f, 0.3f);
			matrices.translate(0, 0, -0.252f); //extra -0.002 here to prevent full-cubes from zfighting the card

			MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), 0);
		});

		shard.icon().ifRight( texId -> {
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
