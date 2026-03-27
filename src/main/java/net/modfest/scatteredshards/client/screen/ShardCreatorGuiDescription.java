package net.modfest.scatteredshards.client.screen;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WCardPanel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WToggleButton;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.client.screen.widget.WAlternativeToggle;
import net.modfest.scatteredshards.client.screen.widget.WLayoutBox;
import net.modfest.scatteredshards.client.screen.widget.WLeftRightPanel;
import net.modfest.scatteredshards.client.screen.widget.WProtectableField;
import net.modfest.scatteredshards.client.screen.widget.WShardPanel;
import net.modfest.scatteredshards.networking.C2SModifyShard;
import net.modfest.scatteredshards.util.ModMetaUtil;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ShardCreatorGuiDescription extends LightweightGuiDescription {
	public static final Component TITLE_TEXT = Component.translatable("gui.scattered_shards.creator.title");
	public static final Component NAME_TEXT = Component.translatable("gui.scattered_shards.creator.field.name");
	public static final Component LORE_TEXT = Component.translatable("gui.scattered_shards.creator.field.lore");
	public static final Component HINT_TEXT = Component.translatable("gui.scattered_shards.creator.field.hint");
	public static final Component TEXTURE_TEXT = Component.translatable("gui.scattered_shards.creator.field.texture");
	public static final Component ICON_TEXTURE_TEXT = Component.translatable("gui.scattered_shards.creator.icon.texture");
	public static final Component ICON_ITEM_TEXT = Component.translatable("gui.scattered_shards.creator.icon.item");
	public static final Component ITEM_TEXT = Component.translatable("gui.scattered_shards.creator.field.item.id");
	public static final Component USE_MOD_ICON_TEXT = Component.translatable("gui.scattered_shards.creator.toggle.mod_icon");
	public static final Component SAVE_TEXT = Component.translatable("gui.scattered_shards.creator.button.save");
	private static final String PREVIOUS_VALUE = "<previous_value>";

	private Identifier shardId;
	private Shard shard;
	private Identifier modIcon;

	WLayoutBox editorPanel = new WLayoutBox(Axis.VERTICAL);
	WShardPanel shardPanel = new WShardPanel();

	WLabel titleLabel = new WLabel(TITLE_TEXT);

	/*
	 * No matter how much intelliJ complains, these lambdas cannot be changed into method references due to when they
	 * bind. Shard is null right now. Using the full lambda captures the shard variable instead of the [nonexistant]
	 * method.
	 */
	public WProtectableField nameField = new WProtectableField(NAME_TEXT)
		.setTextChangedListener(it -> shard.setName(it))
		.setMaxLength(32);
	public WProtectableField loreField = new WProtectableField(LORE_TEXT)
		.setTextChangedListener(it -> shard.setLore(it))
		.setMaxLength(70);
	public WProtectableField hintField = new WProtectableField(HINT_TEXT)
		.setTextChangedListener(it -> shard.setHint(it))
		.setMaxLength(70);

	public WAlternativeToggle iconToggle = new WAlternativeToggle(ICON_TEXTURE_TEXT, ICON_ITEM_TEXT);
	public WCardPanel cardPanel = new WCardPanel();
	public WLayoutBox textureIconPanel = new WLayoutBox(Axis.VERTICAL);
	public WLayoutBox itemIconPanel = new WLayoutBox(Axis.VERTICAL);

	public static Identifier parseTexture(String path) {
		if (path.isBlank()) {
			return null;
		}
		Identifier id = Identifier.tryParse(path);
		if (id == null) {
			return null;
		}
		Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(id);
		return resource.isPresent() ? id : null;
	}

	public WProtectableField textureField = new WProtectableField(TEXTURE_TEXT)
		.setChangedListener(path -> {
			this.iconPath = parseTexture(path);
			updateTextureIcon();
		});

	public WToggleButton textureToggle = new WToggleButton(USE_MOD_ICON_TEXT)
		.setOnToggle(on -> {
			textureField.setEditable(!on);
			updateTextureIcon();
		});

	public WProtectableField itemField = new WProtectableField(ITEM_TEXT)
		.setChangedListener((it) -> {
			if (it.isBlank() || Objects.equals(it, PREVIOUS_VALUE)) {
				return;
			}

			try {
				updateItem(new StringReader(it));
			} catch (Exception ignored) {
			}

			updateItemIcon();
		});

	public WButton saveButton = new WButton(SAVE_TEXT)
		.setOnClick(() -> ClientPlayNetworking.send(new C2SModifyShard(shardId, shard)));

	private Item item = null;
	private DataComponentMap itemComponents = DataComponentMap.EMPTY;
	private Identifier iconPath = null;


	private void updateItem(StringReader reader) throws CommandSyntaxException {
		var itemReader = new ItemParser(Minecraft.getInstance().level.registryAccess());
		var result = itemReader.parse(reader);

		this.item = result.item().value();
		DataComponentMap.Builder mapBuilder = DataComponentMap.builder();
		mapBuilder.addAll(result.components().split().added());
		this.itemComponents = mapBuilder.build();
	}

	private void updateItemIcon() {
		if (item == null) {
			shard.setIcon(Shard.MISSING_ICON);
			return;
		}
		ItemStack stack = item.getDefaultInstance();
		if (!itemComponents.isEmpty()) {
			stack.applyComponents(itemComponents);
		}
		shard.setIcon(Either.left(stack));
	}

	private void updateTextureIcon() {
		boolean useModIcon = textureToggle.getToggle();
		if (useModIcon) {
			shard.setIcon(Either.right(modIcon));
		} else if (iconPath != null) {
			shard.setIcon(Either.right(iconPath));
		} else {
			shard.setIcon(Shard.MISSING_ICON);
		}
	}

	public ShardCreatorGuiDescription(Identifier shardId, Shard shard, String modId) {
		this(shardId);
		this.shard = shard;

		this.modIcon = ModMetaUtil.touchModIcon(modId);
		shard.setSourceId(Identifier.fromNamespaceAndPath(modId, "shard_pack"));

		// Initialize field values
		this.nameField.setText(shard.name().tryCollapseToString());
		this.loreField.setText(shard.lore().tryCollapseToString());
		this.hintField.setText(shard.hint().tryCollapseToString());
		shard.icon().ifRight(a -> {
			this.iconToggle.setLeft();
			if (Objects.equals(a, modIcon)) {
				this.textureToggle.setToggle(true);
			} else {
				this.textureToggle.setToggle(false);
				if (Objects.equals(a, Shard.MISSING_ICON_ID)) {
					this.textureField.setText("");
				} else {
					this.textureField.setText(a.toString());
				}
			}
		});
		shard.icon().ifLeft(itemStack -> {
			this.iconToggle.setRight();

			if (itemStack.getComponentsPatch().isEmpty()) {
				this.itemField.setText(BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString());
			} else {
				// TODO
				this.itemField.setText(PREVIOUS_VALUE);
			}

			this.item = itemStack.getItem();
			this.itemComponents = itemStack.getComponents();
			updateItemIcon();
		});

		shardPanel.setShard(shard);
	}

	public ShardCreatorGuiDescription(Identifier shardId) {
		this.shardId = shardId;

		WLeftRightPanel root = new WLeftRightPanel(editorPanel, shardPanel);
		this.setRootPanel(root);

		editorPanel.setBackgroundPainter(BackgroundPainter.VANILLA);
		editorPanel.setInsets(Insets.ROOT_PANEL);
		editorPanel.setSpacing(3);
		editorPanel.setHorizontalAlignment(HorizontalAlignment.LEFT);

		editorPanel.add(titleLabel);
		editorPanel.add(nameField);
		editorPanel.add(loreField);
		editorPanel.add(hintField);

		editorPanel.add(iconToggle);
		editorPanel.add(cardPanel,
			editorPanel.getWidth() - editorPanel.getInsets().left() - editorPanel.getInsets().right(),
			70 - 18 - 4);

		cardPanel.add(textureIconPanel);
		cardPanel.add(itemIconPanel);
		iconToggle.setLeft();
		cardPanel.setSelectedIndex(0);

		textureIconPanel.add(textureField);
		textureIconPanel.add(textureToggle);

		itemIconPanel.add(itemField);

		editorPanel.add(saveButton);

		iconToggle.onLeft(() -> {
			cardPanel.setSelectedIndex(0);
			updateTextureIcon();
		}).onRight(() -> {
			cardPanel.setSelectedIndex(1);
			updateItemIcon();
		});

		root.validate(this);
	}

	@Override
	public void addPainters() {
		//Don't add the default root painter.
	}

	public static class Screen extends CottonClientScreen {

		public Screen(Identifier shardId, Shard shard, String modId) {
			super(new ShardCreatorGuiDescription(shardId, shard, modId));
		}

		public static Screen newShard(String modId, ShardType shardType) {
			Identifier shardTypeId = ScatteredShardsAPI.getClientLibrary().shardTypes().get(shardType).orElse(ShardType.MISSING_ID);
			return new Screen(
				ShardType.createModId(shardTypeId, modId),
				Shard.emptyOfType(shardTypeId),
				modId
			);
		}

		public static Screen editShard(Shard shard) {
			Identifier shardId = ScatteredShardsAPI.getClientLibrary().shards().get(shard).orElse(Shard.MISSING_SHARD_SOURCE);
			String modId = shardId.getNamespace();
			return new Screen(shardId, shard, modId);
		}
	}
}
