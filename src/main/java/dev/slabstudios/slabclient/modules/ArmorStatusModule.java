package dev.slabstudios.slabclient.modules;

import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

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
	public void render(boolean force) {
		if (!visible && !force) return;

		Minecraft mc = Minecraft.getMinecraft();
		if (mc.thePlayer == null) return;

		int itemY = y;
		
		// Set up GUI rendering states for items
		GlStateManager.pushMatrix();
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.enableDepth();

		// 1. Render armor from Helmet (index 3) down to Boots (index 0)
		for (int i = 3; i >= 0; i--) {
			ItemStack stack = mc.thePlayer.inventory.armorInventory[i];
			if (stack != null) {
				renderItemSlot(mc, stack, x, itemY);
				itemY += 18;
			}
		}

		// 2. Render currently held main hand item
		ItemStack handStack = mc.thePlayer.getCurrentEquippedItem();
		if (handStack != null) {
			renderItemSlot(mc, handStack, x, itemY);
			itemY += 18;
		}

		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableDepth();
		GlStateManager.popMatrix();
	}

	private void renderItemSlot(Minecraft mc, ItemStack stack, int itemX, int itemY) {
		// Render the item icon
		mc.getRenderItem().renderItemAndEffectIntoGUI(stack, itemX, itemY);
		// Render standard vanilla overlay (durability bar / stack size)
		mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, itemX, itemY);

		// Render custom durability text beside the item
		if (stack.isItemStackDamageable()) {
			int maxDamage = stack.getMaxDamage();
			int currentDamage = stack.getItemDamage();
			int durability = maxDamage - currentDamage;
			
			float pct = (float) durability / (float) maxDamage;
			int color = 0xFF55FF55; // Green
			if (pct <= 0.3F) {
				color = 0xFFFF5555; // Red
			} else if (pct <= 0.7F) {
				color = 0xFFFFFF55; // Yellow
			}

			String text = String.valueOf(durability);
			mc.fontRendererObj.drawStringWithShadow(text, itemX + 20, itemY + 4, color);
		} else if (stack.stackSize > 1) {
			// If not damageable but stacked (e.g. food/potions), show stack size next to it
			String text = "x" + stack.stackSize;
			mc.fontRendererObj.drawStringWithShadow(text, itemX + 20, itemY + 4, 0xFFFFFFFF);
		}
	}

	@Override
	public int getWidth() {
		return 64;
	}

	@Override
	public int getHeight() {
		// We dynamically compute height based on how many items are currently equipped,
		// or use a fixed size to allocate bounding box space.
		// Standard maximum height is 5 items * 18 = 90. Let's return that.
		return 90;
	}

}
