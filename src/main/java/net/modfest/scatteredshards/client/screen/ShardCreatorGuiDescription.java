package net.modfest.scatteredshards.client.screen;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WBox;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WTabPanel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.WToggleButton;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.shard.Shard;

import java.util.function.Consumer;

public class ShardCreatorGuiDescription extends LightweightGuiDescription {

	public static class Screen extends CottonClientScreen {

		public Screen(Shard shard, String modId) {
			super(new ShardCreatorGuiDescription(shard, modId));
		}

		public Screen() {
			this(Shard.MISSING_SHARD.copy(), ScatteredShards.ID);
		}
	}

	private Shard shard;
	private Identifier modIcon;

	private Item item = null;
	private NbtCompound itemNbt = null;
	private Identifier iconPath = null;

	private WTextField addStringField(WBox parent, String name, Consumer<String> action, int maxLength) {
		var field = new WProtectableField(Text.translatable("gui.scattered_shards.creator.field." + name));
		field.setMaxLength(maxLength);
		field.setChangedListener(action);
		parent.add(field, 185, 18);
		return field;
	}
	/*
	private WTextField addStringField(WPlainPanel root, String name, Consumer<String> action) {
		return addStringField(root, name, action, Integer.MAX_VALUE);
	}*/

	private WTextField addTextField(WBox parent, String name, Consumer<Text> action, int maxLength) {
		return addStringField(parent, name, str -> action.accept(Text.literal(str)), maxLength);
	}
	
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

	private void updateTextureIcon(boolean useModIcon) {
		if (useModIcon) {
			shardPanel.setIcon(Either.right(modIcon));
		} else if (iconPath != null) {
			shardPanel.setIcon(Either.right(iconPath));
		} else {
			shardPanel.setIcon(Shard.MISSING_ICON);
		}
	}
	
	WBox editorPanel = new WBox(Axis.VERTICAL);
	WShardPanel shardPanel = new WShardPanel();
	
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
		editorPanel.setHorizontalAlignment(HorizontalAlignment.LEFT);
		
		var title = new WLabel(Text.translatable("gui.scattered_shards.creator.title"));
		title.setColor(titleColor);
		editorPanel.add(title, 185, 18);
		
		var name = addTextField(editorPanel, "name", (it) -> shard.setName(it), 36);
		var lore = addTextField(editorPanel, "lore", (it) -> shard.setLore(it), 256);
		var hint = addTextField(editorPanel, "hint", (it) -> shard.setHint(it), 256);
		
		//WSlider textureItemSlider = new WSlider();
		//var toggles = new WBox(Axis.HORIZONTAL);
		//var textureToggle = new WButton(Text.translatable("gui.scattered_shards.creator.icon.texture"));
		//var itemToggle = new WButton(Text.translatable("gui.scattered_shards.creator.icon.item"));
		//toggles.add(textureToggle, 90, 18);
		//toggles.add(itemToggle, 90, 18);
		//textureToggle.setEnabled(false);
		//editorPanel.add(toggles, 185, 18);
		
		WAlternativeToggle toggle = new WAlternativeToggle();
		editorPanel.add(toggle, 185, 18);
		toggle.leftLabel = Text.translatable("gui.scattered_shards.creator.icon.texture");
		toggle.rightLabel = Text.translatable("gui.scattered_shards.creator.icon.item");
		
		
		var textureField = addStringField(editorPanel, "texture", path -> {
			this.iconPath = !path.isBlank()
				? Identifier.tryParse(path)
				: null;
			updateTextureIcon(false);
		}, 256);
		
		toggle.onLeft(() -> {
			textureField.setEditable(true);
			updateTextureIcon(true);
		});
		
		toggle.onRight(() -> {
			textureField.setEditable(false);
			updateTextureIcon(false);
		});
		
		/*

		addStringField(itemSettings, "item.id", str -> {
			Item item = null;
			var id = Identifier.tryParse(str);
			if (id != null) {
				item = Registries.ITEM.containsId(id)
					? Registries.ITEM.get(id)
					: null;
			}
			this.item = item;
			updateItemIcon();
		}, 0);

		addStringField(itemSettings, "item.nbt", str -> {
			try {
				this.itemNbt = null;
				this.itemNbt = StringNbtReader.parse(str);
			} catch (CommandSyntaxException ignored) {
			}
			updateItemIcon();
		}, 24);

		root.add(textureSettings, 0, 124);

		textureToggle.setOnClick(() -> {
			textureToggle.setEnabled(false);
			itemToggle.setEnabled(true);
			root.add(textureSettings, 0, 124);
			root.remove(itemSettings);
			root.validate(this);
		});

		itemToggle.setOnClick(() -> {
			itemToggle.setEnabled(false);
			textureToggle.setEnabled(true);
			root.add(itemSettings, 0, 124);
			root.remove(textureSettings);
			root.validate(this);
		});
		*/
		root.validate(this);
	}
	
	@Override
	public void addPainters() {
		//Don't add the default root painter.
	}
}
