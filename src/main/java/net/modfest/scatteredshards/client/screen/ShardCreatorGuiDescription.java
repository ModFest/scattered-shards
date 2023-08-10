package net.modfest.scatteredshards.client.screen;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.WToggleButton;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.shard.Shard;

import java.util.function.Consumer;

public class ShardCreatorGuiDescription extends ShardGuiDescription {

	public static class Screen extends CottonClientScreen {

		public Screen(Shard shard, String modId) {
			super(new ShardCreatorGuiDescription(shard, modId));
		}

		public Screen() {
			this(Shard.MISSING_SHARD, ScatteredShards.ID);
		}
	}

	private final Shard shard;
	private final Identifier modIcon;

	private Item item = null;
	private NbtCompound itemNbt = null;
	private Identifier iconPath = null;

	private WTextField addStringField(WPlainPanel root, String name, Consumer<String> action, int maxLength, int y) {
		var field = new WTextField(Text.translatable("gui.scattered_shards.creator.field." + name));
		field.setMaxLength(maxLength);
		field.setChangedListener(action);
		root.add(field, 0, y, 172, 20);
		return field;
	}

	private WTextField addStringField(WPlainPanel root, String name, Consumer<String> action, int y) {
		return addStringField(root, name, action, Integer.MAX_VALUE, y);
	}

	private WTextField addTextField(WPlainPanel root, String name, Consumer<Text> action, int maxLength, int y) {
		return addStringField(root, name, str -> action.accept(Text.literal(str)), maxLength, y);
	}

	private void updateItemIcon() {
		if (item == null) {
			shard.setIcon(Shard.MISSING_ICON);
			return;
		}
		var stack = item.getDefaultStack();
		if (itemNbt != null) {
			stack.setNbt(itemNbt);
		}
		shard.setIcon(stack);
	}

	private void updateTextureIcon(boolean useModIcon) {
		if (useModIcon) {
			shard.setIcon(Either.right(modIcon));
		} else if (iconPath != null) {
			shard.setIcon(Either.right(iconPath));
		} else {
			shard.setIcon(Shard.MISSING_ICON);
		}
	}

	public ShardCreatorGuiDescription(Shard shard, String modId) {
		this.shard = shard;
		this.modIcon = new Identifier(modId, "icon.png");
		Shard.getSourceForModId(modId).ifPresent(shard::setSource);

		var title = new WLabel(Text.translatable("gui.scattered_shards.creator.title"));
		title.setColor(titleColor);
		root.add(title, 0, 0);

		addTextField(root, "name", shard::setName, 14, 20);
		addTextField(root, "lore", shard::setLore, 21, 44);
		addTextField(root, "hint", shard::setHint, 21, 68);

		var textureToggle = new WButton(Text.translatable("gui.scattered_shards.creator.icon.texture"));
		textureToggle.setEnabled(false);
		var textureSettings = new WPlainPanel();

		var itemToggle = new WButton(Text.translatable("gui.scattered_shards.creator.icon.item"));
		var itemSettings = new WPlainPanel();

		var textureField = addStringField(textureSettings, "texture", path -> {
			this.iconPath = !path.isBlank()
				? Identifier.tryParse(path)
				: null;
			updateTextureIcon(false);
		}, 0);

		var toggle = new WToggleButton(Text.translatable("gui.scattered_shards.creator.toggle.mod_icon"));
		toggle.setOnToggle(useModIcon -> {
			textureField.setEditable(!useModIcon);
			updateTextureIcon(useModIcon);
		});
		textureSettings.add(toggle, 0, 24);

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

		root.add(textureToggle, 0, 100, 84, 20);
		root.add(itemToggle, 88, 100, 84, 20);

		root.add(new WShardPanel(shard), 176, -7);
		root.validate(this);
	}
}
