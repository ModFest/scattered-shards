package net.modfest.scatteredshards.client.screen;

import org.quiltmc.loader.api.QuiltLoader;

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
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.client.screen.widget.WAlternativeToggle;
import net.modfest.scatteredshards.client.screen.widget.WLayoutBox;
import net.modfest.scatteredshards.client.screen.widget.WLeftRightPanel;
import net.modfest.scatteredshards.client.screen.widget.WProtectableField;
import net.modfest.scatteredshards.client.screen.widget.WShardPanel;
import net.modfest.scatteredshards.component.ScatteredShardsComponents;
import net.modfest.scatteredshards.networking.ScatteredShardsNetworking;

public class ShardCreatorGuiDescription extends LightweightGuiDescription {
	public static final String BASE_KEY = "gui.scattered_shards.creator.";
	public static final Text TITLE_TEXT = Text.translatable(BASE_KEY + "title");
	public static final Text NAME_TEXT = Text.translatable(BASE_KEY + "field.name");
	public static final Text LORE_TEXT = Text.translatable(BASE_KEY + "field.lore");
	public static final Text HINT_TEXT = Text.translatable(BASE_KEY + "field.hint");
	public static final Text TEXTURE_TEXT = Text.translatable(BASE_KEY + "field.texture");
	public static final Text ICON_TEXTURE_TEXT = Text.translatable(BASE_KEY + "icon.texture");
	public static final Text ICON_ITEM_TEXT = Text.translatable(BASE_KEY + "icon.item");
	public static final Text ITEM_TEXT = Text.translatable(BASE_KEY + "field.item.id");
	public static final Text NBT_TEXT = Text.translatable(BASE_KEY + "field.item.nbt");
	public static final Text USE_MOD_ICON_TEXT = Text.translatable(BASE_KEY + "toggle.mod_icon");
	public static final Text SAVE_TEXT = Text.translatable(BASE_KEY + "button.save");
	
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
		var id = Identifier.tryParse(path);
		if (id == null) {
			return null;
		}
		var resource = MinecraftClient.getInstance().getResourceManager().getResource(id);
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
			.setChangedListener((it)-> {
				this.item = null;
				var id = Identifier.tryParse(it);
				if (id != null) {
					this.item = Registries.ITEM.containsId(id)
						? Registries.ITEM.get(id)
						: null;
				}
				updateItemIcon();
			});
	
	public WProtectableField nbtField = new WProtectableField(NBT_TEXT)
			.setChangedListener((it) -> {
				try {
					this.itemNbt = null;
					this.itemNbt = StringNbtReader.parse(it);
				} catch (CommandSyntaxException ignored) {
				}
				updateItemIcon();
			});
	
	
	public WButton saveButton = new WButton(SAVE_TEXT)
		.setOnClick(() -> {
			ScatteredShardsNetworking.c2sModifyShard(shardId, shard);
		});
	
	private Item item = null;
	private NbtCompound itemNbt = null;
	private Identifier iconPath = null;
	
	
	private void updateItemIcon() {
		if (item == null) {
			shard.setIcon(Shard.MISSING_ICON);
			return;
		}
		var stack = item.getDefaultStack();
		if (itemNbt != null) {
			stack.setNbt(itemNbt);
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
		shardPanel.setShard(shard);
		this.modIcon = QuiltLoader.getModContainer(modId)
				.map(it -> it.metadata().icon(16))
				.filter(it -> it != null && it.startsWith("assets/"))
				.map(it -> it.substring("assets/".length()))
				.map(it -> {
					int firstSlash = it.indexOf("/");
					String namespace = it.substring(0,firstSlash);
					String path = it.substring(firstSlash+1);
					
					return new Identifier(namespace, path);
				})
				.orElse(Shard.MISSING_ICON.right().get()); //TODO: Deal with non-resource icons here.
		Shard.getSourceForModId(modId).ifPresent(shard::setSource);
		shard.setSourceId(new Identifier(modId, "shard_pack"));
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
				70-18-4);
		
		cardPanel.add(textureIconPanel);
		cardPanel.add(itemIconPanel);
		iconToggle.setLeft();
		cardPanel.setSelectedIndex(0);
		
		textureIconPanel.add(textureField);
		textureIconPanel.add(textureToggle);
		
		itemIconPanel.add(itemField);
		itemIconPanel.add(nbtField);
		
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
			return new Screen(
				shardType.createModId(modId),
				Shard.emptyOfType(shardType),
				modId
			);
		}

		public static Screen editShard(Shard shard) {
			Identifier shardId = ScatteredShardsComponents.getShardLibrary(MinecraftClient.getInstance().world).getId(shard);
			String modId = shardId.getNamespace();
			return new Screen(shardId, shard, modId);
		}
	}
}
