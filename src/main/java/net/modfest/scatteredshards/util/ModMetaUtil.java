package net.modfest.scatteredshards.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.modfest.scatteredshards.ScatteredShards;
import org.apache.commons.lang3.Validate;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Liberally stolen from ModMenu. Thanks ModMenu!
 */
public class ModMetaUtil {
	private static final Map<String, ResourceLocation> iconTextures = new ConcurrentHashMap<>();
	private static final Map<Path, DynamicTexture> modIconCache = new ConcurrentHashMap<>();

	public static DynamicTexture createIcon(ModContainer iconSource, String iconPath) {
		try {
			Path path = iconSource.getPath(iconPath);
			DynamicTexture cachedIcon = modIconCache.get(path);
			if (cachedIcon != null) {
				return cachedIcon;
			}
			cachedIcon = modIconCache.get(path);
			if (cachedIcon != null) {
				return cachedIcon;
			}
			try (InputStream inputStream = Files.newInputStream(path)) {
				NativeImage image = NativeImage.read(Objects.requireNonNull(inputStream));
				Validate.validState(image.getHeight() == image.getWidth(), "Must be square icon");
				DynamicTexture tex = new DynamicTexture(() -> iconPath, image);
				modIconCache.put(path, tex);
				return tex;
			}

		} catch (IllegalStateException e) {
			if (e.getMessage().equals("Must be square icon")) {
				ScatteredShards.LOGGER.error("Mod icon must be a square for icon source {}: {}",
					iconSource.getMetadata().getId(),
					iconPath,
					e
				);
			}

			return null;
		} catch (Throwable t) {
			if (!iconPath.equals("assets/" + iconSource.getMetadata().getId() + "/icon.png")) {
				ScatteredShards.LOGGER.error("Invalid mod icon for icon source {}: {}", iconSource.getMetadata().getId(), iconPath, t);
			}
			return null;
		}
	}

	public static DynamicTexture getMissingIcon() {
		return createIcon(
			FabricLoader.getInstance()
				.getModContainer(ScatteredShards.ID)
				.orElseThrow(() -> new RuntimeException("Cannot get ModContainer for Fabric mod with id " + ScatteredShards.ID)),
			"assets/" + ScatteredShards.ID + "/unknown_icon.png"
		);
	}

	public static DynamicTexture getIcon(ModContainer mod, int preferredSize) {
		if (mod == null) return getMissingIcon();
		ModMetadata meta = mod.getMetadata();
		String modId = meta.getId();
		String iconPath = meta.getIconPath(preferredSize).orElse("assets/" + modId + "/icon.png");
		final String finalIconSourceId = modId;
		ModContainer iconSource = FabricLoader.getInstance()
			.getModContainer(modId)
			.orElseThrow(() -> new RuntimeException("Cannot get ModContainer for Fabric mod with id " + finalIconSourceId));
		DynamicTexture icon = createIcon(iconSource, iconPath);
		if (icon == null) return getMissingIcon();
		return icon;
	}

	public static ResourceLocation touchModIcon(String modId) {
		return iconTextures.computeIfAbsent(modId, id -> {
			ResourceLocation iconTexture = ResourceLocation.fromNamespaceAndPath(ScatteredShards.ID, modId + "_icon");
			Minecraft.getInstance().getTextureManager().register(iconTexture, ModMetaUtil.getIcon(FabricLoader.getInstance().getModContainer(modId).orElse(null), 16));
			return iconTexture;
		});
	}

	public static void touchIconTexture(ResourceLocation iconTexture) {
		if (!iconTexture.getNamespace().equals(ScatteredShards.ID) || !iconTexture.getPath().endsWith("_icon")) return;
		touchModIcon(iconTexture.getPath().substring(0, iconTexture.getPath().length() - "_icon".length()));
	}
}
