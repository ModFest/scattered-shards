package net.modfest.scatteredshards.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.shard.*;
import net.modfest.scatteredshards.block.ShardBlockEntity;
import net.modfest.scatteredshards.util.ModMetaUtil;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class ShardBlockEntityRenderer implements BlockEntityRenderer<ShardBlockEntity> {
	public static final float BLOCK_SCALE = 0.75f;
	
	private static final Identifier DISTANCE_GLOW_TEX = ScatteredShards.id("textures/entity/shard_distance_glow.png");
	private static final Identifier DISTANCE_HALO_TEX = ScatteredShards.id("textures/entity/shard_distance_halo.png");

	public ShardBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {

	}


	@Override
	public void render(ShardBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {
		boolean collected = entity.getAnimations().collected();
		final int actualLight = collected ? light : LightmapTextureManager.MAX_LIGHT_COORDINATE;

		Shard shard = entity.getShard(ScatteredShardsAPI.getClientLibrary());
		if (shard == null) {
			//Let's make one up!
			shard = Shard.MISSING_SHARD;
		}

		shard.icon().ifRight(ModMetaUtil::touchIconTexture);
		ShardType shardType = ScatteredShardsAPI.getClientLibrary().shardTypes().get(shard.shardTypeId()).orElse(ShardType.MISSING);

		float angle = entity.getAnimations().getAngle(tickDelta);
		Quaternionf rot = new Quaternionf(new AxisAngle4f(angle, 0f, 1f, 0f));
		Quaternionf tilt = new Quaternionf(new AxisAngle4f((float) (Math.PI / 8), 0f, 0f, 1f));

		matrices.push();

		matrices.translate(0.5, 0.5, 0.5);
		matrices.multiply(rot);
		matrices.multiply(tilt);

		float alpha = collected ? 0.5f : 1f;

		VertexConsumer buf = vertexConsumers.getBuffer(RenderLayer.getItemEntityTranslucentCull(ShardType.getBackingTexture(shard.shardTypeId())));

		/*
		 * A note about scale here:
		 * Cards are about 0.75m in their largest dimension.
		 * 
		 * Pixel density is largestSize px / BLOCK_SCALE m
		 * That defaults to 32/0.75 or 42.6 px/m, but can vary depending on the shard backing size.
		 */
		ShardTextureSettings.Size size = shardType.getTextureSettings().getSize();
		float largestSize = Math.max(size.width(), size.height());
		float metersPerPixel = BLOCK_SCALE / largestSize;
		float cardHeight = size.height() * metersPerPixel;
		float cardWidth = size.width() * metersPerPixel;

		float halfHeight = cardHeight / 2f;
		float halfWidth = cardWidth / 2f;

		Vector3f dl = new Vector3f(-halfWidth, -halfHeight, 0f);
		Vector3f dr = new Vector3f(halfWidth, -halfHeight, 0f);
		Vector3f ul = new Vector3f(-halfWidth, halfHeight, 0f);
		Vector3f ur = new Vector3f(halfWidth, halfHeight, 0f);

		Vector3f normal = new Vector3f(0, 0, -1);

		//Draw card back
		buf
			.vertex(matrices.peek().getPositionMatrix(), dl.x, dl.y, dl.z)
			.color(1, 1, 1, alpha)
			.texture(0, 1)
			.overlay(overlay)
			.light(actualLight)
			.normal(normal.x(), normal.y(), normal.z());

		buf
			.vertex(matrices.peek().getPositionMatrix(), dr.x, dr.y, dr.z)
			.color(1, 1, 1, alpha)
			.texture(1, 1)
			.overlay(overlay)
			.light(actualLight)
			.normal(normal.x(), normal.y(), normal.z());

		buf
			.vertex(matrices.peek().getPositionMatrix(), ur.x, ur.y, ur.z)
			.color(1, 1, 1, alpha)
			.texture(1, 0)
			.overlay(overlay)
			.light(actualLight)
			.normal(normal.x(), normal.y(), normal.z());

		buf
			.vertex(matrices.peek().getPositionMatrix(), ul.x, ul.y, ul.z)
			.color(1, 1, 1, alpha)
			.texture(0, 0)
			.overlay(overlay)
			.light(actualLight)
			.normal(normal.x(), normal.y(), normal.z());

		//Draw card front
		Vector3f revNormal = normal.mul(-1, -1, -1);
		buf = vertexConsumers.getBuffer(RenderLayer.getItemEntityTranslucentCull(ShardType.getFrontTexture(shard.shardTypeId())));
		buf
			.vertex(matrices.peek().getPositionMatrix(), dl.x, dl.y, dl.z)
			.color(1, 1, 1, alpha)
			.texture(1, 1)
			.overlay(overlay)
			.light(actualLight)
			.normal(matrices.peek(), revNormal.x(), revNormal.y(), revNormal.z());

		buf
			.vertex(matrices.peek().getPositionMatrix(), ul.x, ul.y, ul.z)
			.color(1, 1, 1, alpha)
			.texture(1, 0)
			.overlay(overlay)
			.light(actualLight)
			.normal(matrices.peek(), revNormal.x(), revNormal.y(), revNormal.z());

		buf
			.vertex(matrices.peek().getPositionMatrix(), ur.x, ur.y, ur.z)
			.color(1, 1, 1, alpha)
			.texture(0, 0)
			.overlay(overlay)
			.light(actualLight)
			.normal(matrices.peek(), revNormal.x(), revNormal.y(), revNormal.z());

		buf
			.vertex(matrices.peek().getPositionMatrix(), dr.x, dr.y, dr.z)
			.color(1, 1, 1, alpha)
			.texture(0, 1)
			.overlay(overlay)
			.light(actualLight)
			.normal(matrices.peek(), revNormal.x(), revNormal.y(), revNormal.z());

		ShardIconOffsets.Offset offset = shardType.getOffsets().getNormal();

		shard.icon().ifLeft(stack -> {
			matrices.translate((4 - offset.left()) * metersPerPixel, (8 - offset.up()) * metersPerPixel, -0.005f); //extra -0.002 here to prevent full-cubes from zfighting the card
			matrices.scale(-0.38f, 0.38f, 0.001f /*0.6f*/);

			MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ItemDisplayContext.GUI, actualLight, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), 0);
		});

		shard.icon().ifRight(texId -> {
			VertexConsumer v = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(texId));
			
			Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
			
			int left = offset.left();
			int top = offset.up();
			int right = left + 16;
			int bottom = top + 16;

			v.vertex(positionMatrix, ur.x - (right * metersPerPixel), ur.y - (bottom * metersPerPixel), ur.z - 0.002f)
				.color(0xFF_FFFFFF)
				.texture(1, 1)
				.overlay(overlay)
				.light(actualLight)
				.normal(matrices.peek(), revNormal.x(), revNormal.y(), revNormal.z());

			v.vertex(positionMatrix, ur.x - (right * metersPerPixel), ur.y - (top * metersPerPixel), ur.z - 0.002f)
				.color(0xFF_FFFFFF)
				.texture(1, 0)
				.overlay(overlay)
				.light(actualLight)
				.normal(matrices.peek(), revNormal.x(), revNormal.y(), revNormal.z());

			v.vertex(positionMatrix, ur.x - (left * metersPerPixel), ur.y - (top * metersPerPixel), ur.z - 0.002f)
				.color(0xFF_FFFFFF)
				.texture(0, 0)
				.overlay(overlay)
				.light(actualLight)
				.normal(matrices.peek(), revNormal.x(), revNormal.y(), revNormal.z());

			v.vertex(positionMatrix, ur.x - (left * metersPerPixel), ur.y - (bottom * metersPerPixel), ur.z - 0.002f)
				.color(0xFF_FFFFFF)
				.texture(0, 1)
				.overlay(overlay)
				.light(actualLight)
				.normal(matrices.peek(), revNormal.x(), revNormal.y(), revNormal.z());
		});

		matrices.pop();

		float glowSize = entity.getGlowSize();
		float glowStrength = entity.getGlowStrength();

		if (!collected && glowSize > 0 && glowStrength > 0) {
			matrices.push();

			Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();

			matrices.translate(0.5, 0.5, 0.5);

			matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(camera.getYaw()));
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch() + 90));

			BlockPos pos = entity.getPos();
			double distToShard = Math.sqrt(camera.getPos()
				.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));

			float scale = 2f + (float) MathHelper.clamp((distToShard - 2) * 0.12, 0, glowSize);
			matrices.scale(scale, scale, scale);

			float distFadeAlpha = (float) MathHelper.clamp((distToShard - 1) * 0.1 * glowStrength, 0, 1);

			int color = shardType.glowColor();
			float r = ((color >> 16) & 0xFF) / 255f;
			float g = ((color >> 8) & 0xFF) / 255f;
			float b = (color & 0xFF) / 255f;

			renderGlowingBillboard(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(DISTANCE_HALO_TEX)), r, g, b, distFadeAlpha);
			matrices.translate(0, -0.01, 0);
			renderGlowingBillboard(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(DISTANCE_GLOW_TEX)), 1f, 1f, 1f, distFadeAlpha);

			matrices.pop();
		}
	}

	private void renderGlowingBillboard(MatrixStack matrices, VertexConsumer v, float r, float g, float b, float a) {
		int maxLight = LightmapTextureManager.MAX_LIGHT_COORDINATE;
		int noOverlay = OverlayTexture.DEFAULT_UV;

		v.vertex(matrices.peek().getPositionMatrix(), -0.5f, 0, -0.5f)
			.color(r, g, b, a)
			.texture(0, 0)
			.overlay(noOverlay)
			.light(maxLight)
			.normal(0, 1, 0);

		v.vertex(matrices.peek().getPositionMatrix(), 0.5f, 0, -0.5f)
			.color(r, g, b, a)
			.texture(1, 0)
			.overlay(noOverlay)
			.light(maxLight)
			.normal(0, 1, 0);

		v.vertex(matrices.peek().getPositionMatrix(), 0.5f, 0, 0.5f)
			.color(r, g, b, a)
			.texture(1, 1)
			.overlay(noOverlay)
			.light(maxLight)
			.normal(0, 1, 0);

		v.vertex(matrices.peek().getPositionMatrix(), -0.5f, 0, 0.5f)
			.color(r, g, b, a)
			.texture(0, 1)
			.overlay(noOverlay)
			.light(maxLight)
			.normal(0, 1, 0);
	}
}
