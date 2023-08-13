package net.modfest.scatteredshards.client.screen;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WCardPanel;
import io.github.cottonmc.cotton.gui.widget.WToggleButton;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.client.screen.widget.WAlternativeToggle;
import net.modfest.scatteredshards.client.screen.widget.WLayoutBox;
import net.modfest.scatteredshards.client.screen.widget.WLeftRightPanel;
import net.modfest.scatteredshards.client.screen.widget.WProtectableField;
import net.modfest.scatteredshards.client.screen.widget.WShardPanel;

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
			.setTextChangedListener(it -> shard.setLore(it));
	public WProtectableField hintField = new WProtectableField(HINT_TEXT)
			.setTextChangedListener(it -> shard.setHint(it));
	
	public WAlternativeToggle iconToggle = new WAlternativeToggle(ICON_TEXTURE_TEXT, ICON_ITEM_TEXT);
	public WCardPanel cardPanel = new WCardPanel();
	public WLayoutBox textureIconPanel = new WLayoutBox(Axis.VERTICAL);
	public WLayoutBox itemIconPanel = new WLayoutBox(Axis.VERTICAL);
	
	public WProtectableField textureField = new WProtectableField(TEXTURE_TEXT)
			.setChangedListener(path -> {
				this.iconPath = !path.isBlank()
					? Identifier.tryParse(path)
					: null;
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
	
	
	public WButton saveButton = new WButton(SAVE_TEXT);
	
	
	private Item item = null;
	private NbtCompound itemNbt = null;
	private Identifier iconPath = null;
	
	
	private void updateItemIcon() {
		if (item == null) {
			shardPanel.setIcon(Shard.MISSING_ICON);
			return;
		}
		var stack = item.getDefaultStack();
		if (itemNbt != null) {
			stack.setNbt(itemNbt);
		}
		shardPanel.setIcon(Either.left(stack));
	}
	
	private void updateTextureIcon() {
		boolean useModIcon = textureToggle.getToggle();
		if (useModIcon) {
			shardPanel.setIcon(Either.right(modIcon));
		} else if (iconPath != null) {
			shardPanel.setIcon(Either.right(iconPath));
		} else {
			shardPanel.setIcon(Shard.MISSING_ICON);
		}
	}
	
	
	public ShardCreatorGuiDescription(Shard shard, String modId) {
		this();
		shardPanel.setShard(shard);
		this.modIcon = new Identifier(modId, "icon.png");
		Shard.getSourceForModId(modId).ifPresent(shard::setSource);
	}
	
	
	public ShardCreatorGuiDescription() {
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

		public Screen(Shard shard, String modId) {
			super(new ShardCreatorGuiDescription(shard, modId));
		}

		public Screen() {
			this(Shard.MISSING_SHARD.copy(), ScatteredShards.ID);
		}
	}
}
