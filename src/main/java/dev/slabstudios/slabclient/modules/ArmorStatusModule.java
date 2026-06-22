package dev.slabstudios.slabclient.modules;

import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ArmorStatusModule extends Module {

	public ArmorStatusModule(int x, int y) {
		super(x, y);
		this.key = "Armor Status";
		this.value = "Enabled";
		this.visible = true;
	}

	@Override
	public void update() {
		if (enabled) {
			this.value = "Enabled";
		} else {
			this.value = "Disabled";
		}
	}

	@Override
	public void render(GuiGraphicsExtractor guiGraphics, boolean force) {
		if (!visible && !force) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;

		int itemY = y;
		
		// 1. Render armor from Helmet down to Boots
		EquipmentSlot[] armorSlots = {
			EquipmentSlot.HEAD,
			EquipmentSlot.CHEST,
			EquipmentSlot.LEGS,
			EquipmentSlot.FEET
		};
		for (EquipmentSlot slot : armorSlots) {
			ItemStack stack = mc.player.getItemBySlot(slot);
			if (stack != null && !stack.isEmpty()) {
				renderItemSlot(guiGraphics, mc, stack, x, itemY);
				itemY += 18;
			}
		}

		// 2. Render currently held main hand item
		ItemStack mainHand = mc.player.getItemBySlot(EquipmentSlot.MAINHAND);
		if (mainHand != null && !mainHand.isEmpty()) {
			renderItemSlot(guiGraphics, mc, mainHand, x, itemY);
			itemY += 18;
		}

		// 3. Render currently held off hand item
		ItemStack offHand = mc.player.getItemBySlot(EquipmentSlot.OFFHAND);
		if (offHand != null && !offHand.isEmpty()) {
			renderItemSlot(guiGraphics, mc, offHand, x, itemY);
			itemY += 18;
		}
	}

	private void renderItemSlot(GuiGraphicsExtractor guiGraphics, Minecraft mc, ItemStack stack, int itemX, int itemY) {
		// Render the item icon
		guiGraphics.item(stack, itemX, itemY);
		// Render standard vanilla overlay (durability bar / stack size)
		guiGraphics.itemDecorations(mc.font, stack, itemX, itemY);

		// Render custom durability text beside the item
		if (stack.isDamageableItem()) {
			int maxDamage = stack.getMaxDamage();
			int currentDamage = stack.getDamageValue();
			int durability = maxDamage - currentDamage;
			
			float pct = (float) durability / (float) maxDamage;
			int color = 0xFF55FF55; // Green
			if (pct <= 0.3F) {
				color = 0xFFFF5555; // Red
			} else if (pct <= 0.7F) {
				color = 0xFFFFFF55; // Yellow
			}

			String text = String.valueOf(durability);
			guiGraphics.text(mc.font, text, itemX + 20, itemY + 4, color, true);
		} else if (stack.getCount() > 1) {
			// If not damageable but stacked (e.g. food/potions), show stack size next to it
			String text = "x" + stack.getCount();
			guiGraphics.text(mc.font, text, itemX + 20, itemY + 4, 0xFFFFFFFF, true);
		}
	}

	@Override
	public int getWidth() {
		return 64;
	}

	@Override
	public int getHeight() {
		// Standard maximum height is 6 items * 18 = 108.
		return 108;
	}

}
