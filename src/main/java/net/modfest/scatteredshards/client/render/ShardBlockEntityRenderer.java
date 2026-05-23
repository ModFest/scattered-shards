package net.modfest.scatteredshards.client.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec3;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.shard.*;
import net.modfest.scatteredshards.block.ShardBlockEntity;
import net.modfest.scatteredshards.client.Quaternionsf;
import net.modfest.scatteredshards.util.ModMetaUtil;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ShardBlockEntityRenderer implements BlockEntityRenderer<ShardBlockEntity, ShardBlockEntityRenderer.ShardEntityRenderState> {
	public static final float BLOCK_SCALE = 0.75f;

	private static final Quaternionf ITEM_LIGHT_ROTATION_3D = Quaternionsf.rotateDegreesXYZ(-15, 15, 0);
	private static final Quaternionf ITEM_LIGHT_ROTATION_FLAT = Axis.XP.rotationDegrees(-45);

	private static final Identifier DISTANCE_GLOW_TEX = ScatteredShards.id("textures/entity/shard_distance_glow.png");
	private static final Identifier DISTANCE_HALO_TEX = ScatteredShards.id("textures/entity/shard_distance_halo.png");

	private final ItemModelResolver itemModelResolver;

	public ShardBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
		itemModelResolver = ctx.itemModelResolver();
	}

	private void renderGlowingBillboard(PoseStack.Pose pose, VertexConsumer v, float r, float g, float b, float a) {
		int maxLight = LightCoordsUtil.FULL_BRIGHT;
		int noOverlay = OverlayTexture.NO_OVERLAY;

		v.addVertex(pose.pose(), -0.5f, 0, -0.5f)
			.setColor(r, g, b, a)
			.setUv(0, 0)
			.setOverlay(noOverlay)
			.setLight(maxLight)
			.setNormal(0, 1, 0);

		v.addVertex(pose.pose(), 0.5f, 0, -0.5f)
			.setColor(r, g, b, a)
			.setUv(1, 0)
			.setOverlay(noOverlay)
			.setLight(maxLight)
			.setNormal(0, 1, 0);

		v.addVertex(pose.pose(), 0.5f, 0, 0.5f)
			.setColor(r, g, b, a)
			.setUv(1, 1)
			.setOverlay(noOverlay)
			.setLight(maxLight)
			.setNormal(0, 1, 0);

		v.addVertex(pose.pose(), -0.5f, 0, 0.5f)
			.setColor(r, g, b, a)
			.setUv(0, 1)
			.setOverlay(noOverlay)
			.setLight(maxLight)
			.setNormal(0, 1, 0);
	}

	public static class ShardEntityRenderState extends BlockEntityRenderState {
		ShardBlockEntity.Animations animations;
		Shard shard;
		ItemStackRenderState itemState;
		float partialTicks;
		float glowSize;
		float glowStrength;
	}

	@Override
	public ShardEntityRenderState createRenderState() {
		return new ShardEntityRenderState();
	}

	@Override
	public void extractRenderState(ShardBlockEntity blockEntity, ShardEntityRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

		state.partialTicks = partialTicks;
		state.animations = blockEntity.getAnimations();
		state.shard = blockEntity.getShard(ScatteredShardsAPI.getClientLibrary());

		if (state.shard == null) {
			//Let's make one up!
			state.shard = Shard.MISSING_SHARD;
		}

		state.shard.icon().ifLeft(stack -> {
			state.itemState = new ItemStackRenderState();
			this.itemModelResolver.updateForTopItem(state.itemState, stack, ItemDisplayContext.GUI, blockEntity.getLevel(), null, 0);
		});

		state.glowSize = blockEntity.getGlowSize();
		state.glowStrength = blockEntity.getGlowStrength();
	}

	@Override
	public void submit(ShardEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {

		boolean collected = state.animations.collected();
		final int actualLight = collected ? state.lightCoords : LightCoordsUtil.FULL_BRIGHT;
		var overlay = state.breakProgress == null ? OverlayTexture.NO_OVERLAY : state.breakProgress.progress();

		var shard = state.shard;

		shard.icon().ifRight(ModMetaUtil::touchIconTexture);
		ShardType shardType = ScatteredShardsAPI.getClientLibrary().shardTypes().get(shard.shardTypeId()).orElse(ShardType.MISSING);

		poseStack.pushPose();

		poseStack.translate(0.5, 0.5, 0.5);
		poseStack.mulPose(Quaternionsf.rotateXYZ(
			0,
			/* rot */ state.animations.getAngle(state.partialTicks),
			/* tilt */ Mth.PI / 8.F
		));

		float alpha = collected ? 0.5f : 1f;


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


		//VertexConsumer buf = submitNodeCollector.getBuffer(RenderType.itemEntityTranslucentCull(ShardType.getBackingTexture(shard.shardTypeId())));
		submitNodeCollector.submitCustomGeometry(
			poseStack,
			RenderTypes.itemTranslucent(ShardType.getBackingTexture(shard.shardTypeId())),
			(pose, buf) -> {
				//Draw card back
				buf
					.addVertex(pose.pose(), dl.x, dl.y, dl.z)
					.setColor(1, 1, 1, alpha)
					.setUv(0, 1)
					.setOverlay(overlay)
					.setLight(actualLight)
					.setNormal(normal.x(), normal.y(), normal.z());

				buf
					.addVertex(pose.pose(), dr.x, dr.y, dr.z)
					.setColor(1, 1, 1, alpha)
					.setUv(1, 1)
					.setOverlay(overlay)
					.setLight(actualLight)
					.setNormal(normal.x(), normal.y(), normal.z());

				buf
					.addVertex(pose.pose(), ur.x, ur.y, ur.z)
					.setColor(1, 1, 1, alpha)
					.setUv(1, 0)
					.setOverlay(overlay)
					.setLight(actualLight)
					.setNormal(normal.x(), normal.y(), normal.z());

				buf
					.addVertex(pose.pose(), ul.x, ul.y, ul.z)
					.setColor(1, 1, 1, alpha)
					.setUv(0, 0)
					.setOverlay(overlay)
					.setLight(actualLight)
					.setNormal(normal.x(), normal.y(), normal.z());

			}
		);

		//Draw card front
		Vector3f revNormal = normal.mul(-1, -1, -1);


		submitNodeCollector.submitCustomGeometry(
			poseStack,
			RenderTypes.itemTranslucent(ShardType.getFrontTexture(shard.shardTypeId())),
			(pose, buf) -> {
//				buf = submitNodeCollector.getBuffer(RenderType.itemEntityTranslucentCull(ShardType.getFrontTexture(shard.shardTypeId())));
				buf
					.addVertex(pose.pose(), dl.x, dl.y, dl.z)
					.setColor(1, 1, 1, alpha)
					.setUv(1, 1)
					.setOverlay(overlay)
					.setLight(actualLight)
					.setNormal(pose, revNormal.x(), revNormal.y(), revNormal.z());

				buf
					.addVertex(pose.pose(), ul.x, ul.y, ul.z)
					.setColor(1, 1, 1, alpha)
					.setUv(1, 0)
					.setOverlay(overlay)
					.setLight(actualLight)
					.setNormal(pose, revNormal.x(), revNormal.y(), revNormal.z());

				buf
					.addVertex(pose.pose(), ur.x, ur.y, ur.z)
					.setColor(1, 1, 1, alpha)
					.setUv(0, 0)
					.setOverlay(overlay)
					.setLight(actualLight)
					.setNormal(pose, revNormal.x(), revNormal.y(), revNormal.z());

				buf
					.addVertex(pose.pose(), dr.x, dr.y, dr.z)
					.setColor(1, 1, 1, alpha)
					.setUv(0, 1)
					.setOverlay(overlay)
					.setLight(actualLight)
					.setNormal(pose, revNormal.x(), revNormal.y(), revNormal.z());
			}
		);


		ShardIconOffsets.Offset offset = shardType.getOffsets().getNormal();

		shard.icon().ifLeft(stack -> {
			final Minecraft client = Minecraft.getInstance();
			final GpuBufferSlice shaderLights = RenderSystem.getShaderLights();

			int left = offset.left();
			int top = offset.up();
			// Constant halved as 16 overshot it.
			int right = left + 8;
			int bottom = top + 8;

			//extra -0.002 here to prevent full-cubes from zfighting the card
			poseStack.translate(ur.x-(right * metersPerPixel), ur.y-(bottom * metersPerPixel), -0.005f);

			poseStack.scale(0.38f, 0.38f, 0.001f /*0.6f*/);
			poseStack.mulPose(Axis.YP.rotationDegrees(180));

			// Tinkering borrowed from Glowcase's Item Acceptor
			// Thank you Chai :3
			if (state.itemState.usesBlockLight()) {
				poseStack.last().normal().rotate(ITEM_LIGHT_ROTATION_3D);
				client.gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
			} else {
				poseStack.last().normal().rotate(ITEM_LIGHT_ROTATION_FLAT);
				client.gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_FLAT);
			}

			state.itemState.submit(poseStack, submitNodeCollector, actualLight, OverlayTexture.NO_OVERLAY, 0);

			RenderSystem.setShaderLights(shaderLights);
		});


		shard.icon().ifRight(texId -> {
			submitNodeCollector.submitCustomGeometry(
				poseStack,
				RenderTypes.entityCutout(texId),
				(pose, buf) -> {


					Matrix4f positionMatrix = pose.pose();

					int left = offset.left();
					int top = offset.up();
					int right = left + 16;
					int bottom = top + 16;

					buf.addVertex(positionMatrix, ur.x - (right * metersPerPixel), ur.y - (bottom * metersPerPixel), ur.z - 0.002f)
						.setColor(0xFF_FFFFFF)
						.setUv(1, 1)
						.setOverlay(overlay)
						.setLight(actualLight)
						.setNormal(pose, revNormal.x(), revNormal.y(), revNormal.z());

					buf.addVertex(positionMatrix, ur.x - (right * metersPerPixel), ur.y - (top * metersPerPixel), ur.z - 0.002f)
						.setColor(0xFF_FFFFFF)
						.setUv(1, 0)
						.setOverlay(overlay)
						.setLight(actualLight)
						.setNormal(pose, revNormal.x(), revNormal.y(), revNormal.z());

					buf.addVertex(positionMatrix, ur.x - (left * metersPerPixel), ur.y - (top * metersPerPixel), ur.z - 0.002f)
						.setColor(0xFF_FFFFFF)
						.setUv(0, 0)
						.setOverlay(overlay)
						.setLight(actualLight)
						.setNormal(pose, revNormal.x(), revNormal.y(), revNormal.z());

					buf.addVertex(positionMatrix, ur.x - (left * metersPerPixel), ur.y - (bottom * metersPerPixel), ur.z - 0.002f)
						.setColor(0xFF_FFFFFF)
						.setUv(0, 1)
						.setOverlay(overlay)
						.setLight(actualLight)
						.setNormal(pose, revNormal.x(), revNormal.y(), revNormal.z());

				}
			);
		});

		poseStack.popPose();

		float glowSize = state.glowSize;
		float glowStrength = state.glowStrength;

		if (!collected && glowSize > 0 && glowStrength > 0) {
			poseStack.pushPose();

			poseStack.translate(0.5, 0.5, 0.5);
			poseStack.mulPose(Quaternionsf.rotateDegreesYXZ(-camera.yRot, camera.xRot + 90, 0));

			BlockPos pos = state.blockPos;
			double distToShard = Math.sqrt(camera.pos
				.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));

			float scale = 2f + (float) Mth.clamp((distToShard - 2) * 0.12, 0, glowSize);
			poseStack.scale(scale, scale, scale);

			float distFadeAlpha = (float) Mth.clamp((distToShard - 1) * 0.1 * glowStrength, 0, 1);

			int color = shardType.glowColor();
			float r = ((color >> 16) & 0xFF) / 255f;
			float g = ((color >> 8) & 0xFF) / 255f;
			float b = (color & 0xFF) / 255f;

			submitNodeCollector.submitCustomGeometry(
				poseStack,
				RenderTypes.entityTranslucentEmissive(DISTANCE_HALO_TEX),
				(pose, buf) -> renderGlowingBillboard(pose, buf, r, g, b, distFadeAlpha)
			);

			poseStack.translate(0, -0.01, 0);

			submitNodeCollector.submitCustomGeometry(
				poseStack,
				RenderTypes.entityTranslucentEmissive(DISTANCE_GLOW_TEX),
				(pose, buf) -> renderGlowingBillboard(pose, buf, 1, 1, 1, distFadeAlpha)
			);

			poseStack.popPose();
		}
	}
}
